package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.Vote;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VotePersistenceMapperTest {

    // ── toDomain ─────────────────────────────────────────────────────

    @Test
    void toDomain_mapsAllFields() {
        StoryJpaEntity parent = storyEntity();
        VoteJpaEntity entity = voteEntity(parent);

        Vote result = VotePersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getStoryId()).isEqualTo(parent.getId());
        assertThat(result.getUserId()).isEqualTo(entity.getUserId());
        assertThat(result.getValue()).isEqualTo(entity.getValue());
        assertThat(result.getNumericValue()).isEqualByComparingTo(entity.getNumericValue());
        assertThat(result.isAnonymous()).isEqualTo(entity.isAnonymous());
        assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void toDomain_returnsNullForNullInput() {
        assertThat(VotePersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_handlesNullStoryGracefully() {
        VoteJpaEntity entity = new VoteJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setValue("5");
        entity.setNumericValue(new BigDecimal("5"));
        entity.setAnonymous(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        // story is null

        Vote result = VotePersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getStoryId()).isNull();
    }

    // ── toEntity ─────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        Vote vote = domainVote();
        StoryJpaEntity parent = storyEntity();

        VoteJpaEntity result = VotePersistenceMapper.toEntity(vote, parent);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(vote.getId());
        assertThat(result.getStory()).isSameAs(parent);
        assertThat(result.getUserId()).isEqualTo(vote.getUserId());
        assertThat(result.getValue()).isEqualTo(vote.getValue());
        assertThat(result.getNumericValue()).isEqualByComparingTo(vote.getNumericValue());
        assertThat(result.isAnonymous()).isEqualTo(vote.isAnonymous());
        assertThat(result.getCreatedAt()).isEqualTo(vote.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(vote.getUpdatedAt());
    }

    @Test
    void toEntity_returnsNullForNullInput() {
        assertThat(VotePersistenceMapper.toEntity(null, storyEntity())).isNull();
    }

    // ── Round-trip ───────────────────────────────────────────────────

    @Test
    void roundTrip_domainToEntityAndBack() {
        Vote original = domainVote();
        StoryJpaEntity parent = storyEntity();
        parent.setId(original.getStoryId());

        VoteJpaEntity entity = VotePersistenceMapper.toEntity(original, parent);
        Vote roundTripped = VotePersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getStoryId()).isEqualTo(original.getStoryId());
        assertThat(roundTripped.getUserId()).isEqualTo(original.getUserId());
        assertThat(roundTripped.getValue()).isEqualTo(original.getValue());
        assertThat(roundTripped.getNumericValue()).isEqualByComparingTo(original.getNumericValue());
        assertThat(roundTripped.isAnonymous()).isEqualTo(original.isAnonymous());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTripped.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }

    // ── List variants ────────────────────────────────────────────────

    @Test
    void toDomainList_mapsAllElements() {
        StoryJpaEntity parent = storyEntity();
        List<VoteJpaEntity> entities = List.of(voteEntity(parent), voteEntity(parent));

        List<Vote> result = VotePersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    @Test
    void toDomainList_returnsEmptyForNull() {
        assertThat(VotePersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toDomainList_returnsEmptyForEmptyList() {
        assertThat(VotePersistenceMapper.toDomainList(Collections.emptyList())).isEmpty();
    }

    @Test
    void toEntityList_mapsAllElements() {
        StoryJpaEntity parent = storyEntity();
        List<Vote> votes = List.of(domainVote(), domainVote());

        List<VoteJpaEntity> result = VotePersistenceMapper.toEntityList(votes, parent);

        assertThat(result).hasSize(2);
    }

    @Test
    void toEntityList_returnsEmptyForNull() {
        assertThat(VotePersistenceMapper.toEntityList(null, storyEntity())).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static StoryJpaEntity storyEntity() {
        StoryJpaEntity entity = new StoryJpaEntity();
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private static VoteJpaEntity voteEntity(StoryJpaEntity parent) {
        VoteJpaEntity entity = new VoteJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setStory(parent);
        entity.setUserId(UUID.randomUUID());
        entity.setValue("5");
        entity.setNumericValue(new BigDecimal("5"));
        entity.setAnonymous(true);
        entity.setCreatedAt(Instant.parse("2026-01-15T10:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-01-15T10:05:00Z"));
        return entity;
    }

    private static Vote domainVote() {
        Instant now = Instant.parse("2026-01-15T10:00:00Z");
        return new Vote(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "8",
                new BigDecimal("8"),
                true,
                now,
                now
        );
    }
}
