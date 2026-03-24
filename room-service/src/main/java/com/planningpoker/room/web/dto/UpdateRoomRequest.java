package com.planningpoker.room.web.dto;

/**
 * Request body for updating a room's editable fields.
 */
public record UpdateRoomRequest(
        String name,
        String description,
        Integer maxParticipants
) {}
