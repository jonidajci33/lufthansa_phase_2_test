package com.planningpoker.estimation.infrastructure.config;

import com.planningpoker.estimation.application.port.out.VotingNotificationPort;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.VotingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket-based implementation of {@link VotingNotificationPort}.
 * Sends real-time voting notifications to room subscribers via STOMP topics.
 */
@Component
public class WebSocketNotifier implements VotingNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotifier.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyVotingStarted(UUID roomId, UUID storyId, String storyTitle) {
        String destination = "/topic/rooms/" + roomId + "/stories";
        var storyPayload = new VotingStartedStory(storyId, roomId, storyTitle, StoryStatus.VOTING.name());
        var innerPayload = new VotingStartedMessage("voting-started", storyPayload);
        var envelope = new WsEnvelope<>("DATA", Instant.now().toString(),
                UUID.randomUUID().toString(), innerPayload);
        messagingTemplate.convertAndSend(destination, envelope);
        log.debug("Sent voting-started notification to {} — storyId={}", destination, storyId);
    }

    @Override
    public void notifyVoteSubmitted(UUID roomId, UUID storyId, int voteCount) {
        String destination = "/topic/rooms/" + roomId + "/votes";
        var payload = new VoteNotification(storyId, voteCount);
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Sent vote notification to {} — storyId={}, voteCount={}", destination, storyId, voteCount);
    }

    @Override
    public void notifyVotingResults(UUID roomId, VotingResult result) {
        String destination = "/topic/rooms/" + roomId + "/results";
        messagingTemplate.convertAndSend(destination, result);
        log.debug("Sent voting results to {} — storyId={}, avg={}, consensus={}",
                destination, result.storyId(), result.averageScore(), result.consensusReached());
    }

    @Override
    public void notifyStoryAdded(UUID roomId, Story story) {
        String destination = "/topic/rooms/" + roomId + "/stories";
        var storyPayload = new StoryPayload(story.getId(), roomId, story.getTitle(),
                story.getDescription(), story.getStatus().name(), story.getSortOrder(),
                story.getFinalScore(), story.isConsensusReached(), story.getVoteCount());
        var innerPayload = new StoryUpdateMessage("added", storyPayload);
        var envelope = new WsEnvelope<>("DATA", Instant.now().toString(),
                UUID.randomUUID().toString(), innerPayload);
        messagingTemplate.convertAndSend(destination, envelope);
        log.debug("Sent story-added to {} — storyId={}", destination, story.getId());
    }

    @Override
    public void notifyStoryUpdated(UUID roomId, Story story) {
        String destination = "/topic/rooms/" + roomId + "/stories";
        var storyPayload = new StoryPayload(story.getId(), roomId, story.getTitle(),
                story.getDescription(), story.getStatus().name(), story.getSortOrder(),
                story.getFinalScore(), story.isConsensusReached(), story.getVoteCount());
        var innerPayload = new StoryUpdateMessage("updated", storyPayload);
        var envelope = new WsEnvelope<>("DATA", Instant.now().toString(),
                UUID.randomUUID().toString(), innerPayload);
        messagingTemplate.convertAndSend(destination, envelope);
        log.debug("Sent story-updated to {} — storyId={}", destination, story.getId());
    }

    @Override
    public void notifyStoryDeleted(UUID roomId, UUID storyId) {
        String destination = "/topic/rooms/" + roomId + "/stories";
        var deletePayload = new StoryDeletePayload(storyId);
        var innerPayload = new StoryUpdateMessage("deleted", deletePayload);
        var envelope = new WsEnvelope<>("DATA", Instant.now().toString(),
                UUID.randomUUID().toString(), innerPayload);
        messagingTemplate.convertAndSend(destination, envelope);
        log.debug("Sent story-deleted to {} — storyId={}", destination, storyId);
    }

    // ── Notification payload (anonymous — no actual vote values) ──────

    private record VoteNotification(
            UUID storyId,
            int voteCount
    ) {}

    // ── WsEnvelope and voting-started payloads ──────────────────────

    private record WsEnvelope<T>(
            String type,
            String timestamp,
            String correlationId,
            T payload
    ) {}

    private record VotingStartedMessage(
            String type,
            VotingStartedStory story
    ) {}

    private record VotingStartedStory(
            UUID id,
            UUID roomId,
            String title,
            String status
    ) {}

    // ── Story CRUD payloads ──────────────────────────────────────────

    private record StoryUpdateMessage(
            String type,
            Object story
    ) {}

    private record StoryPayload(
            UUID id,
            UUID roomId,
            String title,
            String description,
            String status,
            int sortOrder,
            java.math.BigDecimal finalScore,
            boolean consensusReached,
            int voteCount
    ) {}

    private record StoryDeletePayload(
            UUID storyId
    ) {}
}
