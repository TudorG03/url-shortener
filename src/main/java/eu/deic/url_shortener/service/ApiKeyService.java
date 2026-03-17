package eu.deic.url_shortener.service;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import eu.deic.url_shortener.domain.ApiKey;
import eu.deic.url_shortener.dto.CreateApiKeyRequest;
import eu.deic.url_shortener.dto.CreateApiKeyResponse;
import eu.deic.url_shortener.repository.ApiKeyRepository;
import eu.deic.url_shortener.util.CodeGenerator;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    private final PasswordEncoder passwordEncoder;

    private static final int API_KEY_LENGTH = 32;

    private static final String API_KEY_PREFIX = "urlsh_";

    private static final int API_KEY_PREFIX_LENGTH = 8;

    public ApiKeyService(PasswordEncoder passwordEncoder, ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CreateApiKeyResponse createApiKey(CreateApiKeyRequest request) {
        String rawKey = CodeGenerator.generate(API_KEY_LENGTH);
        rawKey = API_KEY_PREFIX + rawKey;
        String prefix = rawKey.substring(
                API_KEY_PREFIX.length(),
                API_KEY_PREFIX.length() + API_KEY_PREFIX_LENGTH);

        String hashedKey = passwordEncoder.encode(rawKey);

        ApiKey saved = ApiKey.builder()
                .keyHash(hashedKey)
                .keyPrefix(prefix)
                .owner(request.getOwner())
                .build();

        apiKeyRepository.save(saved);

        CreateApiKeyResponse response = CreateApiKeyResponse.builder()
                .apiKey(rawKey)
                .owner(request.getOwner())
                .createdAt(saved.getCreatedAt())
                .build();

        return response;
    }
}
