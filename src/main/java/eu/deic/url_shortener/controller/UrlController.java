package eu.deic.url_shortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.deic.url_shortener.dto.CreateUrlRequest;
import eu.deic.url_shortener.dto.CreateUrlResponse;
import eu.deic.url_shortener.dto.UrlStatsResponse;
import eu.deic.url_shortener.service.UrlService;
import jakarta.validation.Valid;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateUrlResponse> createShortCode(
            @RequestBody @Valid CreateUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String owner = userDetails.getUsername();
        CreateUrlResponse response = urlService.createUrl(request, owner);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/urls/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getStats(@PathVariable String shortCode) {
        UrlStatsResponse response = urlService.getStats(shortCode);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/api/v1/urls/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        String owner = userDetails.getUsername();
        urlService.deleteUrl(shortCode, owner);

        return ResponseEntity.noContent().build();
    }
}
