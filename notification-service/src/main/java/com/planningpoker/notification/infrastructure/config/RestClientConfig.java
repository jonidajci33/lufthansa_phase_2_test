package com.planningpoker.notification.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for outgoing REST clients used by infrastructure adapters.
 */
@Configuration
public class RestClientConfig {

    @Value("${app.room-service.url}")
    private String roomServiceUrl;

    @Bean("roomRestClient")
    public RestClient roomRestClient() {
        return RestClient.builder()
                .baseUrl(roomServiceUrl)
                .build();
    }
}
