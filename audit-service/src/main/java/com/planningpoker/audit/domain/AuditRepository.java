package com.planningpoker.audit.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for audit entries.
 */
public interface AuditRepository {

    AuditEntry save(AuditEntry entry);

    Optional<AuditEntry> findById(Long id);

    Page<AuditEntry> findAll(AuditFilter filter, int offset, int limit);

    List<AuditEntry> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    long count(AuditFilter filter);

    boolean existsByEventId(String eventId);
}
