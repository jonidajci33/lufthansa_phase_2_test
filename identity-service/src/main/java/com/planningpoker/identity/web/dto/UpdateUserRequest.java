package com.planningpoker.identity.web.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for updating a user's profile.
 */
public record UpdateUserRequest(
        @Size(max = 255) String displayName,
        @Size(max = 500) String avatarUrl
) {}
