package com.planningpoker.room.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for a single card value within a deck type.
 */
public record DeckValueResponse(
        UUID id,
        String label,
        BigDecimal numericValue,
        int sortOrder
) {
}
