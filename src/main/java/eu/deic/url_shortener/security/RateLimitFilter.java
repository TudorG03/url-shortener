package eu.deic.url_shortener.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.deic.url_shortener.dto.ErrorResponse;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.public.capacity}")
    private int publicCapacity;

    @Value("${app.rate-limit.authenticated.capacity}")
    private int authenticatedCapacity;

    @Value("${app.rate-limit.public.refill-seconds}")
    private int publicRefillSeconds;

    @Value("${app.rate-limit.authenticated.refill-seconds}")
    private int authenticatedRefillSeconds;

    public RateLimitFilter(ObjectMapper objectMapper,
            RateLimitService rateLimitService) {
        this.objectMapper = objectMapper;
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        int capacity = isAuthenticated ? authenticatedCapacity : publicCapacity;
        int refillSeconds = isAuthenticated ? authenticatedRefillSeconds : publicRefillSeconds;
        String key = isAuthenticated ? auth.getName() : request.getRemoteAddr();

        ConsumptionProbe probe = rateLimitService.consume(key, capacity, refillSeconds);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.getWriter().write(objectMapper.writeValueAsString(getErrorResponse(waitSeconds)));
        }
    }

    private ErrorResponse getErrorResponse(long waitSeconds) {
        return ErrorResponse.builder()
                .status(429)
                .error("Too Many Requests")
                .message("Rate limit exceeded. Retry after " + waitSeconds + " seconds.")
                .timestamp(Instant.now())
                .build();
    }
}
