package eu.deic.url_shortener.controller;

import eu.deic.url_shortener.service.UrlService;

public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }
}
