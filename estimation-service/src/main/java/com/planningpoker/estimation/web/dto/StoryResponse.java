package com.planningpoker.estimation.web.dto;

import com.planningpoker.estimation.domain.StoryStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Standard story response returned from public API endpoints.
 */
public record StoryResponse(
        UUID id,
        UUID roomId,
        String title,
        String description,
        StoryStatus status,
        int sortOrder,
        BigDecimal finalScore,
        boolean consensusReached,
        int voteCount,
        Instant createdAt
) {
}
