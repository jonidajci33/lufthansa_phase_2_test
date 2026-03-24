package com.planningpoker.room.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for outgoing REST clients used by infrastructure adapters.
 */
@Configuration
public class RestClientConfig {

    @Value("${app.services.identity-url}")
    private String identityServiceUrl;

    @Bean("identityRestClient")
    public RestClient identityRestClient() {
        return RestClient.builder()
                .baseUrl(identityServiceUrl)
                .build();
    }
}
