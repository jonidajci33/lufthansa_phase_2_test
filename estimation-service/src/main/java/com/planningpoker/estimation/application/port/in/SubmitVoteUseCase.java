package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.web.dto.SubmitVoteRequest;

import java.util.UUID;

/**
 * Primary port for submitting a vote on a story.
 */
public interface SubmitVoteUseCase {

    Vote submitVote(SubmitVoteRequest request, UUID storyId, UUID userId);
}
