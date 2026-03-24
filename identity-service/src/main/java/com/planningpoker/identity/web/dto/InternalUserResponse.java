package com.planningpoker.identity.web.dto;

import java.util.UUID;

/**
 * Lightweight user response for service-to-service communication.
 */
public record InternalUserResponse(
        UUID id,
        String username,
        String displayName,
        boolean isActive
) {
}
