package com.planningpoker.estimation.infrastructure.messaging;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.estimation.infrastructure.messaging.EstimationEventKafkaPublisher.StoryCreatedPayload;
import com.planningpoker.estimation.infrastructure.messaging.EstimationEventKafkaPublisher.StoryDeletedPayload;
import com.planningpoker.estimation.infrastructure.messaging.EstimationEventKafkaPublisher.StoryUpdatedPayload;
import com.planningpoker.estimation.infrastructure.messaging.EstimationEventKafkaPublisher.VoteSubmittedPayload;
import com.planningpoker.estimation.infrastructure.messaging.EstimationEventKafkaPublisher.VotingFinishedPayload;
import com.planningpoker.estimation.infrastructure.messaging.EstimationEventKafkaPublisher.VotingStartedPayload;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.EventTypes;
import com.planningpoker.shared.event.Topics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EstimationEventKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    @InjectMocks
    private EstimationEventKafkaPublisher publisher;

    @Captor
    private ArgumentCaptor<DomainEvent<?>> eventCaptor;

    // ── Helpers ───────────────────────────────────────────────────────

    private static final UUID ROOM_ID = UUID.fromString("aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID STORY_ID = UUID.fromString("bbbb2222-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID MODERATOR_ID = UUID.fromString("cccc3333-cccc-cccc-cccc-cccccccccccc");

    private static Story sampleStory(StoryStatus status) {
        Instant now = Instant.now();
        return new Story(STORY_ID, ROOM_ID, "Login Flow", "Estimate login",
                status, 1, new BigDecimal("5.00"), false,
                new ArrayList<>(), now, now);
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishStoryCreated
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishStoryCreatedEvent() {
        Story story = sampleStory(StoryStatus.PENDING);

        publisher.publishStoryCreated(story);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_STORY_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.STORY_CREATED);
        assertThat(event.source()).isEqualTo("estimation-service");
        assertThat(event.specVersion()).isEqualTo("1.0");
        assertThat(event.dataContentType()).isEqualTo("application/json");
        assertThat(event.id()).isNotBlank();
        assertThat(event.time()).isNotNull();

        assertThat(event.data()).isInstanceOf(StoryCreatedPayload.class);
        StoryCreatedPayload payload = (StoryCreatedPayload) event.data();
        assertThat(payload.storyId()).isEqualTo(STORY_ID);
        assertThat(payload.roomId()).isEqualTo(ROOM_ID);
        assertThat(payload.title()).isEqualTo("Login Flow");
        assertThat(payload.sortOrder()).isEqualTo(1);
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishStoryUpdated
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishStoryUpdatedEvent() {
        Story story = sampleStory(StoryStatus.VOTED);

        publisher.publishStoryUpdated(story);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_STORY_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.STORY_UPDATED);
        assertThat(event.source()).isEqualTo("estimation-service");

        assertThat(event.data()).isInstanceOf(StoryUpdatedPayload.class);
        StoryUpdatedPayload payload = (StoryUpdatedPayload) event.data();
        assertThat(payload.storyId()).isEqualTo(STORY_ID);
        assertThat(payload.roomId()).isEqualTo(ROOM_ID);
        assertThat(payload.title()).isEqualTo("Login Flow");
        assertThat(payload.description()).isEqualTo("Estimate login");
        assertThat(payload.status()).isEqualTo("VOTED");
        assertThat(payload.sortOrder()).isEqualTo(1);
        assertThat(payload.finalScore()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishStoryDeleted
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishStoryDeletedEvent() {
        publisher.publishStoryDeleted(STORY_ID, ROOM_ID);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_STORY_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.STORY_DELETED);
        assertThat(event.source()).isEqualTo("estimation-service");

        assertThat(event.data()).isInstanceOf(StoryDeletedPayload.class);
        StoryDeletedPayload payload = (StoryDeletedPayload) event.data();
        assertThat(payload.storyId()).isEqualTo(STORY_ID);
        assertThat(payload.roomId()).isEqualTo(ROOM_ID);
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishVotingStarted
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishVotingStartedEvent() {
        Story story = sampleStory(StoryStatus.VOTING);

        publisher.publishVotingStarted(story, MODERATOR_ID);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_VOTE_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.VOTING_STARTED);
        assertThat(event.source()).isEqualTo("estimation-service");

        assertThat(event.data()).isInstanceOf(VotingStartedPayload.class);
        VotingStartedPayload payload = (VotingStartedPayload) event.data();
        assertThat(payload.storyId()).isEqualTo(STORY_ID);
        assertThat(payload.roomId()).isEqualTo(ROOM_ID);
        assertThat(payload.storyTitle()).isEqualTo("Login Flow");
        assertThat(payload.moderatorId()).isEqualTo(MODERATOR_ID);
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishVoteSubmitted
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishVoteSubmittedEvent() {
        publisher.publishVoteSubmitted(STORY_ID, ROOM_ID, 5);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_VOTE_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.VOTE_SUBMITTED);
        assertThat(event.source()).isEqualTo("estimation-service");

        assertThat(event.data()).isInstanceOf(VoteSubmittedPayload.class);
        VoteSubmittedPayload payload = (VoteSubmittedPayload) event.data();
        assertThat(payload.storyId()).isEqualTo(STORY_ID);
        assertThat(payload.voteCount()).isEqualTo(5);
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishVotingFinished
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishVotingFinishedEvent() {
        VotingResult result = new VotingResult(STORY_ID, new BigDecimal("3.50"), 4, true);

        publisher.publishVotingFinished(STORY_ID, ROOM_ID, result);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_VOTE_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.VOTING_FINISHED);
        assertThat(event.source()).isEqualTo("estimation-service");

        assertThat(event.data()).isInstanceOf(VotingFinishedPayload.class);
        VotingFinishedPayload payload = (VotingFinishedPayload) event.data();
        assertThat(payload.storyId()).isEqualTo(STORY_ID);
        assertThat(payload.averageScore()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(payload.totalVotes()).isEqualTo(4);
        assertThat(payload.consensusReached()).isTrue();
    }

    @Test
    void shouldPublishVotingFinishedEvent_noConsensus() {
        VotingResult result = new VotingResult(STORY_ID, new BigDecimal("4.25"), 8, false);

        publisher.publishVotingFinished(STORY_ID, ROOM_ID, result);

        verify(kafkaTemplate).send(
                eq(Topics.ESTIMATION_VOTE_EVENTS),
                eq(ROOM_ID.toString()),
                eventCaptor.capture()
        );

        VotingFinishedPayload payload = (VotingFinishedPayload) eventCaptor.getValue().data();
        assertThat(payload.consensusReached()).isFalse();
        assertThat(payload.totalVotes()).isEqualTo(8);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Story events use ESTIMATION_STORY_EVENTS topic, vote events use ESTIMATION_VOTE_EVENTS
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void storyCreatedEvent_shouldUsStoryTopic() {
        publisher.publishStoryCreated(sampleStory(StoryStatus.PENDING));
        verify(kafkaTemplate).send(eq(Topics.ESTIMATION_STORY_EVENTS), eq(ROOM_ID.toString()), eventCaptor.capture());
    }

    @Test
    void votingStartedEvent_shouldUseVoteTopic() {
        publisher.publishVotingStarted(sampleStory(StoryStatus.VOTING), MODERATOR_ID);
        verify(kafkaTemplate).send(eq(Topics.ESTIMATION_VOTE_EVENTS), eq(ROOM_ID.toString()), eventCaptor.capture());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Payload record accessors (additional coverage)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCreateStoryCreatedPayloadWithAccessors() {
        UUID sid = UUID.randomUUID();
        UUID rid = UUID.randomUUID();
        StoryCreatedPayload payload = new StoryCreatedPayload(sid, rid, "Title", 3);

        assertThat(payload.storyId()).isEqualTo(sid);
        assertThat(payload.roomId()).isEqualTo(rid);
        assertThat(payload.title()).isEqualTo("Title");
        assertThat(payload.sortOrder()).isEqualTo(3);
    }

    @Test
    void shouldCreateStoryUpdatedPayloadWithAccessors() {
        UUID sid = UUID.randomUUID();
        UUID rid = UUID.randomUUID();
        StoryUpdatedPayload payload = new StoryUpdatedPayload(
                sid, rid, "Updated", "desc", "VOTED", 2, new BigDecimal("8.00"));

        assertThat(payload.storyId()).isEqualTo(sid);
        assertThat(payload.roomId()).isEqualTo(rid);
        assertThat(payload.title()).isEqualTo("Updated");
        assertThat(payload.description()).isEqualTo("desc");
        assertThat(payload.status()).isEqualTo("VOTED");
        assertThat(payload.sortOrder()).isEqualTo(2);
        assertThat(payload.finalScore()).isEqualByComparingTo(new BigDecimal("8.00"));
    }

    @Test
    void shouldCreateStoryDeletedPayloadWithAccessors() {
        UUID sid = UUID.randomUUID();
        UUID rid = UUID.randomUUID();
        StoryDeletedPayload payload = new StoryDeletedPayload(sid, rid);

        assertThat(payload.storyId()).isEqualTo(sid);
        assertThat(payload.roomId()).isEqualTo(rid);
    }

    @Test
    void shouldCreateVotingStartedPayloadWithAccessors() {
        UUID sid = UUID.randomUUID();
        UUID rid = UUID.randomUUID();
        UUID mid = UUID.randomUUID();
        VotingStartedPayload payload = new VotingStartedPayload(sid, rid, "Story", mid);

        assertThat(payload.storyId()).isEqualTo(sid);
        assertThat(payload.roomId()).isEqualTo(rid);
        assertThat(payload.storyTitle()).isEqualTo("Story");
        assertThat(payload.moderatorId()).isEqualTo(mid);
    }

    @Test
    void shouldCreateVoteSubmittedPayloadWithAccessors() {
        UUID sid = UUID.randomUUID();
        VoteSubmittedPayload payload = new VoteSubmittedPayload(sid, 7);

        assertThat(payload.storyId()).isEqualTo(sid);
        assertThat(payload.voteCount()).isEqualTo(7);
    }

    @Test
    void shouldCreateVotingFinishedPayloadWithAccessors() {
        UUID sid = UUID.randomUUID();
        VotingFinishedPayload payload = new VotingFinishedPayload(
                sid, new BigDecimal("2.50"), 3, true);

        assertThat(payload.storyId()).isEqualTo(sid);
        assertThat(payload.averageScore()).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(payload.totalVotes()).isEqualTo(3);
        assertThat(payload.consensusReached()).isTrue();
    }
}
