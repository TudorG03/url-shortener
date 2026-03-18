package eu.deic.url_shortener.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.endsWith;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import eu.deic.url_shortener.dto.CreateApiKeyRequest;
import eu.deic.url_shortener.repository.ApiKeyRepository;
import eu.deic.url_shortener.repository.UrlRepository;
import eu.deic.url_shortener.service.ApiKeyService;

@AutoConfigureMockMvc
class UrlIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UrlRepository urlRepository;

    private String rawApiKey;

    private String invalidDeleteApiKey;

    @BeforeEach
    void setUp() {
        urlRepository.deleteAll();
        apiKeyRepository.deleteAll();

        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");
        rawApiKey = apiKeyService.createApiKey(request).getApiKey();

        request.setOwner("deleter");
        invalidDeleteApiKey = apiKeyService.createApiKey(request).getApiKey();
    }

    @Test
    void createUrl_shouldReturn201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example.com"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty());
    }

    @Test
    void createUrl_shouldReturn201_whenRequestIsValidWithCustomCode() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example.com",
                            "customCode": "custom"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.shortCode").value("custom"))
                .andExpect(jsonPath("$.shortUrl").value(endsWith("/custom")));
    }

    @Test
    void getStats_shouldReturn200_whenShortCodeIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example-stats.com",
                            "customCode": "stats"
                        }
                        """));

        mockMvc.perform(get("/api/v1/urls/stats")
                .header("X-API-Key", rawApiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example-stats.com"))
                .andExpect(jsonPath("$.shortCode").value("stats"))
                .andExpect(jsonPath("$.shortUrl").value(endsWith("/stats")));
    }

    @Test
    void deleteUrl_shouldReturn204_whenOwnerIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example-delete.com",
                            "customCode": "delete"
                        }
                        """));

        mockMvc.perform(delete("/api/v1/urls/delete")
                .header("X-API-Key", rawApiKey))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUrl_shouldReturn403_whenUserIsNotOwner() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example-delete2.com",
                            "customCode": "delete2"
                        }
                        """));

        mockMvc.perform(delete("/api/v1/urls/delete2")
                .header("X-API-Key", invalidDeleteApiKey))
                .andExpect(status().isForbidden());
    }
}
