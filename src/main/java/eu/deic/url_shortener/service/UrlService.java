package eu.deic.url_shortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.deic.url_shortener.repository.UrlRepository;

@Service
public class UrlService {

    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository,
            @Value("${app.base-url}") String baseUrl) {
        this.urlRepository = urlRepository;
    }
}
