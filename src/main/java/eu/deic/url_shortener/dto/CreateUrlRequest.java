package eu.deic.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Data
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "Must be a valid URL")
    private String originalUrl;

    @Size(max = 10, message = "Custom code must be 10 characters or less")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Custom code must be alphanumeric")
    private String customCode; // optional — null means auto-generate

    private Instant expiresAt; // optional — null means no expiry
}
