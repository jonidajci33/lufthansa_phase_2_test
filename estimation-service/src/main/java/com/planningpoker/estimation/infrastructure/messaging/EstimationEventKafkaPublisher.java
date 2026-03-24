package com.planningpoker.estimation.infrastructure.messaging;

import com.planningpoker.estimation.application.port.out.EstimationEventPublisherPort;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.EventTypes;
import com.planningpoker.shared.event.Topics;
import com.planningpoker.shared.observability.CorrelationIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka-based implementation of {@link EstimationEventPublisherPort}.
 * Publishes domain events to {@code estimation.story.events} and {@code estimation.vote.events} topics.
 */
@Component
public class EstimationEventKafkaPublisher implements EstimationEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(EstimationEventKafkaPublisher.class);
    private static final String SOURCE = "estimation-service";

    private final KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    public EstimationEventKafkaPublisher(KafkaTemplate<String, DomainEvent<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishStoryCreated(Story story) {
        var payload = new StoryCreatedPayload(
                story.getId(),
                story.getRoomId(),
                story.getTitle(),
                story.getSortOrder()
        );

        DomainEvent<StoryCreatedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.STORY_CREATED,
                payload,
                CorrelationIdUtil.current()
        );

        sendStoryEvent(story.getRoomId().toString(), event);
        log.info("Published STORY_CREATED event for story={} in room={}", story.getId(), story.getRoomId());
    }

    @Override
    public void publishStoryUpdated(Story story) {
        var payload = new StoryUpdatedPayload(
                story.getId(),
                story.getRoomId(),
                story.getTitle(),
                story.getDescription(),
                story.getStatus().name(),
                story.getSortOrder(),
                story.getFinalScore()
        );

        DomainEvent<StoryUpdatedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.STORY_UPDATED,
                payload,
                CorrelationIdUtil.current()
        );

        sendStoryEvent(story.getRoomId().toString(), event);
        log.info("Published STORY_UPDATED event for story={} in room={}", story.getId(), story.getRoomId());
    }

    @Override
    public void publishStoryDeleted(UUID storyId, UUID roomId) {
        var payload = new StoryDeletedPayload(storyId, roomId);

        DomainEvent<StoryDeletedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.STORY_DELETED,
                payload,
                CorrelationIdUtil.current()
        );

        sendStoryEvent(roomId.toString(), event);
        log.info("Published STORY_DELETED event for story={} in room={}", storyId, roomId);
    }

    @Override
    public void publishVotingStarted(Story story, UUID moderatorId) {
        var payload = new VotingStartedPayload(
                story.getId(),
                story.getRoomId(),
                story.getTitle(),
                moderatorId
        );

        DomainEvent<VotingStartedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.VOTING_STARTED,
                payload,
                CorrelationIdUtil.current()
        );

        sendVoteEvent(story.getRoomId().toString(), event);
        log.info("Published VOTING_STARTED event for story={} in room={}", story.getId(), story.getRoomId());
    }

    @Override
    public void publishVoteSubmitted(UUID storyId, UUID roomId, int voteCount) {
        var payload = new VoteSubmittedPayload(storyId, voteCount);

        DomainEvent<VoteSubmittedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.VOTE_SUBMITTED,
                payload,
                CorrelationIdUtil.current()
        );

        sendVoteEvent(roomId.toString(), event);
        log.info("Published VOTE_SUBMITTED event for story={} (voteCount={})", storyId, voteCount);
    }

    @Override
    public void publishVotingFinished(UUID storyId, UUID roomId, VotingResult result) {
        var payload = new VotingFinishedPayload(
                storyId,
                result.averageScore(),
                result.totalVotes(),
                result.consensusReached()
        );

        DomainEvent<VotingFinishedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.VOTING_FINISHED,
                payload,
                CorrelationIdUtil.current()
        );

        sendVoteEvent(roomId.toString(), event);
        log.info("Published VOTING_FINISHED event for story={} (avg={}, consensus={})",
                storyId, result.averageScore(), result.consensusReached());
    }

    private void sendStoryEvent(String key, DomainEvent<?> event) {
        kafkaTemplate.send(Topics.ESTIMATION_STORY_EVENTS, key, event);
    }

    private void sendVoteEvent(String key, DomainEvent<?> event) {
        kafkaTemplate.send(Topics.ESTIMATION_VOTE_EVENTS, key, event);
    }

    // ── Event payload DTOs ────────────────────────────────────────────

    public record StoryCreatedPayload(
            UUID storyId,
            UUID roomId,
            String title,
            int sortOrder
    ) {}

    public record StoryUpdatedPayload(
            UUID storyId,
            UUID roomId,
            String title,
            String description,
            String status,
            int sortOrder,
            BigDecimal finalScore
    ) {}

    public record StoryDeletedPayload(
            UUID storyId,
            UUID roomId
    ) {}

    public record VotingStartedPayload(
            UUID storyId,
            UUID roomId,
            String storyTitle,
            UUID moderatorId
    ) {}

    public record VoteSubmittedPayload(
            UUID storyId,
            int voteCount
    ) {}

    public record VotingFinishedPayload(
            UUID storyId,
            BigDecimal averageScore,
            int totalVotes,
            boolean consensusReached
    ) {}
}
