package eu.deic.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateApiKeyRequest {

    @NotBlank(message = "Owner name must be provided")
    @Size(max = 30, message = "Owner name must have length maximum 30")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Owner name must be alphanumeric")
    private String owner;
}
