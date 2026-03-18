package eu.deic.url_shortener.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import eu.deic.url_shortener.repository.ApiKeyRepository;

@AutoConfigureMockMvc
class ApiKeyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @BeforeEach
    void setUp() {
        apiKeyRepository.deleteAll();
    }

    @Test
    void createApiKey_shouldReturn201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/admin/keys")
                .header("X-Admin-Secret", "test-secret")
                .contentType("application/json")
                .content("""
                        {
                            "owner": "testOwner"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey", startsWith("urlsh_")))
                .andExpect(jsonPath("$.owner").value("testOwner"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
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

    @Test
    void createApiKey_shouldReturn403_whenAdminSecretIsWrong() throws Exception {
        mockMvc.perform(post("/api/v1/admin/keys")
                .header("X-Admin-Secret", "wrong-secret")
                .contentType("application/json")
                .content("""
                        {
                            "owner": "testOwner"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createApiKey_shouldReturn403_whenAdminSecretIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/admin/keys")
                .contentType("application/json")
                .content("""
                        {
                            "owner": "testOwner"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createApiKey_shouldPersistKeyInDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/admin/keys")
                .header("X-Admin-Secret", "test-secret")
                .contentType("application/json")
                .content("""
                        {
                            "owner": "testOwner"
                        }
                        """))
                .andExpect(status().isCreated());

        assertThat(apiKeyRepository.findAll()).hasSize(1);
        assertThat(apiKeyRepository.findAll().get(0).getOwner()).isEqualTo("testOwner");
        assertThat(apiKeyRepository.findAll().get(0).getKeyHash()).isNotNull();
        assertThat(apiKeyRepository.findAll().get(0).getKeyPrefix()).hasSize(8);
        assertThat(apiKeyRepository.findAll().get(0).getActive()).isTrue();
    }
}
