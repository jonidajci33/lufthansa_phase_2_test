package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.VotingResult;

import java.util.UUID;

/**
 * Primary port for finishing a voting round and calculating results.
 */
public interface FinishVotingUseCase {

    VotingResult finishVoting(UUID storyId, UUID requesterId);
}
