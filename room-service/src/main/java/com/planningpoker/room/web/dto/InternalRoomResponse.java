package com.planningpoker.room.web.dto;

import com.planningpoker.room.domain.RoomStatus;

import java.util.UUID;

/**
 * Lightweight room response for service-to-service communication.
 */
public record InternalRoomResponse(
        UUID id,
        String name,
        UUID moderatorId,
        RoomStatus status,
        DeckTypeResponse deckType
) {
}
