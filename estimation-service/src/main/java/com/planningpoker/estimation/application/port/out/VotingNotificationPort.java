package com.planningpoker.estimation.application.port.out;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.VotingResult;

import java.util.UUID;

/**
 * Secondary (driven) port for sending real-time WebSocket notifications.
 */
public interface VotingNotificationPort {

    void notifyVotingStarted(UUID roomId, UUID storyId, String storyTitle);

    void notifyVoteSubmitted(UUID roomId, UUID storyId, int voteCount);

    void notifyVotingResults(UUID roomId, VotingResult result);

    void notifyStoryAdded(UUID roomId, Story story);

    void notifyStoryUpdated(UUID roomId, Story story);

    void notifyStoryDeleted(UUID roomId, UUID storyId);
}
