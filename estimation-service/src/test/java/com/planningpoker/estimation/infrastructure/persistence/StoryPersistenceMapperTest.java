package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.Vote;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StoryPersistenceMapperTest {

    // ── toDomain ─────────────────────────────────────────────────────

    @Test
    void toDomain_mapsAllFields() {
        StoryJpaEntity entity = storyEntity();

        Story result = StoryPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getRoomId()).isEqualTo(entity.getRoomId());
        assertThat(result.getTitle()).isEqualTo(entity.getTitle());
        assertThat(result.getDescription()).isEqualTo(entity.getDescription());
        assertThat(result.getStatus()).isEqualTo(entity.getStatus());
        assertThat(result.getSortOrder()).isEqualTo(entity.getSortOrder());
        assertThat(result.getFinalScore()).isEqualByComparingTo(entity.getFinalScore());
        assertThat(result.isConsensusReached()).isEqualTo(entity.isConsensusReached());
        assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void toDomain_mapsNestedVotes() {
        StoryJpaEntity entity = storyEntity();
        VoteJpaEntity vote1 = voteEntity(entity);
        VoteJpaEntity vote2 = voteEntity(entity);
        entity.setVotes(new ArrayList<>(List.of(vote1, vote2)));

        Story result = StoryPersistenceMapper.toDomain(entity);

        assertThat(result.getVotes()).hasSize(2);
    }

    @Test
    void toDomain_returnsNullForNullInput() {
        assertThat(StoryPersistenceMapper.toDomain(null)).isNull();
    }

    // ── toEntity ─────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        Story story = domainStory();

        StoryJpaEntity result = StoryPersistenceMapper.toEntity(story);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(story.getId());
        assertThat(result.getRoomId()).isEqualTo(story.getRoomId());
        assertThat(result.getTitle()).isEqualTo(story.getTitle());
        assertThat(result.getDescription()).isEqualTo(story.getDescription());
        assertThat(result.getStatus()).isEqualTo(story.getStatus());
        assertThat(result.getSortOrder()).isEqualTo(story.getSortOrder());
        assertThat(result.getFinalScore()).isEqualByComparingTo(story.getFinalScore());
        assertThat(result.isConsensusReached()).isEqualTo(story.isConsensusReached());
        assertThat(result.getCreatedAt()).isEqualTo(story.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(story.getUpdatedAt());
    }

    @Test
    void toEntity_mapsNestedVotes() {
        Vote vote = new Vote(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "5", new BigDecimal("5"), true, Instant.now(), Instant.now());
        Story story = new Story(UUID.randomUUID(), UUID.randomUUID(), "Title", "Desc",
                StoryStatus.VOTING, 0, null, false,
                List.of(vote), Instant.now(), Instant.now());

        StoryJpaEntity result = StoryPersistenceMapper.toEntity(story);

        assertThat(result.getVotes()).hasSize(1);
        assertThat(result.getVotes().get(0).getStory()).isSameAs(result);
    }

    @Test
    void toEntity_returnsNullForNullInput() {
        assertThat(StoryPersistenceMapper.toEntity(null)).isNull();
    }

    // ── Round-trip ───────────────────────────────────────────────────

    @Test
    void roundTrip_domainToEntityAndBack() {
        Story original = domainStory();

        StoryJpaEntity entity = StoryPersistenceMapper.toEntity(original);
        Story roundTripped = StoryPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getRoomId()).isEqualTo(original.getRoomId());
        assertThat(roundTripped.getTitle()).isEqualTo(original.getTitle());
        assertThat(roundTripped.getDescription()).isEqualTo(original.getDescription());
        assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTripped.getSortOrder()).isEqualTo(original.getSortOrder());
        assertThat(roundTripped.getFinalScore()).isEqualByComparingTo(original.getFinalScore());
        assertThat(roundTripped.isConsensusReached()).isEqualTo(original.isConsensusReached());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTripped.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }

    // ── List variants ────────────────────────────────────────────────

    @Test
    void toDomainList_mapsAllElements() {
        List<StoryJpaEntity> entities = List.of(storyEntity(), storyEntity());

        List<Story> result = StoryPersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    @Test
    void toDomainList_returnsEmptyForNull() {
        assertThat(StoryPersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toDomainList_returnsEmptyForEmptyList() {
        assertThat(StoryPersistenceMapper.toDomainList(Collections.emptyList())).isEmpty();
    }

    @Test
    void toEntityList_mapsAllElements() {
        List<Story> stories = List.of(domainStory(), domainStory());

        List<StoryJpaEntity> result = StoryPersistenceMapper.toEntityList(stories);

        assertThat(result).hasSize(2);
    }

    @Test
    void toEntityList_returnsEmptyForNull() {
        assertThat(StoryPersistenceMapper.toEntityList(null)).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static StoryJpaEntity storyEntity() {
        StoryJpaEntity entity = new StoryJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setRoomId(UUID.randomUUID());
        entity.setTitle("Login Page");
        entity.setDescription("Estimate the login page");
        entity.setStatus(StoryStatus.VOTED);
        entity.setSortOrder(1);
        entity.setFinalScore(new BigDecimal("5.00"));
        entity.setConsensusReached(true);
        entity.setVotes(new ArrayList<>());
        entity.setCreatedAt(Instant.parse("2026-01-15T10:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-01-15T11:00:00Z"));
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
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private static Story domainStory() {
        Instant now = Instant.parse("2026-01-15T10:00:00Z");
        return new Story(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Login Page",
                "Estimate the login page",
                StoryStatus.VOTED,
                1,
                new BigDecimal("5.00"),
                true,
                new ArrayList<>(),
                now,
                now
        );
    }
}
