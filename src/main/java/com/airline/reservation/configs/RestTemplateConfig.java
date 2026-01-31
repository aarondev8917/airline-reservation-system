package com.airline.reservation.configs;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for HTTP clients used to call third-party APIs (e.g. Aviationstack).
 * Uses connect/read timeouts suitable for production.
 */
@Configuration
public class RestTemplateConfig {

    private static final int CONNECT_TIMEOUT_SEC = 5;
    private static final int READ_TIMEOUT_SEC = 15;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SEC))
                .setReadTimeout(Duration.ofSeconds(READ_TIMEOUT_SEC))
                .build();
    }
}

