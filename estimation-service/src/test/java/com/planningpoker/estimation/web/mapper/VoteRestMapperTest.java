package com.planningpoker.estimation.web.mapper;

import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.web.dto.VoteResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoteRestMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        Vote vote = new Vote(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "8",
                new BigDecimal("8"),
                true,
                Instant.parse("2026-01-15T10:00:00Z"),
                Instant.parse("2026-01-15T10:05:00Z")
        );

        VoteResponse result = VoteRestMapper.toResponse(vote);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(vote.getId());
        assertThat(result.userId()).isEqualTo(vote.getUserId());
        assertThat(result.value()).isEqualTo("8");
        assertThat(result.numericValue()).isEqualByComparingTo(new BigDecimal("8"));
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2026-01-15T10:00:00Z"));
    }

    @Test
    void toResponse_returnsNullForNullInput() {
        assertThat(VoteRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_handlesNullNumericValue() {
        Vote vote = new Vote(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "?",
                null,
                true,
                Instant.now(),
                Instant.now()
        );

        VoteResponse result = VoteRestMapper.toResponse(vote);

        assertThat(result.numericValue()).isNull();
        assertThat(result.value()).isEqualTo("?");
    }

    @Test
    void toResponseList_mapsAllElements() {
        Vote vote1 = Vote.create(UUID.randomUUID(), UUID.randomUUID(), "3", new BigDecimal("3"));
        Vote vote2 = Vote.create(UUID.randomUUID(), UUID.randomUUID(), "5", new BigDecimal("5"));

        List<VoteResponse> result = VoteRestMapper.toResponseList(List.of(vote1, vote2));

        assertThat(result).hasSize(2);
    }

    @Test
    void toResponseList_returnsEmptyForNull() {
        assertThat(VoteRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_returnsEmptyForEmptyList() {
        assertThat(VoteRestMapper.toResponseList(Collections.emptyList())).isEmpty();
    }
}
