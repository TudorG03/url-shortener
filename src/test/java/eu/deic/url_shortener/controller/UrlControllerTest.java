package eu.deic.url_shortener.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import eu.deic.url_shortener.config.PasswordEncoderConfig;
import eu.deic.url_shortener.config.SecurityConfig;
import eu.deic.url_shortener.dto.CreateUrlRequest;
import eu.deic.url_shortener.dto.CreateUrlResponse;
import eu.deic.url_shortener.dto.UrlStatsResponse;
import eu.deic.url_shortener.exception.ForbiddenException;
import eu.deic.url_shortener.exception.ShortCodeAlreadyExistsException;
import eu.deic.url_shortener.exception.UrlNotFoundException;
import eu.deic.url_shortener.service.UrlService;
import eu.deic.url_shortener.security.ApiKeyAuthFilter;
import eu.deic.url_shortener.security.ApiKeyAuthService;
import eu.deic.url_shortener.repository.ApiKeyRepository;

@WebMvcTest(UrlController.class)
@Import({ SecurityConfig.class, ApiKeyAuthFilter.class, ApiKeyAuthService.class, PasswordEncoderConfig.class })
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @Test
    @WithMockUser
    void createShortUrl_shouldCreateUrl_whenNoCodeIsProvided() throws Exception {
        CreateUrlResponse response = CreateUrlResponse.builder()
                .originalUrl("https://www.example.com")
                .shortCode("random")
                .shortUrl("http://localhost:8080/random")
                .build();

        when(urlService.createUrl(any(CreateUrlRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "originalUrl": "https://www.example.com"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("random"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"));
    }

    @Test
    @WithMockUser
    void createShortUrl_shouldCreateUrl_whenShortCodeIsValid() throws Exception {
        CreateUrlResponse response = CreateUrlResponse.builder()
                .originalUrl("https://www.example.com")
                .shortCode("custom")
                .shortUrl("http://localhost:8080/random")
                .build();

        when(urlService.createUrl(any(CreateUrlRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "originalUrl": "https://www.example.com",
                            "customCode": "custom"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("custom"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"));
    }

    @Test
    @WithMockUser
    void createShortUrl_shouldReturn409Status_whenShortCodeIsNotValid() throws Exception {
        when(urlService.createUrl(any(CreateUrlRequest.class), any()))
                .thenThrow(ShortCodeAlreadyExistsException.class);

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "originalUrl": "https://www.example.com",
                            "customCode": "custom"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void getStats_shouldReturnUrlStatsResponse_whenShortCodeIsValid() throws Exception {
        UrlStatsResponse response = UrlStatsResponse.builder()
                .shortCode("custom")
                .shortUrl("http://localhost:8080/custom").originalUrl("https://www.example.com")
                .clickCount(3L)
                .build();

        when(urlService.getStats(eq("custom")))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/urls/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("custom"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/custom"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.clickCount").value(3));
    }

    @Test
    @WithMockUser
    void getStats_shouldReturn404Status_whenShortCodeIsNotValid() throws Exception {
        when(urlService.getStats(anyString()))
                .thenThrow(new UrlNotFoundException("custom"));

        mockMvc.perform(get("/api/v1/urls/custom"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteShortUrl_shouldReturn204Status_whenShortCodeAndUserAreValid() throws Exception {
        mockMvc.perform(delete("/api/v1/urls/custom"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteShortUrl_shouldReturn404Status_whenShortCodeIsNotValid() throws Exception {
        doThrow(new UrlNotFoundException("custom"))
                .when(urlService).deleteUrl(anyString(), anyString());

        mockMvc.perform(delete("/api/v1/urls/custom"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteShortUrl_shouldReturn403Status_whenUserIsNotValid() throws Exception {
        doThrow(new ForbiddenException("custom", "user"))
                .when(urlService).deleteUrl(anyString(), anyString());

        mockMvc.perform(delete("/api/v1/urls/custom"))
                .andExpect(status().isForbidden());
    }
}
