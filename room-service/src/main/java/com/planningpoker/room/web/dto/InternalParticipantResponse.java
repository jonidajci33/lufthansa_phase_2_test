package com.planningpoker.room.web.dto;

import com.planningpoker.room.domain.ParticipantRole;

import java.util.UUID;

/**
 * Lightweight participant response for service-to-service communication.
 */
public record InternalParticipantResponse(
        UUID userId,
        ParticipantRole role
) {
}
