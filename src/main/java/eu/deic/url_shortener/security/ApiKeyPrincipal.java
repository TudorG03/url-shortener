package eu.deic.url_shortener.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import eu.deic.url_shortener.domain.ApiKey;

public class ApiKeyPrincipal implements UserDetails {

    private final ApiKey apiKey;

    public ApiKeyPrincipal(ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public @Nullable String getPassword() {
        return apiKey.getKeyHash();
    }

    @Override
    public String getUsername() {
        return apiKey.getOwner();
    }

    @Override
    public boolean isEnabled() {
        return apiKey.getActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
