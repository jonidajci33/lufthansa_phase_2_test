package com.planningpoker.room.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for creating a custom deck type.
 */
public record CreateDeckTypeRequest(
        @NotBlank String name,
        @NotEmpty List<DeckValueRequest> values
) {}
