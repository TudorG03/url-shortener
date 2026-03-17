package eu.deic.url_shortener.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.deic.url_shortener.config.PasswordEncoderConfig;
import eu.deic.url_shortener.config.RedisClientConfig;
import eu.deic.url_shortener.config.SecurityConfig;
import eu.deic.url_shortener.exception.UrlInactiveException;
import eu.deic.url_shortener.exception.UrlNotFoundException;
import eu.deic.url_shortener.service.UrlService;
import eu.deic.url_shortener.security.ApiKeyAuthFilter;
import eu.deic.url_shortener.security.ApiKeyAuthService;
import eu.deic.url_shortener.security.RateLimitFilter;
import eu.deic.url_shortener.security.RateLimitService;
import eu.deic.url_shortener.repository.ApiKeyRepository;

import io.github.bucket4j.ConsumptionProbe;
import io.lettuce.core.RedisClient;

@WebMvcTest(RedirectController.class)
@Import({ SecurityConfig.class, ApiKeyAuthFilter.class, RateLimitFilter.class, ApiKeyAuthService.class,
        PasswordEncoderConfig.class, RedisClientConfig.class })
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @MockitoBean
    RateLimitService rateLimitService;

    @MockitoBean
    private RedisClient redisClient;

    @MockitoBean
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ConsumptionProbe mockProbe = mock(ConsumptionProbe.class);
        when(mockProbe.isConsumed()).thenReturn(true);
        when(rateLimitService.consume(anyString(), anyInt(), anyInt())).thenReturn(mockProbe);
    }

    @Test
    void redirect_shouldReturnLocationHeader_whenShortCodeIsValid() throws Exception {
        when(urlService.getUrlForRedirect(eq("custom")))
                .thenReturn("https://example.com");

        mockMvc.perform(get("/custom"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void redirect_shouldReturn404Status_whenShortCodeIsNotValid() throws Exception {
        when(urlService.getUrlForRedirect(eq("custom")))
                .thenThrow(new UrlNotFoundException("custom"));

        mockMvc.perform(get("/custom"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_shouldReturn410Status_whenShortCodeIsInactive() throws Exception {
        when(urlService.getUrlForRedirect(eq("custom")))
                .thenThrow(new UrlInactiveException("custom"));

        mockMvc.perform(get("/custom"))
                .andExpect(status().isGone());
    }
}
