package com.planningpoker.estimation.web.mapper;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.web.dto.StoryResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StoryRestMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        Vote vote = new Vote(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "5", new BigDecimal("5"), true, Instant.now(), Instant.now());
        Story story = new Story(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Login Page",
                "Estimate the login page",
                StoryStatus.VOTED,
                2,
                new BigDecimal("5.00"),
                true,
                new ArrayList<>(List.of(vote)),
                Instant.parse("2026-01-15T10:00:00Z"),
                Instant.parse("2026-01-15T11:00:00Z")
        );

        StoryResponse result = StoryRestMapper.toResponse(story);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(story.getId());
        assertThat(result.roomId()).isEqualTo(story.getRoomId());
        assertThat(result.title()).isEqualTo("Login Page");
        assertThat(result.description()).isEqualTo("Estimate the login page");
        assertThat(result.status()).isEqualTo(StoryStatus.VOTED);
        assertThat(result.sortOrder()).isEqualTo(2);
        assertThat(result.finalScore()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.consensusReached()).isTrue();
        assertThat(result.voteCount()).isEqualTo(1);
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2026-01-15T10:00:00Z"));
    }

    @Test
    void toResponse_returnsNullForNullInput() {
        assertThat(StoryRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponseList_mapsAllElements() {
        Story story1 = Story.create(UUID.randomUUID(), "Story 1", "Desc 1", 0);
        Story story2 = Story.create(UUID.randomUUID(), "Story 2", "Desc 2", 1);

        List<StoryResponse> result = StoryRestMapper.toResponseList(List.of(story1, story2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Story 1");
        assertThat(result.get(1).title()).isEqualTo("Story 2");
    }

    @Test
    void toResponseList_returnsEmptyForNull() {
        assertThat(StoryRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_returnsEmptyForEmptyList() {
        assertThat(StoryRestMapper.toResponseList(Collections.emptyList())).isEmpty();
    }
}
