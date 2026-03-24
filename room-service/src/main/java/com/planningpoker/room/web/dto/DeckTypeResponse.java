package com.planningpoker.room.web.dto;

import com.planningpoker.room.domain.DeckCategory;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for deck type information.
 */
public record DeckTypeResponse(
        UUID id,
        String name,
        DeckCategory category,
        boolean isSystem,
        List<DeckValueResponse> values
) {
}
