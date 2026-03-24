package com.planningpoker.audit.application.port.in;

import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for querying audit entries.
 */
public interface QueryAuditUseCase {

    Page<AuditEntry> list(AuditFilter filter, int offset, int limit);

    AuditEntry getById(Long id);

    List<AuditEntry> getEntityHistory(String entityType, UUID entityId);
}
