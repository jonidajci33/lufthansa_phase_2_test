package com.planningpoker.room.web.dto;

import com.planningpoker.room.domain.RoomStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard room response returned from public API endpoints.
 */
public record RoomResponse(
        UUID id,
        String name,
        String description,
        UUID moderatorId,
        DeckTypeResponse deckType,
        String shortCode,
        RoomStatus status,
        int maxParticipants,
        int participantCount,
        Instant createdAt
) {
}
