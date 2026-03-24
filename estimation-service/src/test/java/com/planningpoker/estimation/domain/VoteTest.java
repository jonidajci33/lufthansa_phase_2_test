package com.planningpoker.estimation.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoteTest {

    @Test
    void shouldCreateVoteWithFactory() {
        UUID storyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Vote vote = Vote.create(storyId, userId, "5", new BigDecimal("5"));

        assertThat(vote.getId()).isNotNull();
        assertThat(vote.getStoryId()).isEqualTo(storyId);
        assertThat(vote.getUserId()).isEqualTo(userId);
        assertThat(vote.getValue()).isEqualTo("5");
        assertThat(vote.getNumericValue()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(vote.isAnonymous()).isTrue();
        assertThat(vote.getCreatedAt()).isNotNull();
        assertThat(vote.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreateNonNumericVote() {
        UUID storyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Vote vote = Vote.create(storyId, userId, "?", null);

        assertThat(vote.getValue()).isEqualTo("?");
        assertThat(vote.getNumericValue()).isNull();
    }

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        Vote vote1 = new Vote(id, UUID.randomUUID(), UUID.randomUUID(), "5",
                new BigDecimal("5"), true, null, null);
        Vote vote2 = new Vote(id, UUID.randomUUID(), UUID.randomUUID(), "8",
                new BigDecimal("8"), false, null, null);

        assertThat(vote1).isEqualTo(vote2);
        assertThat(vote1.hashCode()).isEqualTo(vote2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Vote vote1 = Vote.create(UUID.randomUUID(), UUID.randomUUID(), "5", new BigDecimal("5"));
        Vote vote2 = Vote.create(UUID.randomUUID(), UUID.randomUUID(), "5", new BigDecimal("5"));

        assertThat(vote1).isNotEqualTo(vote2);
    }
}
