package eu.deic.url_shortener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import eu.deic.url_shortener.security.ApiKeyAuthFilter;
import eu.deic.url_shortener.security.RateLimitFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.admin.secret}")
    private String adminSecret;

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter, RateLimitFilter rateLimitFilter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, ApiKeyAuthFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/{shortCode}").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").access((authentication, context) -> {
                            String header = context.getRequest().getHeader("X-Admin-Secret");
                            return new AuthorizationDecision(adminSecret.equals(header));
                        })
                        .anyRequest().authenticated());

        return http.build();
    }
}
