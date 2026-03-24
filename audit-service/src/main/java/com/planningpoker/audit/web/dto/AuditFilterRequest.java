package com.planningpoker.audit.web.dto;

import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.AuditOperation;

import java.time.Instant;
import java.util.UUID;

/**
 * Query parameter DTO for filtering audit entries.
 */
public record AuditFilterRequest(
        String entityType,
        String operation,
        UUID userId,
        Instant from,
        Instant to
) {

    public AuditFilter toDomain() {
        AuditOperation op = null;
        if (operation != null && !operation.isBlank()) {
            op = AuditOperation.valueOf(operation.toUpperCase());
        }
        return new AuditFilter(entityType, op, userId, from, to);
    }
}
