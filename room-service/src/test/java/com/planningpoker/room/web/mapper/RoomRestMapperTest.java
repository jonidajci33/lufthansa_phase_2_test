package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import com.planningpoker.room.web.dto.InternalRoomResponse;
import com.planningpoker.room.web.dto.RoomResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomRestMapperTest {

    // ── toResponse ────────────────────────────────────────────────────

    @Test
    void toResponse_shouldReturnNull_whenDomainIsNull() {
        assertThat(RoomRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        Room domain = createDomain();

        RoomResponse result = RoomRestMapper.toResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(domain.getId());
        assertThat(result.name()).isEqualTo(domain.getName());
        assertThat(result.description()).isEqualTo(domain.getDescription());
        assertThat(result.moderatorId()).isEqualTo(domain.getModeratorId());
        assertThat(result.shortCode()).isEqualTo(domain.getShortCode());
        assertThat(result.status()).isEqualTo(domain.getStatus());
        assertThat(result.maxParticipants()).isEqualTo(domain.getMaxParticipants());
        assertThat(result.participantCount()).isEqualTo(domain.getParticipants().size());
        assertThat(result.createdAt()).isEqualTo(domain.getCreatedAt());
        assertThat(result.deckType()).isNotNull();
        assertThat(result.deckType().id()).isEqualTo(domain.getDeckType().getId());
    }

    @Test
    void toResponse_shouldHandleNullDeckType() {
        Room domain = new Room(
                UUID.randomUUID(), "Room", null, UUID.randomUUID(),
                null, "CODE1234", RoomStatus.ACTIVE, 50,
                new ArrayList<>(), Instant.now(), Instant.now()
        );

        RoomResponse result = RoomRestMapper.toResponse(domain);

        assertThat(result.deckType()).isNull();
        assertThat(result.participantCount()).isZero();
    }

    @Test
    void toResponseList_shouldReturnEmptyList_whenNull() {
        assertThat(RoomRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_shouldMapAllElements() {
        List<Room> domains = List.of(createDomain(), createDomain());

        List<RoomResponse> result = RoomRestMapper.toResponseList(domains);

        assertThat(result).hasSize(2);
    }

    // ── toInternalResponse ────────────────────────────────────────────

    @Test
    void toInternalResponse_shouldReturnNull_whenDomainIsNull() {
        assertThat(RoomRestMapper.toInternalResponse(null)).isNull();
    }

    @Test
    void toInternalResponse_shouldMapAllFields() {
        Room domain = createDomain();

        InternalRoomResponse result = RoomRestMapper.toInternalResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(domain.getId());
        assertThat(result.name()).isEqualTo(domain.getName());
        assertThat(result.moderatorId()).isEqualTo(domain.getModeratorId());
        assertThat(result.status()).isEqualTo(domain.getStatus());
        assertThat(result.deckType()).isNotNull();
        assertThat(result.deckType().id()).isEqualTo(domain.getDeckType().getId());
    }

    @Test
    void toInternalResponse_shouldHandleNullDeckType() {
        Room domain = new Room(
                UUID.randomUUID(), "Room", null, UUID.randomUUID(),
                null, "CODE1234", RoomStatus.ACTIVE, 50,
                new ArrayList<>(), Instant.now(), Instant.now()
        );

        InternalRoomResponse result = RoomRestMapper.toInternalResponse(domain);

        assertThat(result.deckType()).isNull();
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static Room createDomain() {
        Instant now = Instant.now();
        UUID roomId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        DeckValue value = new DeckValue(UUID.randomUUID(), "5", new BigDecimal("5"), 3);
        DeckType deckType = new DeckType(
                UUID.randomUUID(), "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(value), now
        );

        RoomParticipant moderator = new RoomParticipant(
                UUID.randomUUID(), roomId, moderatorId, "moderator",
                ParticipantRole.MODERATOR, now, null, true
        );

        return new Room(
                roomId, "Sprint Planning", "Weekly sprint planning",
                moderatorId, deckType, "ABC12345", RoomStatus.ACTIVE,
                50, new ArrayList<>(List.of(moderator)), now, now
        );
    }
}
