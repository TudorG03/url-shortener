package eu.deic.url_shortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import eu.deic.url_shortener.domain.ApiKey;
import eu.deic.url_shortener.dto.CreateApiKeyRequest;
import eu.deic.url_shortener.dto.CreateApiKeyResponse;
import eu.deic.url_shortener.repository.ApiKeyRepository;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApiKeyService apiKeyService;

    @Test
    void createApiKey_shouldReturnResponse_withRawKey() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");

        ApiKey saved = ApiKey.builder()
                .owner("testOwner")
                .keyHash("hashed")
                .keyPrefix("aB3xK9mP")
                .active(true)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(saved);

        CreateApiKeyResponse response = apiKeyService.createApiKey(request);

        assertThat(response.getApiKey()).isNotNull();
        assertThat(response.getApiKey()).startsWith("urlsh_");
        assertThat(response.getApiKey()).hasSizeGreaterThan(6);
        assertThat(response.getOwner()).isEqualTo("testOwner");
    }

    @Test
    void createApiKey_shouldHashRawKey_beforeSaving() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");

        ApiKey saved = ApiKey.builder()
                .owner("testOwner")
                .keyHash("hashed")
                .keyPrefix("aB3xK9mP")
                .createdAt(Instant.now())
                .active(true)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(saved);

        apiKeyService.createApiKey(request);

        verify(passwordEncoder).encode(anyString());

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        ApiKey capturedKey = captor.getValue();

        assertThat(capturedKey.getKeyHash()).isEqualTo("hashed");
        assertThat(capturedKey.getKeyHash()).doesNotStartWith("urlsh_");
    }

    @Test
    void createApiKey_shouldSaveCorrectPrefix() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");

        ApiKey saved = ApiKey.builder()
                .owner("testOwner")
                .keyHash("hashed")
                .keyPrefix("aB3xK9mP")
                .createdAt(Instant.now())
                .active(true)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(saved);

        apiKeyService.createApiKey(request);

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        ApiKey capturedKey = captor.getValue();

        assertThat(capturedKey.getKeyPrefix()).hasSize(8);
        assertThat(capturedKey.getOwner()).isEqualTo("testOwner");
    }

    @Test
    void createApiKey_shouldSaveActiveKey() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");

        ApiKey saved = ApiKey.builder()
                .owner("testOwner")
                .keyHash("hashed")
                .keyPrefix("aB3xK9mP")
                .createdAt(Instant.now())
                .active(true)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(saved);

        apiKeyService.createApiKey(request);

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());

        assertThat(captor.getValue().getActive()).isTrue();
    }
}
