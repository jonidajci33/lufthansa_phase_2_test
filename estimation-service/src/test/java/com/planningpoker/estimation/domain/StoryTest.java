package com.planningpoker.estimation.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoryTest {

    // ── Factory helpers ───────────────────────────────────────────────

    private static Story pendingStory() {
        return Story.create(UUID.randomUUID(), "Login Page", "Estimate the login page", 0);
    }

    private static Story votingStory() {
        Story story = pendingStory();
        story.startVoting();
        return story;
    }

    private static Vote numericVote(UUID userId, String value, BigDecimal numericValue) {
        return Vote.create(UUID.randomUUID(), userId, value, numericValue);
    }

    // ── Constructor / defaults ────────────────────────────────────────

    @Test
    void shouldCreateWithDefaultPendingStatus() {
        Story story = Story.create(UUID.randomUUID(), "Title", "Desc", 0);

        assertThat(story.getStatus()).isEqualTo(StoryStatus.PENDING);
        assertThat(story.getId()).isNotNull();
        assertThat(story.getTitle()).isEqualTo("Title");
        assertThat(story.getDescription()).isEqualTo("Desc");
        assertThat(story.getFinalScore()).isNull();
        assertThat(story.isConsensusReached()).isFalse();
        assertThat(story.getVotes()).isEmpty();
        assertThat(story.getCreatedAt()).isNotNull();
    }

    // ── startVoting() ─────────────────────────────────────────────────

    @Test
    void shouldStartVoting() {
        Story story = pendingStory();

        story.startVoting();

        assertThat(story.getStatus()).isEqualTo(StoryStatus.VOTING);
        assertThat(story.getVotes()).isEmpty();
    }

    @Test
    void shouldNotStartVotingWhenAlreadyVoting() {
        Story story = votingStory();

        assertThatThrownBy(story::startVoting)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VOTING");
    }

    @Test
    void shouldNotStartVotingWhenRevealed() {
        Story story = votingStory();
        UUID userId = UUID.randomUUID();
        story.addVote(numericVote(userId, "5", new BigDecimal("5")));
        story.finishVoting();

        assertThatThrownBy(story::startVoting)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VOTED");
    }

    // ── finishVoting() ────────────────────────────────────────────────

    @Test
    void shouldFinishVoting() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        story.addVote(numericVote(user1, "5", new BigDecimal("5")));
        story.addVote(numericVote(user2, "8", new BigDecimal("8")));

        story.finishVoting();

        assertThat(story.getStatus()).isEqualTo(StoryStatus.VOTED);
        assertThat(story.getFinalScore()).isNotNull();
        assertThat(story.getFinalScore()).isEqualByComparingTo(new BigDecimal("6.50"));
    }

    @Test
    void shouldNotFinishVotingWhenPending() {
        Story story = pendingStory();

        assertThatThrownBy(story::finishVoting)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    // ── calculateAverage() ────────────────────────────────────────────

    @Test
    void shouldCalculateAverageIgnoringNonNumeric() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();
        story.addVote(numericVote(user1, "5", new BigDecimal("5")));
        story.addVote(numericVote(user2, "8", new BigDecimal("8")));
        story.addVote(Vote.create(UUID.randomUUID(), user3, "?", null));

        BigDecimal average = story.calculateAverage();

        assertThat(average).isEqualByComparingTo(new BigDecimal("6.50"));
    }

    @Test
    void shouldCalculateAverageWithAllNumeric() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();
        story.addVote(numericVote(user1, "3", new BigDecimal("3")));
        story.addVote(numericVote(user2, "5", new BigDecimal("5")));
        story.addVote(numericVote(user3, "8", new BigDecimal("8")));

        BigDecimal average = story.calculateAverage();

        assertThat(average).isEqualByComparingTo(new BigDecimal("5.33"));
    }

    @Test
    void shouldHandleNoNumericVotes() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        story.addVote(Vote.create(UUID.randomUUID(), user1, "?", null));
        story.addVote(Vote.create(UUID.randomUUID(), user2, "coffee", null));

        BigDecimal average = story.calculateAverage();

        assertThat(average).isNull();
    }

    // ── consensus ─────────────────────────────────────────────────────

    @Test
    void shouldDetectConsensus() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();
        story.addVote(numericVote(user1, "5", new BigDecimal("5")));
        story.addVote(numericVote(user2, "5", new BigDecimal("5")));
        story.addVote(numericVote(user3, "5", new BigDecimal("5")));

        story.finishVoting();

        assertThat(story.isConsensusReached()).isTrue();
    }

    @Test
    void shouldDetectNoConsensus() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        story.addVote(numericVote(user1, "3", new BigDecimal("3")));
        story.addVote(numericVote(user2, "8", new BigDecimal("8")));

        story.finishVoting();

        assertThat(story.isConsensusReached()).isFalse();
    }

    // ── addVote() ─────────────────────────────────────────────────────

    @Test
    void shouldAddVote() {
        Story story = votingStory();
        UUID userId = UUID.randomUUID();

        story.addVote(numericVote(userId, "5", new BigDecimal("5")));

        assertThat(story.getVotes()).hasSize(1);
        assertThat(story.getVotes().get(0).getValue()).isEqualTo("5");
    }

    @Test
    void shouldReplaceVoteFromSameUser() {
        Story story = votingStory();
        UUID userId = UUID.randomUUID();

        story.addVote(numericVote(userId, "5", new BigDecimal("5")));
        story.addVote(numericVote(userId, "8", new BigDecimal("8")));

        assertThat(story.getVotes()).hasSize(1);
        assertThat(story.getVotes().get(0).getValue()).isEqualTo("8");
    }

    @Test
    void shouldNotAddVoteWhenNotVoting() {
        Story story = pendingStory();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> story.addVote(numericVote(userId, "5", new BigDecimal("5"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    // ── update() ──────────────────────────────────────────────────────

    @Test
    void shouldUpdateOnlyWhenPending() {
        Story story = pendingStory();

        story.update("New Title", "New Description");

        assertThat(story.getTitle()).isEqualTo("New Title");
        assertThat(story.getDescription()).isEqualTo("New Description");
    }

    @Test
    void shouldNotUpdateWhenVoting() {
        Story story = votingStory();

        assertThatThrownBy(() -> story.update("New", "Desc"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VOTING");
    }

    // ── getVoteCount() ────────────────────────────────────────────────

    @Test
    void shouldGetVoteCount() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        story.addVote(numericVote(user1, "3", new BigDecimal("3")));
        story.addVote(numericVote(user2, "5", new BigDecimal("5")));

        assertThat(story.getVoteCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroVoteCountWhenEmpty() {
        Story story = pendingStory();

        assertThat(story.getVoteCount()).isZero();
    }

    // ── equals / hashCode ─────────────────────────────────────────────

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Story story1 = new Story(id, UUID.randomUUID(), "Title1", null,
                StoryStatus.PENDING, 0, null, false, null, now, now);
        Story story2 = new Story(id, UUID.randomUUID(), "Title2", null,
                StoryStatus.VOTING, 1, null, false, null, now, now);

        assertThat(story1).isEqualTo(story2);
        assertThat(story1.hashCode()).isEqualTo(story2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Story story1 = pendingStory();
        Story story2 = pendingStory();

        assertThat(story1).isNotEqualTo(story2);
    }
}
