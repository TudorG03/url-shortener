package eu.deic.url_shortener.service;

import eu.deic.url_shortener.domain.Url;
import eu.deic.url_shortener.dto.CreateUrlRequest;
import eu.deic.url_shortener.dto.CreateUrlResponse;
import eu.deic.url_shortener.dto.UrlStatsResponse;
import eu.deic.url_shortener.exception.ForbiddenException;
import eu.deic.url_shortener.exception.ShortCodeAlreadyExistsException;
import eu.deic.url_shortener.exception.UrlNotFoundException;
import eu.deic.url_shortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService(urlRepository, "http://localhost:8080");
    }

    @Test
    void createUrl_shouldSaveAndReturnResponse_whenRequestIsValid() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://www.example.com");

        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlRepository.save(any(Url.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateUrlResponse response = urlService.createUrl(request, "testOwner");

        assertThat(response.getOriginalUrl()).isEqualTo("https://www.example.com");
        assertThat(response.getShortCode()).isNotNull();
        assertThat(response.getShortUrl()).startsWith("http://localhost:8080/");
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    void createUrl_shouldThrowShortCodeAlreadyExistsException_whenCustomCodeIsTaken() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://www.example.com");
        request.setCustomCode("custom");

        when(urlRepository.existsByShortCode(eq("custom"))).thenReturn(true);

        assertThrows(ShortCodeAlreadyExistsException.class, () -> urlService.createUrl(request, "testOwner"));
    }

    @Test
    void getUrlForRedirect_shouldReturnOriginalUrl_whenShortCodeIsValid() {
        Url url = new Url();
        url.setOriginalUrl("https://example.com");

        when(urlRepository.findByShortCodeAndActiveTrue(eq("custom")))
                .thenReturn(Optional.of(url));

        String result = urlService.getUrlForRedirect("custom");

        assertThat(result).isEqualTo("https://example.com");
        verify(urlRepository, times(1)).incrementClickCount(eq("custom"));
    }

    @Test
    void getUrlForRedirect_shouldThrowUrlNotFoundException_whenShortCodeIsUnknown() {
        when(urlRepository.findByShortCodeAndActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getUrlForRedirect("unknown"));
    }

    @Test
    void getStats_shouldReturnUrlStatsResponse_whenUrlIsValid() {
        Url url = new Url();
        url.setOriginalUrl("https://example.com");
        url.setShortCode("custom");
        url.setActive(false);

        when(urlRepository.findByShortCode("custom"))
                .thenReturn(Optional.of(url));

        UrlStatsResponse response = urlService.getStats("custom");

        assertThat(response.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(response.getShortCode()).isEqualTo("custom");
        assertThat(response.getActive()).isFalse();
    }

    @Test
    void getStats_shouldThrowUrlNotFoundException_whenUrlIsUnknown() {
        when(urlRepository.findByShortCode("custom"))
                .thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getStats("custom"));
    }

    @Test
    void deleteUrl_shouldSetActiveToFalse_whenIsOwner() {
        Url url = new Url();
        url.setOriginalUrl("https://example.com");
        url.setShortCode("custom");
        url.setCreatedBy("testOwner");

        when(urlRepository.findByShortCodeAndActiveTrue(eq("custom")))
                .thenReturn(Optional.of(url));

        urlService.deleteUrl("custom", "testOwner");

        ArgumentCaptor<Url> captor = ArgumentCaptor.forClass(Url.class);
        verify(urlRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo("testOwner");
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void deleteUrl_shouldThrowForbiddenException_whenIsNotOwner() {
        Url url = new Url();
        url.setOriginalUrl("https://example.com");
        url.setShortCode("custom");
        url.setCreatedBy("testOwner");

        when(urlRepository.findByShortCodeAndActiveTrue("custom"))
                .thenReturn(Optional.of(url));

        assertThrows(ForbiddenException.class, () -> urlService.deleteUrl("custom", "somebody"));
    }

    @Test
    void deleteUrl_shouldThrowUrlNotFoundException_whenShortCodeIsUnknown() {
        when(urlRepository.findByShortCodeAndActiveTrue("custom"))
                .thenReturn(Optional.empty());
        when(urlRepository.existsByShortCode(eq("custom")))
                .thenReturn(false);

        assertThrows(UrlNotFoundException.class, () -> urlService.deleteUrl("custom", "somebody"));
    }
}
