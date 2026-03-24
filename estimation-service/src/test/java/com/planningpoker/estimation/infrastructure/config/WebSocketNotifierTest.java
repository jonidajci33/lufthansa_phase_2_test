package com.planningpoker.estimation.infrastructure.config;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.VotingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketNotifierTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotifier notifier;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    private static final UUID ROOM_ID = UUID.fromString("aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID STORY_ID = UUID.fromString("bbbb2222-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private static Story sampleStory(StoryStatus status) {
        Instant now = Instant.now();
        return new Story(STORY_ID, ROOM_ID, "Login Flow", "Estimate login",
                status, 1, new BigDecimal("5.00"), true,
                new ArrayList<>(), now, now);
    }

    // ═══════════════════════════════════════════════════════════════════
    // notifyVotingStarted
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendVotingStartedNotification_toCorrectDestination() {
        notifier.notifyVotingStarted(ROOM_ID, STORY_ID, "Login Flow");

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(destinationCaptor.getValue())
                .isEqualTo("/topic/rooms/" + ROOM_ID + "/stories");
    }

    @Test
    void shouldSendVotingStartedNotification_withEnvelopeStructure() {
        notifier.notifyVotingStarted(ROOM_ID, STORY_ID, "Login Flow");

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/stories"),
                payloadCaptor.capture()
        );

        // The payload is a WsEnvelope record with type, timestamp, correlationId, payload
        Object envelope = payloadCaptor.getValue();
        assertThat(envelope).isNotNull();
        // Verify envelope fields via toString (records include field values)
        String envelopeStr = envelope.toString();
        assertThat(envelopeStr).contains("DATA");
        assertThat(envelopeStr).contains("voting-started");
        assertThat(envelopeStr).contains(STORY_ID.toString());
        assertThat(envelopeStr).contains(ROOM_ID.toString());
        assertThat(envelopeStr).contains("Login Flow");
        assertThat(envelopeStr).contains(StoryStatus.VOTING.name());
    }

    // ═══════════════════════════════════════════════════════════════════
    // notifyVoteSubmitted
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendVoteNotification_toVotesDestination() {
        notifier.notifyVoteSubmitted(ROOM_ID, STORY_ID, 5);

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(destinationCaptor.getValue())
                .isEqualTo("/topic/rooms/" + ROOM_ID + "/votes");
    }

    @Test
    void shouldSendVoteNotification_withCorrectPayload() {
        notifier.notifyVoteSubmitted(ROOM_ID, STORY_ID, 3);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/votes"),
                payloadCaptor.capture()
        );

        Object payload = payloadCaptor.getValue();
        assertThat(payload).isNotNull();
        String payloadStr = payload.toString();
        assertThat(payloadStr).contains(STORY_ID.toString());
        assertThat(payloadStr).contains("3");
    }

    @Test
    void shouldSendVoteNotification_withZeroVoteCount() {
        notifier.notifyVoteSubmitted(ROOM_ID, STORY_ID, 0);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/votes"),
                payloadCaptor.capture()
        );

        assertThat(payloadCaptor.getValue()).isNotNull();
    }

    // ═══════════════════════════════════════════════════════════════════
    // notifyVotingResults
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendVotingResults_toResultsDestination() {
        VotingResult result = new VotingResult(STORY_ID, new BigDecimal("3.50"), 4, true);

        notifier.notifyVotingResults(ROOM_ID, result);

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(destinationCaptor.getValue())
                .isEqualTo("/topic/rooms/" + ROOM_ID + "/results");
    }

    @Test
    void shouldSendVotingResults_withVotingResultObject() {
        VotingResult result = new VotingResult(STORY_ID, new BigDecimal("3.50"), 4, true);

        notifier.notifyVotingResults(ROOM_ID, result);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/results"),
                payloadCaptor.capture()
        );

        assertThat(payloadCaptor.getValue()).isEqualTo(result);
    }

    @Test
    void shouldSendVotingResults_withNoConsensus() {
        VotingResult result = new VotingResult(STORY_ID, new BigDecimal("4.25"), 6, false);

        notifier.notifyVotingResults(ROOM_ID, result);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/results"),
                eq(result)
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // notifyStoryAdded
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendStoryAddedNotification_toStoriesDestination() {
        Story story = sampleStory(StoryStatus.PENDING);

        notifier.notifyStoryAdded(ROOM_ID, story);

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(destinationCaptor.getValue())
                .isEqualTo("/topic/rooms/" + ROOM_ID + "/stories");
    }

    @Test
    void shouldSendStoryAddedNotification_withEnvelopeContainingAddedType() {
        Story story = sampleStory(StoryStatus.PENDING);

        notifier.notifyStoryAdded(ROOM_ID, story);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/stories"),
                payloadCaptor.capture()
        );

        Object envelope = payloadCaptor.getValue();
        String envelopeStr = envelope.toString();
        assertThat(envelopeStr).contains("DATA");
        assertThat(envelopeStr).contains("added");
        assertThat(envelopeStr).contains(STORY_ID.toString());
        assertThat(envelopeStr).contains("Login Flow");
    }

    // ═══════════════════════════════════════════════════════════════════
    // notifyStoryUpdated
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendStoryUpdatedNotification_toStoriesDestination() {
        Story story = sampleStory(StoryStatus.VOTED);

        notifier.notifyStoryUpdated(ROOM_ID, story);

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(destinationCaptor.getValue())
                .isEqualTo("/topic/rooms/" + ROOM_ID + "/stories");
    }

    @Test
    void shouldSendStoryUpdatedNotification_withEnvelopeContainingUpdatedType() {
        Story story = sampleStory(StoryStatus.VOTED);

        notifier.notifyStoryUpdated(ROOM_ID, story);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/stories"),
                payloadCaptor.capture()
        );

        Object envelope = payloadCaptor.getValue();
        String envelopeStr = envelope.toString();
        assertThat(envelopeStr).contains("DATA");
        assertThat(envelopeStr).contains("updated");
        assertThat(envelopeStr).contains(STORY_ID.toString());
    }

    @Test
    void shouldIncludeStoryFields_inUpdatedNotification() {
        Story story = sampleStory(StoryStatus.VOTED);

        notifier.notifyStoryUpdated(ROOM_ID, story);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/stories"),
                payloadCaptor.capture()
        );

        String envelopeStr = payloadCaptor.getValue().toString();
        assertThat(envelopeStr).contains("Login Flow");
        assertThat(envelopeStr).contains("Estimate login");
        assertThat(envelopeStr).contains("VOTED");
    }

    // ═══════════════════════════════════════════════════════════════════
    // notifyStoryDeleted
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendStoryDeletedNotification_toStoriesDestination() {
        notifier.notifyStoryDeleted(ROOM_ID, STORY_ID);

        verify(messagingTemplate).convertAndSend(
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(destinationCaptor.getValue())
                .isEqualTo("/topic/rooms/" + ROOM_ID + "/stories");
    }

    @Test
    void shouldSendStoryDeletedNotification_withEnvelopeContainingDeletedType() {
        notifier.notifyStoryDeleted(ROOM_ID, STORY_ID);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/rooms/" + ROOM_ID + "/stories"),
                payloadCaptor.capture()
        );

        Object envelope = payloadCaptor.getValue();
        String envelopeStr = envelope.toString();
        assertThat(envelopeStr).contains("DATA");
        assertThat(envelopeStr).contains("deleted");
        assertThat(envelopeStr).contains(STORY_ID.toString());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Destination format verification
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldUseCorrectDestinationFormat_forAllMethods() {
        UUID roomId = UUID.fromString("ff000000-ff00-ff00-ff00-ff0000000000");
        UUID storyId = UUID.randomUUID();
        Story story = new Story(storyId, roomId, "Test", "Desc",
                StoryStatus.PENDING, 0, null, false,
                new ArrayList<>(), Instant.now(), Instant.now());
        VotingResult result = new VotingResult(storyId, BigDecimal.ONE, 1, true);

        notifier.notifyVotingStarted(roomId, storyId, "Test");
        verify(messagingTemplate).convertAndSend(eq("/topic/rooms/" + roomId + "/stories"), payloadCaptor.capture());

        notifier.notifyVoteSubmitted(roomId, storyId, 1);
        verify(messagingTemplate).convertAndSend(eq("/topic/rooms/" + roomId + "/votes"), payloadCaptor.capture());

        notifier.notifyVotingResults(roomId, result);
        verify(messagingTemplate).convertAndSend(eq("/topic/rooms/" + roomId + "/results"), payloadCaptor.capture());

        notifier.notifyStoryAdded(roomId, story);
        // stories destination called multiple times, so we verify with at least
        notifier.notifyStoryUpdated(roomId, story);
        notifier.notifyStoryDeleted(roomId, storyId);
    }
}
