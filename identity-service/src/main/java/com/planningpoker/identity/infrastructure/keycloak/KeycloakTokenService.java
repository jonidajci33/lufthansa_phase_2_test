package com.planningpoker.identity.infrastructure.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calls Keycloak's OpenID Connect token and logout endpoints directly via HTTP.
 * Uses {@link java.net.http.HttpClient} to avoid additional dependencies.
 */
@Component
public class KeycloakTokenService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakTokenService.class);
    private static final String CLIENT_ID = "planning-poker-spa";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String tokenEndpoint;
    private final String logoutEndpoint;

    public KeycloakTokenService(KeycloakProperties properties, ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;

        String base = properties.serverUrl() + "/realms/" + properties.realm()
                + "/protocol/openid-connect";
        this.tokenEndpoint = base + "/token";
        this.logoutEndpoint = base + "/logout";
    }

    /**
     * Authenticates a user with username/password via the Resource Owner Password Credentials grant.
     *
     * @return parsed token response
     */
    public TokenResponse login(String username, String password) {
        Map<String, String> params = Map.of(
                "grant_type", "password",
                "client_id", CLIENT_ID,
                "username", username,
                "password", password,
                "scope", "openid"
        );
        return callTokenEndpoint(params);
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @return parsed token response with new tokens
     */
    public TokenResponse refresh(String refreshToken) {
        Map<String, String> params = Map.of(
                "grant_type", "refresh_token",
                "client_id", CLIENT_ID,
                "refresh_token", refreshToken,
                "scope", "openid"
        );
        return callTokenEndpoint(params);
    }

    /**
     * Invalidates a refresh token at the Keycloak logout endpoint.
     */
    public void logout(String refreshToken) {
        Map<String, String> params = Map.of(
                "client_id", CLIENT_ID,
                "refresh_token", refreshToken
        );

        String body = encodeFormData(params);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(logoutEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("Keycloak logout failed: status={}, body={}", response.statusCode(), response.body());
                throw new KeycloakTokenException("Logout failed with status " + response.statusCode());
            }
            log.debug("Keycloak logout successful");
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KeycloakTokenException("Failed to call Keycloak logout endpoint", e);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private TokenResponse callTokenEndpoint(Map<String, String> params) {
        String body = encodeFormData(params);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Keycloak token request failed: status={}, body={}", response.statusCode(), response.body());
                throw new KeycloakTokenException(
                        "Token request failed with status " + response.statusCode());
            }

            JsonNode json = objectMapper.readTree(response.body());
            return new TokenResponse(
                    json.get("access_token").asText(),
                    json.get("refresh_token").asText(),
                    json.get("expires_in").asLong()
            );
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KeycloakTokenException("Failed to call Keycloak token endpoint", e);
        }
    }

    private static String encodeFormData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * Holds the parsed fields from a Keycloak token response.
     */
    public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}

    /**
     * Runtime exception for Keycloak token endpoint failures.
     */
    public static class KeycloakTokenException extends RuntimeException {
        public KeycloakTokenException(String message) {
            super(message);
        }

        public KeycloakTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
