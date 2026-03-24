package com.planningpoker.room.web.dto;

import com.planningpoker.room.domain.ParticipantRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for room participant information.
 */
public record ParticipantResponse(
        UUID id,
        UUID userId,
        String username,
        ParticipantRole role,
        Instant joinedAt,
        boolean isConnected
) {
}
