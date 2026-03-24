package com.planningpoker.room.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for creating a new room.
 */
public record CreateRoomRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID deckTypeId,
        Integer maxParticipants
) {}
