package com.planningpoker.estimation.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for creating a new story in a room.
 */
public record CreateStoryRequest(
        @NotNull UUID roomId,
        @NotBlank String title,
        String description
) {}
