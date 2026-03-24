package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomPersistenceMapperTest {

    // ── toDomain ──────────────────────────────────────────────────────

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(RoomPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        RoomJpaEntity entity = createEntity();

        Room result = RoomPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getName()).isEqualTo(entity.getName());
        assertThat(result.getDescription()).isEqualTo(entity.getDescription());
        assertThat(result.getModeratorId()).isEqualTo(entity.getModeratorId());
        assertThat(result.getShortCode()).isEqualTo(entity.getShortCode());
        assertThat(result.getStatus()).isEqualTo(entity.getStatus());
        assertThat(result.getMaxParticipants()).isEqualTo(entity.getMaxParticipants());
        assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        assertThat(result.getDeckType()).isNotNull();
        assertThat(result.getParticipants()).hasSize(1);
    }

    @Test
    void toDomain_shouldHandleNullDeckType() {
        RoomJpaEntity entity = createEntity();
        entity.setDeckType(null);

        Room result = RoomPersistenceMapper.toDomain(entity);

        assertThat(result.getDeckType()).isNull();
    }

    // ── toEntity ──────────────────────────────────────────────────────

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertThat(RoomPersistenceMapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Room domain = createDomain();

        RoomJpaEntity result = RoomPersistenceMapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(domain.getId());
        assertThat(result.getName()).isEqualTo(domain.getName());
        assertThat(result.getDescription()).isEqualTo(domain.getDescription());
        assertThat(result.getModeratorId()).isEqualTo(domain.getModeratorId());
        assertThat(result.getShortCode()).isEqualTo(domain.getShortCode());
        assertThat(result.getStatus()).isEqualTo(domain.getStatus());
        assertThat(result.getMaxParticipants()).isEqualTo(domain.getMaxParticipants());
        assertThat(result.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(domain.getUpdatedAt());
        assertThat(result.getDeckType()).isNotNull();
        assertThat(result.getParticipants()).hasSize(1);
    }

    // ── round-trip ────────────────────────────────────────────────────

    @Test
    void roundTrip_shouldPreserveAllFields() {
        Room original = createDomain();

        RoomJpaEntity entity = RoomPersistenceMapper.toEntity(original);
        Room roundTripped = RoomPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getName()).isEqualTo(original.getName());
        assertThat(roundTripped.getDescription()).isEqualTo(original.getDescription());
        assertThat(roundTripped.getModeratorId()).isEqualTo(original.getModeratorId());
        assertThat(roundTripped.getShortCode()).isEqualTo(original.getShortCode());
        assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTripped.getMaxParticipants()).isEqualTo(original.getMaxParticipants());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTripped.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
        assertThat(roundTripped.getDeckType().getId()).isEqualTo(original.getDeckType().getId());
        assertThat(roundTripped.getParticipants()).hasSize(original.getParticipants().size());
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static RoomJpaEntity createEntity() {
        Instant now = Instant.now();

        DeckTypeJpaEntity deckType = new DeckTypeJpaEntity();
        deckType.setId(UUID.randomUUID());
        deckType.setName("Fibonacci");
        deckType.setCategory(DeckCategory.FIBONACCI);
        deckType.setSystem(true);
        deckType.setCreatedAt(now);
        deckType.setValues(new ArrayList<>());

        RoomJpaEntity entity = new RoomJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Sprint Planning");
        entity.setDescription("Weekly sprint planning");
        entity.setModeratorId(UUID.randomUUID());
        entity.setDeckType(deckType);
        entity.setShortCode("ABC12345");
        entity.setStatus(RoomStatus.ACTIVE);
        entity.setMaxParticipants(50);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        RoomParticipantJpaEntity participant = new RoomParticipantJpaEntity();
        participant.setId(UUID.randomUUID());
        participant.setRoom(entity);
        participant.setUserId(entity.getModeratorId());
        participant.setRole(ParticipantRole.MODERATOR);
        participant.setJoinedAt(now);
        participant.setConnected(true);

        entity.setParticipants(new ArrayList<>(List.of(participant)));

        return entity;
    }

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
