package eu.deic.url_shortener.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import eu.deic.url_shortener.config.SecurityConfig;
import eu.deic.url_shortener.exception.UrlInactiveException;
import eu.deic.url_shortener.exception.UrlNotFoundException;
import eu.deic.url_shortener.service.UrlService;

@WebMvcTest(RedirectController.class)
@Import(SecurityConfig.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void redirect_shouldReturnLocationHeader_whenShortCodeIsValid() throws Exception {
        when(urlService.getUrlForRedirect(eq("custom")))
                .thenReturn("https://example.com");

        mockMvc.perform(get("/custom"))
                .andDo(print())
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
