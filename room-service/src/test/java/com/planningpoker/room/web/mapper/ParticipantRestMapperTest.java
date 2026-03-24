package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.web.dto.InternalParticipantResponse;
import com.planningpoker.room.web.dto.ParticipantResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantRestMapperTest {

    // ── toResponse ────────────────────────────────────────────────────

    @Test
    void toResponse_shouldReturnNull_whenDomainIsNull() {
        assertThat(ParticipantRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        RoomParticipant domain = createDomain();

        ParticipantResponse result = ParticipantRestMapper.toResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(domain.getId());
        assertThat(result.userId()).isEqualTo(domain.getUserId());
        assertThat(result.role()).isEqualTo(domain.getRole());
        assertThat(result.joinedAt()).isEqualTo(domain.getJoinedAt());
        assertThat(result.isConnected()).isEqualTo(domain.isConnected());
    }

    @Test
    void toResponseList_shouldReturnEmptyList_whenNull() {
        assertThat(ParticipantRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_shouldMapAllElements() {
        List<RoomParticipant> domains = List.of(createDomain(), createDomain());

        List<ParticipantResponse> result = ParticipantRestMapper.toResponseList(domains);

        assertThat(result).hasSize(2);
    }

    // ── toInternalResponse ────────────────────────────────────────────

    @Test
    void toInternalResponse_shouldReturnNull_whenDomainIsNull() {
        assertThat(ParticipantRestMapper.toInternalResponse(null)).isNull();
    }

    @Test
    void toInternalResponse_shouldMapAllFields() {
        RoomParticipant domain = createDomain();

        InternalParticipantResponse result = ParticipantRestMapper.toInternalResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(domain.getUserId());
        assertThat(result.role()).isEqualTo(domain.getRole());
    }

    @Test
    void toInternalResponseList_shouldReturnEmptyList_whenNull() {
        assertThat(ParticipantRestMapper.toInternalResponseList(null)).isEmpty();
    }

    @Test
    void toInternalResponseList_shouldMapAllElements() {
        List<RoomParticipant> domains = List.of(createDomain(), createDomain());

        List<InternalParticipantResponse> result = ParticipantRestMapper.toInternalResponseList(domains);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static RoomParticipant createDomain() {
        return new RoomParticipant(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "testuser",
                ParticipantRole.PARTICIPANT, Instant.now(), null, true
        );
    }
}
