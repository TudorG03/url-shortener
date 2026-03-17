package eu.deic.url_shortener.service;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.deic.url_shortener.domain.Url;
import eu.deic.url_shortener.dto.CreateUrlRequest;
import eu.deic.url_shortener.dto.CreateUrlResponse;
import eu.deic.url_shortener.dto.UrlStatsResponse;
import eu.deic.url_shortener.exception.ForbiddenException;
import eu.deic.url_shortener.exception.ShortCodeAlreadyExistsException;
import eu.deic.url_shortener.exception.UrlInactiveException;
import eu.deic.url_shortener.exception.UrlNotFoundException;
import eu.deic.url_shortener.repository.UrlRepository;
import eu.deic.url_shortener.util.CodeGenerator;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final String baseUrl;
    private static final int SHORT_CODE_LEN = 6;

    public UrlService(UrlRepository urlRepository,
            @Value("${app.base-url}") String baseUrl) {
        this.urlRepository = urlRepository;
        this.baseUrl = baseUrl;
    }

    public CreateUrlResponse createUrl(CreateUrlRequest request, String owner) {
        String shortCode;
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            if (urlRepository.existsByShortCode(request.getCustomCode())) {
                throw new ShortCodeAlreadyExistsException(request.getCustomCode());
            }
            shortCode = request.getCustomCode();
        } else {
            do {
                shortCode = CodeGenerator.generate(SHORT_CODE_LEN);
            } while (urlRepository.existsByShortCode(shortCode));
        }

        Url url = Url.builder()
                .createdBy(owner)
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .expiresAt(request.getExpiresAt())
                .build();

        Url saved = urlRepository.save(url);

        return CreateUrlResponse.builder()
                .originalUrl(saved.getOriginalUrl())
                .shortUrl(baseUrl + "/" + saved.getShortCode())
                .shortCode(saved.getShortCode())
                .createdAt(saved.getCreatedAt())
                .expiresAt(saved.getExpiresAt())
                .build();
    }

    public String getUrlForRedirect(String shortCode) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> {
                    if (urlRepository.existsByShortCode(shortCode)) {
                        return new UrlInactiveException(shortCode);
                    }
                    return new UrlNotFoundException(shortCode);
                });

        urlRepository.incrementClickCount(shortCode);
        return url.getOriginalUrl();
    }

    public UrlStatsResponse getStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));

        return UrlStatsResponse.builder()
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .active(url.getActive())
                .build();
    }

    public void deleteUrl(String shortCode, String owner) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> {
                    if (urlRepository.existsByShortCode(shortCode)) {
                        return new UrlInactiveException(shortCode);
                    }
                    return new UrlNotFoundException(shortCode);
                });

        if (!owner.equals(url.getCreatedBy())) {
            throw new ForbiddenException(shortCode, owner);
        }

        url.setActive(false);
        urlRepository.save(url);
    }
}
