package com.planningpoker.estimation.application.service;

import com.planningpoker.estimation.application.port.in.FinishVotingUseCase;
import com.planningpoker.estimation.application.port.in.GetVotesUseCase;
import com.planningpoker.estimation.application.port.in.StartVotingUseCase;
import com.planningpoker.estimation.application.port.in.SubmitVoteUseCase;
import com.planningpoker.estimation.application.port.out.ActiveRoomCachePort;
import com.planningpoker.estimation.application.port.out.EstimationEventPublisherPort;
import com.planningpoker.estimation.application.port.out.RoomValidationPort;
import com.planningpoker.estimation.application.port.out.StoryPersistencePort;
import com.planningpoker.estimation.application.port.out.VotePersistencePort;
import com.planningpoker.estimation.application.port.out.VotingNotificationPort;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.estimation.web.dto.SubmitVoteRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates voting-related use cases.
 * <p>
 * Core business logic:
 * <ul>
 *   <li>startVoting — verify moderator, transition story to VOTING, cache active story</li>
 *   <li>submitVote — verify story is VOTING, create/replace vote, notify via WebSocket</li>
 *   <li>finishVoting — verify moderator, transition story to VOTED, calculate results, notify</li>
 *   <li>getVotes — only if story status is VOTED</li>
 * </ul>
 */
@Service
@Transactional
public class VotingService implements StartVotingUseCase, FinishVotingUseCase,
        SubmitVoteUseCase, GetVotesUseCase {

    private final StoryPersistencePort storyPersistencePort;
    private final VotePersistencePort votePersistencePort;
    private final RoomValidationPort roomValidationPort;
    private final EstimationEventPublisherPort eventPublisherPort;
    private final VotingNotificationPort votingNotificationPort;
    private final ActiveRoomCachePort activeRoomCachePort;

    public VotingService(StoryPersistencePort storyPersistencePort,
                         VotePersistencePort votePersistencePort,
                         RoomValidationPort roomValidationPort,
                         EstimationEventPublisherPort eventPublisherPort,
                         VotingNotificationPort votingNotificationPort,
                         ActiveRoomCachePort activeRoomCachePort) {
        this.storyPersistencePort = storyPersistencePort;
        this.votePersistencePort = votePersistencePort;
        this.roomValidationPort = roomValidationPort;
        this.eventPublisherPort = eventPublisherPort;
        this.votingNotificationPort = votingNotificationPort;
        this.activeRoomCachePort = activeRoomCachePort;
    }

    // -- StartVotingUseCase -----------------------------------------------

    @Override
    public Story startVoting(UUID storyId, UUID requesterId) {
        Story story = storyPersistencePort.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story", storyId));

        if (!roomValidationPort.isModeratorOf(story.getRoomId(), requesterId)) {
            throw new BusinessException("NOT_MODERATOR",
                    "Only the room moderator can start voting");
        }

        story.startVoting();
        votePersistencePort.deleteByStoryId(storyId);

        Story saved = storyPersistencePort.save(story);
        activeRoomCachePort.cacheActiveStory(story.getRoomId(), storyId);

        // WebSocket first (synchronous/immediate), then Kafka (async)
        votingNotificationPort.notifyVotingStarted(saved.getRoomId(), saved.getId(), saved.getTitle());
        eventPublisherPort.publishVotingStarted(saved, requesterId);
        return saved;
    }

    // -- SubmitVoteUseCase ------------------------------------------------

    @Override
    public Vote submitVote(SubmitVoteRequest request, UUID storyId, UUID userId) {
        Story story = storyPersistencePort.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story", storyId));

        if (!story.isVotingOpen()) {
            throw new BusinessException("VOTING_NOT_OPEN",
                    "Voting is not open for this story");
        }

        // Use provided numericValue, or try to parse from the string value
        java.math.BigDecimal numericValue = request.numericValue();
        if (numericValue == null && request.value() != null) {
            try {
                numericValue = new java.math.BigDecimal(request.value());
            } catch (NumberFormatException ignored) {
                // Non-numeric value like "?", "coffee" — leave as null
            }
        }
        Vote vote = Vote.create(storyId, userId,
                request.value(), numericValue);

        // Replace existing vote from the same user if present
        votePersistencePort.findByStoryIdAndUserId(storyId, userId)
                .ifPresent(existing -> vote.setId(existing.getId()));

        Vote saved = votePersistencePort.save(vote);

        // Add vote to story for in-memory count
        story.addVote(saved);
        int voteCount = story.getVoteCount();

        votingNotificationPort.notifyVoteSubmitted(story.getRoomId(), storyId, voteCount);
        eventPublisherPort.publishVoteSubmitted(storyId, story.getRoomId(), voteCount);
        return saved;
    }

    // -- FinishVotingUseCase ----------------------------------------------

    @Override
    public VotingResult finishVoting(UUID storyId, UUID requesterId) {
        Story story = storyPersistencePort.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story", storyId));

        if (!roomValidationPort.isModeratorOf(story.getRoomId(), requesterId)) {
            throw new BusinessException("NOT_MODERATOR",
                    "Only the room moderator can finish voting");
        }

        // Load all persisted votes into the story for calculation
        List<Vote> votes = votePersistencePort.findByStoryId(storyId);
        story.setVotes(votes);

        story.finishVoting();

        Story saved = storyPersistencePort.save(story);

        VotingResult result = new VotingResult(
                saved.getId(),
                saved.getFinalScore(),
                saved.getVoteCount(),
                saved.isConsensusReached()
        );

        votingNotificationPort.notifyVotingResults(story.getRoomId(), result);
        votingNotificationPort.notifyStoryUpdated(story.getRoomId(), saved);
        activeRoomCachePort.evictActiveStory(story.getRoomId());
        eventPublisherPort.publishVotingFinished(storyId, story.getRoomId(), result);
        return result;
    }

    // -- GetVotesUseCase --------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Vote> getVotes(UUID storyId) {
        Story story = storyPersistencePort.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story", storyId));

        if (story.getStatus() != StoryStatus.VOTED) {
            throw new BusinessException("VOTING_NOT_FINISHED",
                    "Votes are only visible after voting has finished");
        }

        return votePersistencePort.findByStoryId(storyId);
    }
}
