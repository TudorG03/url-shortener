package eu.deic.url_shortener.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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
class RedirectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UrlRepository urlRepository;

    private String rawApiKey;

    @BeforeEach
    void setUp() {
        urlRepository.deleteAll();
        apiKeyRepository.deleteAll();

        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setOwner("testOwner");
        rawApiKey = apiKeyService.createApiKey(request).getApiKey();
    }

    @Test
    void redirect_shouldReturn302_whenShortCodeIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example.com",
                            "customCode": "redir"
                        }
                        """));

        mockMvc.perform(get("/redir"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example.com"));
    }

    @Test
    void redirect_shouldReturn302_fromCache_onSecondRequest() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example-cached.com",
                            "customCode": "cached"
                        }
                        """));

        mockMvc.perform(get("/cached"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example-cached.com"));

        mockMvc.perform(get("/cached"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example-cached.com"));
    }

    @Test
    void redirect_shouldReturn404_whenShortCodeDoesNotExist() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_shouldReturn410_whenShortCodeIsInactive() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example-gone.com",
                            "customCode": "gone"
                        }
                        """));

        mockMvc.perform(delete("/api/v1/urls/gone")
                .header("X-API-Key", rawApiKey));

        mockMvc.perform(get("/gone"))
                .andExpect(status().isGone());
    }

    @Test
    void redirect_shouldReturn410_afterDeletion_whenCacheWasPopulated() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                .header("X-API-Key", rawApiKey)
                .contentType("application/json")
                .content("""
                        {
                            "originalUrl": "https://www.example-invalidate.com",
                            "customCode": "invalidate"
                        }
                        """));

        mockMvc.perform(get("/invalidate"))
                .andExpect(status().isFound());

        mockMvc.perform(delete("/api/v1/urls/invalidate")
                .header("X-API-Key", rawApiKey))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/invalidate"))
                .andExpect(status().isGone());
    }
}
