package com.planningpoker.estimation.application.port.out;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.VotingResult;

import java.util.UUID;

/**
 * Secondary (driven) port for publishing estimation-related domain events.
 * The infrastructure adapter translates these calls into Kafka messages.
 */
public interface EstimationEventPublisherPort {

    void publishStoryCreated(Story story);

    void publishStoryUpdated(Story story);

    void publishStoryDeleted(UUID storyId, UUID roomId);

    void publishVotingStarted(Story story, UUID moderatorId);

    void publishVoteSubmitted(UUID storyId, UUID roomId, int voteCount);

    void publishVotingFinished(UUID storyId, UUID roomId, VotingResult result);
}
