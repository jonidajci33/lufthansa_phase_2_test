package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomParticipantPersistenceMapperTest {

    // ── toDomain ──────────────────────────────────────────────────────

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(RoomParticipantPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        RoomJpaEntity parentRoom = createParentRoom();
        RoomParticipantJpaEntity entity = createEntity(parentRoom);

        RoomParticipant result = RoomParticipantPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getRoomId()).isEqualTo(parentRoom.getId());
        assertThat(result.getUserId()).isEqualTo(entity.getUserId());
        assertThat(result.getRole()).isEqualTo(entity.getRole());
        assertThat(result.getJoinedAt()).isEqualTo(entity.getJoinedAt());
        assertThat(result.getLeftAt()).isEqualTo(entity.getLeftAt());
        assertThat(result.isConnected()).isEqualTo(entity.isConnected());
    }

    // ── toEntity ──────────────────────────────────────────────────────

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertThat(RoomParticipantPersistenceMapper.toEntity(null, createParentRoom())).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        RoomJpaEntity parentRoom = createParentRoom();
        RoomParticipant domain = createDomain(parentRoom.getId());

        RoomParticipantJpaEntity result = RoomParticipantPersistenceMapper.toEntity(domain, parentRoom);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(domain.getId());
        assertThat(result.getRoom()).isSameAs(parentRoom);
        assertThat(result.getUserId()).isEqualTo(domain.getUserId());
        assertThat(result.getRole()).isEqualTo(domain.getRole());
        assertThat(result.getJoinedAt()).isEqualTo(domain.getJoinedAt());
        assertThat(result.getLeftAt()).isEqualTo(domain.getLeftAt());
        assertThat(result.isConnected()).isEqualTo(domain.isConnected());
    }

    // ── round-trip ────────────────────────────────────────────────────

    @Test
    void roundTrip_shouldPreserveAllFields() {
        RoomJpaEntity parentRoom = createParentRoom();
        RoomParticipant original = createDomain(parentRoom.getId());

        RoomParticipantJpaEntity entity = RoomParticipantPersistenceMapper.toEntity(original, parentRoom);
        RoomParticipant roundTripped = RoomParticipantPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getRoomId()).isEqualTo(original.getRoomId());
        assertThat(roundTripped.getUserId()).isEqualTo(original.getUserId());
        assertThat(roundTripped.getRole()).isEqualTo(original.getRole());
        assertThat(roundTripped.getJoinedAt()).isEqualTo(original.getJoinedAt());
        assertThat(roundTripped.getLeftAt()).isEqualTo(original.getLeftAt());
        assertThat(roundTripped.isConnected()).isEqualTo(original.isConnected());
    }

    // ── list variants ─────────────────────────────────────────────────

    @Test
    void toDomainList_shouldReturnEmptyList_whenNull() {
        assertThat(RoomParticipantPersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toEntityList_shouldReturnEmptyList_whenNull() {
        assertThat(RoomParticipantPersistenceMapper.toEntityList(null, createParentRoom())).isEmpty();
    }

    @Test
    void toDomainList_shouldMapAllElements() {
        RoomJpaEntity parentRoom = createParentRoom();
        List<RoomParticipantJpaEntity> entities = List.of(
                createEntity(parentRoom),
                createEntity(parentRoom)
        );

        List<RoomParticipant> result = RoomParticipantPersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static RoomJpaEntity createParentRoom() {
        RoomJpaEntity entity = new RoomJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Test Room");
        entity.setModeratorId(UUID.randomUUID());
        entity.setShortCode("ABC12345");
        entity.setStatus(RoomStatus.ACTIVE);
        entity.setMaxParticipants(50);
        entity.setParticipants(new ArrayList<>());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private static RoomParticipantJpaEntity createEntity(RoomJpaEntity parentRoom) {
        RoomParticipantJpaEntity entity = new RoomParticipantJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setRoom(parentRoom);
        entity.setUserId(UUID.randomUUID());
        entity.setUsername("testuser");
        entity.setRole(ParticipantRole.PARTICIPANT);
        entity.setJoinedAt(Instant.now());
        entity.setLeftAt(null);
        entity.setConnected(true);
        return entity;
    }

    private static RoomParticipant createDomain(UUID roomId) {
        return new RoomParticipant(
                UUID.randomUUID(), roomId, UUID.randomUUID(), "moderator",
                ParticipantRole.MODERATOR, Instant.now(), null, true
        );
    }
}
