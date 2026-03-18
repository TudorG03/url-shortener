package eu.deic.url_shortener.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import eu.deic.url_shortener.domain.ApiKey;
import eu.deic.url_shortener.repository.ApiKeyRepository;

@Service
public class ApiKeyAuthService implements UserDetailsService {

    private final ApiKeyRepository apiKeyRepository;

    private final PasswordEncoder passwordEncoder;

    private static final int PREFIX_START = 6;

    private static final int PREFIX_LENGTH = 8;

    private static final String USERNAME_NOT_FOUND_EX_MESSAGE = "Invalid API key";

    public ApiKeyAuthService(ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String prefix = username.substring(PREFIX_START, PREFIX_START + PREFIX_LENGTH);
        List<ApiKey> keys = apiKeyRepository.findByKeyPrefixAndActiveTrue(prefix);
        for (ApiKey key : keys) {
            if (passwordEncoder.matches(username, key.getKeyHash())) {
                return new ApiKeyPrincipal(key);
            }
        }

        throw new UsernameNotFoundException(USERNAME_NOT_FOUND_EX_MESSAGE);
    }
}
