package com.planningpoker.room.infrastructure.client;

import com.planningpoker.room.application.port.out.UserValidationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * REST client adapter that implements {@link UserValidationPort} by calling the
 * Identity Service's internal user lookup endpoint.
 * <p>
 * Returns {@code true} if the user exists (HTTP 200), {@code false} if not found (HTTP 404).
 * Falls back to {@code false} on circuit-breaker open or unexpected errors.
 */
@Component
public class IdentityServiceClient implements UserValidationPort {

    private static final Logger log = LoggerFactory.getLogger(IdentityServiceClient.class);

    private final RestClient restClient;

    public IdentityServiceClient(@Qualifier("identityRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public boolean userExists(UUID userId) {
        try {
            return doUserExists(userId);
        } catch (Exception ex) {
            log.warn("Circuit breaker fallback: assuming user {} does not exist. Cause: {}", userId, ex.getMessage());
            return false;
        }
    }

    private boolean doUserExists(UUID userId) {
        var response = restClient.get()
                .uri("/internal/users/{id}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    // 404 is expected — user not found
                })
                .toBodilessEntity();

        return response.getStatusCode().is2xxSuccessful();
    }
}
