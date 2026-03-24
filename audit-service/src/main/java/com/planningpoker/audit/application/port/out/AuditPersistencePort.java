package com.planningpoker.audit.application.port.out;

import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for audit entry persistence.
 */
public interface AuditPersistencePort {

    AuditEntry save(AuditEntry entry);

    Optional<AuditEntry> findById(Long id);

    Page<AuditEntry> findAll(AuditFilter filter, int offset, int limit);

    List<AuditEntry> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    long count(AuditFilter filter);

    boolean existsByEventId(String eventId);
}
