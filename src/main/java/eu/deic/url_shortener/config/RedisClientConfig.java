package eu.deic.url_shortener.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

@Configuration
public class RedisClientConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.timeout}")
    private long millis;

    @Bean
    public RedisClient redisClient() {
        RedisURI redisUri = RedisURI.builder()
                .redis(host, port)
                .withTimeout(Duration.ofMillis(millis))
                .withSsl(sslEnabled)
                .build();

        return RedisClient.create(redisUri);
    }
}
