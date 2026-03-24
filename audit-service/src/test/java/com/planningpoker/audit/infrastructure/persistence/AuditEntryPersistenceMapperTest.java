package com.planningpoker.audit.infrastructure.persistence;

import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditOperation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEntryPersistenceMapperTest {

    // ── toDomain ─────────────────────────────────────────────────────

    @Test
    void toDomain_mapsAllFields() {
        AuditEntryJpaEntity entity = auditEntity();

        AuditEntry result = AuditEntryPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getEntityType()).isEqualTo(entity.getEntityType());
        assertThat(result.getEntityId()).isEqualTo(entity.getEntityId());
        assertThat(result.getOperation()).isEqualTo(entity.getOperation());
        assertThat(result.getUserId()).isEqualTo(entity.getUserId());
        assertThat(result.getSourceService()).isEqualTo(entity.getSourceService());
        assertThat(result.getTimestamp()).isEqualTo(entity.getTimestamp());
        assertThat(result.getPreviousState()).isEqualTo(entity.getPreviousState());
        assertThat(result.getNewState()).isEqualTo(entity.getNewState());
        assertThat(result.getCorrelationId()).isEqualTo(entity.getCorrelationId());
        assertThat(result.getEventId()).isEqualTo(entity.getEventId());
    }

    @Test
    void toDomain_returnsNullForNullInput() {
        assertThat(AuditEntryPersistenceMapper.toDomain(null)).isNull();
    }

    // ── toEntity ─────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        AuditEntry entry = domainAuditEntry();

        AuditEntryJpaEntity result = AuditEntryPersistenceMapper.toEntity(entry);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entry.getId());
        assertThat(result.getEntityType()).isEqualTo(entry.getEntityType());
        assertThat(result.getEntityId()).isEqualTo(entry.getEntityId());
        assertThat(result.getOperation()).isEqualTo(entry.getOperation());
        assertThat(result.getUserId()).isEqualTo(entry.getUserId());
        assertThat(result.getSourceService()).isEqualTo(entry.getSourceService());
        assertThat(result.getTimestamp()).isEqualTo(entry.getTimestamp());
        assertThat(result.getPreviousState()).isEqualTo(entry.getPreviousState());
        assertThat(result.getNewState()).isEqualTo(entry.getNewState());
        assertThat(result.getCorrelationId()).isEqualTo(entry.getCorrelationId());
        assertThat(result.getEventId()).isEqualTo(entry.getEventId());
    }

    @Test
    void toEntity_returnsNullForNullInput() {
        assertThat(AuditEntryPersistenceMapper.toEntity(null)).isNull();
    }

    // ── Round-trip ───────────────────────────────────────────────────

    @Test
    void roundTrip_domainToEntityAndBack() {
        AuditEntry original = domainAuditEntry();

        AuditEntryJpaEntity entity = AuditEntryPersistenceMapper.toEntity(original);
        AuditEntry roundTripped = AuditEntryPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getEntityType()).isEqualTo(original.getEntityType());
        assertThat(roundTripped.getEntityId()).isEqualTo(original.getEntityId());
        assertThat(roundTripped.getOperation()).isEqualTo(original.getOperation());
        assertThat(roundTripped.getUserId()).isEqualTo(original.getUserId());
        assertThat(roundTripped.getSourceService()).isEqualTo(original.getSourceService());
        assertThat(roundTripped.getTimestamp()).isEqualTo(original.getTimestamp());
        assertThat(roundTripped.getPreviousState()).isEqualTo(original.getPreviousState());
        assertThat(roundTripped.getNewState()).isEqualTo(original.getNewState());
        assertThat(roundTripped.getCorrelationId()).isEqualTo(original.getCorrelationId());
        assertThat(roundTripped.getEventId()).isEqualTo(original.getEventId());
    }

    // ── Round-trip with nullable fields ──────────────────────────────

    @Test
    void roundTrip_handlesNullOptionalFields() {
        AuditEntry original = new AuditEntry(
                42L, "Room", UUID.randomUUID(), AuditOperation.DELETED,
                null, "room-service", Instant.now(),
                null, null, null, null
        );

        AuditEntryJpaEntity entity = AuditEntryPersistenceMapper.toEntity(original);
        AuditEntry roundTripped = AuditEntryPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getUserId()).isNull();
        assertThat(roundTripped.getPreviousState()).isNull();
        assertThat(roundTripped.getNewState()).isNull();
        assertThat(roundTripped.getCorrelationId()).isNull();
        assertThat(roundTripped.getEventId()).isNull();
    }

    // ── List variants ────────────────────────────────────────────────

    @Test
    void toDomainList_mapsAllElements() {
        List<AuditEntryJpaEntity> entities = List.of(auditEntity(), auditEntity());

        List<AuditEntry> result = AuditEntryPersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    @Test
    void toDomainList_returnsEmptyForNull() {
        assertThat(AuditEntryPersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toDomainList_returnsEmptyForEmptyList() {
        assertThat(AuditEntryPersistenceMapper.toDomainList(Collections.emptyList())).isEmpty();
    }

    @Test
    void toEntityList_mapsAllElements() {
        List<AuditEntry> entries = List.of(domainAuditEntry(), domainAuditEntry());

        List<AuditEntryJpaEntity> result = AuditEntryPersistenceMapper.toEntityList(entries);

        assertThat(result).hasSize(2);
    }

    @Test
    void toEntityList_returnsEmptyForNull() {
        assertThat(AuditEntryPersistenceMapper.toEntityList(null)).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static AuditEntryJpaEntity auditEntity() {
        AuditEntryJpaEntity entity = new AuditEntryJpaEntity();
        entity.setId(42L);
        entity.setEntityType("Story");
        entity.setEntityId(UUID.randomUUID());
        entity.setOperation(AuditOperation.CREATED);
        entity.setUserId(UUID.randomUUID());
        entity.setSourceService("estimation-service");
        entity.setTimestamp(Instant.parse("2026-01-15T10:00:00Z"));
        entity.setPreviousState(null);
        entity.setNewState("{\"title\":\"Login Page\"}");
        entity.setCorrelationId("corr-123");
        entity.setEventId("evt-" + UUID.randomUUID());
        return entity;
    }

    private static AuditEntry domainAuditEntry() {
        return new AuditEntry(
                99L,
                "Room",
                UUID.randomUUID(),
                AuditOperation.UPDATED,
                UUID.randomUUID(),
                "room-service",
                Instant.parse("2026-01-15T12:00:00Z"),
                "{\"name\":\"Old\"}",
                "{\"name\":\"New\"}",
                "corr-456",
                "evt-" + UUID.randomUUID()
        );
    }
}
