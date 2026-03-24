package com.planningpoker.audit.infrastructure.persistence;

import com.planningpoker.audit.domain.AuditEntry;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link AuditEntry} domain objects and {@link AuditEntryJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class AuditEntryPersistenceMapper {

    private AuditEntryPersistenceMapper() {
        // utility class
    }

    /**
     * Converts an {@link AuditEntryJpaEntity} to a domain {@link AuditEntry}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain audit entry, or {@code null} if the input is null
     */
    public static AuditEntry toDomain(AuditEntryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditEntry(
                entity.getId(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getOperation(),
                entity.getUserId(),
                entity.getSourceService(),
                entity.getTimestamp(),
                entity.getPreviousState(),
                entity.getNewState(),
                entity.getCorrelationId(),
                entity.getEventId()
        );
    }

    /**
     * Converts a domain {@link AuditEntry} to an {@link AuditEntryJpaEntity}.
     *
     * @param entry the domain audit entry (may be null)
     * @return the JPA entity, or {@code null} if the input is null
     */
    public static AuditEntryJpaEntity toEntity(AuditEntry entry) {
        if (entry == null) {
            return null;
        }
        AuditEntryJpaEntity entity = new AuditEntryJpaEntity();
        entity.setId(entry.getId());
        entity.setEntityType(entry.getEntityType());
        entity.setEntityId(entry.getEntityId());
        entity.setOperation(entry.getOperation());
        entity.setUserId(entry.getUserId());
        entity.setSourceService(entry.getSourceService());
        entity.setTimestamp(entry.getTimestamp());
        entity.setPreviousState(entry.getPreviousState());
        entity.setNewState(entry.getNewState());
        entity.setCorrelationId(entry.getCorrelationId());
        entity.setEventId(entry.getEventId());
        return entity;
    }

    /**
     * Converts a list of {@link AuditEntryJpaEntity} to domain {@link AuditEntry} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain audit entries; empty list if input is null or empty
     */
    public static List<AuditEntry> toDomainList(List<AuditEntryJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(AuditEntryPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a list of domain {@link AuditEntry} objects to {@link AuditEntryJpaEntity} instances.
     *
     * @param entries the domain audit entries (may be null)
     * @return an unmodifiable list of JPA entities; empty list if input is null or empty
     */
    public static List<AuditEntryJpaEntity> toEntityList(List<AuditEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        return entries.stream()
                .map(AuditEntryPersistenceMapper::toEntity)
                .toList();
    }
}
