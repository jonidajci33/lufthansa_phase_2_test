package com.planningpoker.identity.web.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Standard user response returned from public API endpoints.
 */
public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String displayName,
        String avatarUrl,
        boolean isActive,
        Set<String> roles,
        Instant createdAt
) {
}
