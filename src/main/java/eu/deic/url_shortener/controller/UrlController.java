package eu.deic.url_shortener.controller;

import org.springframework.web.bind.annotation.RestController;

import eu.deic.url_shortener.service.UrlService;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }
}
