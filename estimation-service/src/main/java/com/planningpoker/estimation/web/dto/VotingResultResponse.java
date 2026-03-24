package com.planningpoker.estimation.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response body for a completed voting round.
 */
public record VotingResultResponse(
        UUID storyId,
        BigDecimal averageScore,
        boolean consensusReached,
        int totalVotes
) {
}
