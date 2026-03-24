package com.planningpoker.identity.infrastructure.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration properties for the Keycloak Admin Client.
 * Bound to the {@code app.keycloak.*} prefix in application.yml.
 */
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(
        String serverUrl,
        String realm,
        String adminClientId,
        String adminUsername,
        String adminPassword
) {
}
