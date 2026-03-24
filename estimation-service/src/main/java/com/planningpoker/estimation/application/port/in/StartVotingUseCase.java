package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Story;

import java.util.UUID;

/**
 * Primary port for starting a voting round on a story.
 */
public interface StartVotingUseCase {

    Story startVoting(UUID storyId, UUID requesterId);
}
