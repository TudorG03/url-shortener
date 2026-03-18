package eu.deic.url_shortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.deic.url_shortener.dto.CreateApiKeyRequest;
import eu.deic.url_shortener.dto.CreateApiKeyResponse;
import eu.deic.url_shortener.service.ApiKeyService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/keys")
    public ResponseEntity<CreateApiKeyResponse> createApiKey(
            @RequestBody @Valid CreateApiKeyRequest request) {
        CreateApiKeyResponse response = apiKeyService.createApiKey(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
