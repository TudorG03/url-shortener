package eu.deic.url_shortener.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CreateUrlResponse {

    private String shortCode;
    private String shortUrl; // full URL e.g. http://localhost:8080/abc123
    private String originalUrl;
    private Instant expiresAt;
    private Instant createdAt;
}
