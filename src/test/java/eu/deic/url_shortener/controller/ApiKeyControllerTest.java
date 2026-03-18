package eu.deic.url_shortener.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.deic.url_shortener.config.PasswordEncoderConfig;
import eu.deic.url_shortener.config.RedisClientConfig;
import eu.deic.url_shortener.config.SecurityConfig;
import eu.deic.url_shortener.dto.CreateApiKeyRequest;
import eu.deic.url_shortener.dto.CreateApiKeyResponse;
import eu.deic.url_shortener.repository.ApiKeyRepository;
import eu.deic.url_shortener.security.ApiKeyAuthFilter;
import eu.deic.url_shortener.security.ApiKeyAuthService;
import eu.deic.url_shortener.security.RateLimitFilter;
import eu.deic.url_shortener.security.RateLimitService;
import eu.deic.url_shortener.service.ApiKeyService;
import io.github.bucket4j.ConsumptionProbe;
import io.lettuce.core.RedisClient;

@WebMvcTest(value = ApiKeyController.class, properties = {
        "app.admin.secret=test-secret"
})
@Import({ SecurityConfig.class, ApiKeyAuthFilter.class, RateLimitFilter.class, ApiKeyAuthService.class,
        PasswordEncoderConfig.class, RedisClientConfig.class })
class ApiKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @MockitoBean
    private ApiKeyService apiKeyService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private RedisClient redisClient;

    @BeforeEach
    void setUp() {
        ConsumptionProbe mockProbe = mock(ConsumptionProbe.class);
        when(mockProbe.isConsumed()).thenReturn(true);
        when(rateLimitService.consume(anyString(), anyInt(), anyInt())).thenReturn(mockProbe);
    }

    @Test
    void createApiKey_shouldCreateApiKey_whenOwnerIsValid() throws Exception {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");

        CreateApiKeyResponse response = CreateApiKeyResponse.builder()
                .apiKey("urlsh_ag3hb98v")
                .owner("testOwner")
                .build();

        when(apiKeyService.createApiKey(any(CreateApiKeyRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/keys")
                .header("X-Admin-Secret", "test-secret")
                .contentType("application/json")
                .content("""
                        {
                            "owner": "testOwner"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey").value("urlsh_ag3hb98v"))
                .andExpect(jsonPath("$.owner").value("testOwner"));
    }

    @Test
    void createApiKey_shouldReturn400_whenOwnerIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/admin/keys")
                .header("X-Admin-Secret", "test-secret")
                .contentType("application/json")
                .content("""
                        {
                            "owner": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }
}
