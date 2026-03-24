package com.planningpoker.identity.infrastructure.config;

import com.planningpoker.identity.infrastructure.keycloak.KeycloakProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that creates the Keycloak Admin Client bean
 * from the {@link KeycloakProperties} configuration.
 */
@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakConfig {

    @Bean
    public Keycloak keycloakAdminClient(KeycloakProperties properties) {
        return KeycloakBuilder.builder()
                .serverUrl(properties.serverUrl())
                .realm("master")
                .clientId(properties.adminClientId())
                .username(properties.adminUsername())
                .password(properties.adminPassword())
                .grantType("password")
                .build();
    }
}
