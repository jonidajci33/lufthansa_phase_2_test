package com.planningpoker.estimation.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value object representing the result of a completed voting round.
 */
public record VotingResult(
        UUID storyId,
        BigDecimal averageScore,
        int totalVotes,
        boolean consensusReached
) {
}
