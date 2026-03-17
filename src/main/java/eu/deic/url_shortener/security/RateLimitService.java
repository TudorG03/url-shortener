package eu.deic.url_shortener.security;

import java.time.Duration;

import org.springframework.stereotype.Service;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;

@Service
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    public RateLimitService(LettuceBasedProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public ConsumptionProbe consume(String key, int capacity, int refillSeconds) {
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(capacity)
                        .refillGreedy(capacity, Duration.ofSeconds(refillSeconds)))
                .build();
        return proxyManager.builder()
                .build(key, () -> config)
                .tryConsumeAndReturnRemaining(1);
    }
}
