package eu.deic.url_shortener.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateApiKeyResponse {

    private String apiKey;
    private String owner;
    private Instant createdAt;
}
