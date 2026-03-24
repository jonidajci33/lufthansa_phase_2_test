package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.InvitationStatus;
import com.planningpoker.room.domain.InvitationType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvitationPersistenceMapperTest {

    // ── toDomain ──────────────────────────────────────────────────────

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(InvitationPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        InvitationJpaEntity entity = createEntity();

        Invitation result = InvitationPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getRoomId()).isEqualTo(entity.getRoomId());
        assertThat(result.getInvitedBy()).isEqualTo(entity.getInvitedBy());
        assertThat(result.getEmail()).isEqualTo(entity.getEmail());
        assertThat(result.getToken()).isEqualTo(entity.getToken());
        assertThat(result.getType()).isEqualTo(entity.getType());
        assertThat(result.getStatus()).isEqualTo(entity.getStatus());
        assertThat(result.getExpiresAt()).isEqualTo(entity.getExpiresAt());
        assertThat(result.getAcceptedAt()).isEqualTo(entity.getAcceptedAt());
        assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }

    // ── toEntity ──────────────────────────────────────────────────────

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertThat(InvitationPersistenceMapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Invitation domain = createDomain();

        InvitationJpaEntity result = InvitationPersistenceMapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(domain.getId());
        assertThat(result.getRoomId()).isEqualTo(domain.getRoomId());
        assertThat(result.getInvitedBy()).isEqualTo(domain.getInvitedBy());
        assertThat(result.getEmail()).isEqualTo(domain.getEmail());
        assertThat(result.getToken()).isEqualTo(domain.getToken());
        assertThat(result.getType()).isEqualTo(domain.getType());
        assertThat(result.getStatus()).isEqualTo(domain.getStatus());
        assertThat(result.getExpiresAt()).isEqualTo(domain.getExpiresAt());
        assertThat(result.getAcceptedAt()).isEqualTo(domain.getAcceptedAt());
        assertThat(result.getCreatedAt()).isEqualTo(domain.getCreatedAt());
    }

    // ── round-trip ────────────────────────────────────────────────────

    @Test
    void roundTrip_shouldPreserveAllFields() {
        Invitation original = createDomain();

        InvitationJpaEntity entity = InvitationPersistenceMapper.toEntity(original);
        Invitation roundTripped = InvitationPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getRoomId()).isEqualTo(original.getRoomId());
        assertThat(roundTripped.getInvitedBy()).isEqualTo(original.getInvitedBy());
        assertThat(roundTripped.getEmail()).isEqualTo(original.getEmail());
        assertThat(roundTripped.getToken()).isEqualTo(original.getToken());
        assertThat(roundTripped.getType()).isEqualTo(original.getType());
        assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTripped.getExpiresAt()).isEqualTo(original.getExpiresAt());
        assertThat(roundTripped.getAcceptedAt()).isEqualTo(original.getAcceptedAt());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    // ── list variant ──────────────────────────────────────────────────

    @Test
    void toDomainList_shouldReturnEmptyList_whenNull() {
        assertThat(InvitationPersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toDomainList_shouldMapAllElements() {
        List<InvitationJpaEntity> entities = List.of(createEntity(), createEntity());

        List<Invitation> result = InvitationPersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static InvitationJpaEntity createEntity() {
        Instant now = Instant.now();
        InvitationJpaEntity entity = new InvitationJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setRoomId(UUID.randomUUID());
        entity.setInvitedBy(UUID.randomUUID());
        entity.setEmail("user@example.com");
        entity.setToken("invite-token-" + UUID.randomUUID());
        entity.setType(InvitationType.EMAIL);
        entity.setStatus(InvitationStatus.PENDING);
        entity.setExpiresAt(now.plusSeconds(86400));
        entity.setAcceptedAt(null);
        entity.setCreatedAt(now);
        return entity;
    }

    private static Invitation createDomain() {
        Instant now = Instant.now();
        return new Invitation(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "user@example.com", "invite-token-123",
                InvitationType.EMAIL, InvitationStatus.PENDING,
                now.plusSeconds(86400), null, now
        );
    }
}
