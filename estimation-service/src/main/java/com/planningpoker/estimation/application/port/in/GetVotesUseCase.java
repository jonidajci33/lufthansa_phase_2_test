package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Vote;

import java.util.List;
import java.util.UUID;

/**
 * Primary port for retrieving votes on a story.
 * Votes are only visible after the story's voting round is finished (REVEALED).
 */
public interface GetVotesUseCase {

    List<Vote> getVotes(UUID storyId);
}
