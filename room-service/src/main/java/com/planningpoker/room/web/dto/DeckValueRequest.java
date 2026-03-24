package com.planningpoker.room.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Request body for a single card value within a deck type.
 */
public record DeckValueRequest(
        @NotBlank String label,
        BigDecimal numericValue
) {}
