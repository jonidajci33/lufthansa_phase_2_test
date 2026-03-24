package com.planningpoker.estimation.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Standard vote response returned from public API endpoints.
 */
public record VoteResponse(
        UUID id,
        UUID userId,
        String value,
        BigDecimal numericValue,
        Instant createdAt
) {
}
