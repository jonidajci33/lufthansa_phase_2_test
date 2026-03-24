package com.planningpoker.room.web.dto;

import com.planningpoker.room.domain.InvitationStatus;
import com.planningpoker.room.domain.InvitationType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for invitation information.
 */
public record InvitationResponse(
        UUID id,
        UUID roomId,
        String email,
        String token,
        InvitationType type,
        InvitationStatus status,
        Instant expiresAt
) {
}
