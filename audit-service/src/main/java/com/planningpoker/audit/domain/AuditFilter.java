package com.planningpoker.audit.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Filter criteria for querying audit entries.
 * All fields are nullable — null means "no filter on this dimension".
 */
public record AuditFilter(
        String entityType,
        AuditOperation operation,
        UUID userId,
        Instant from,
        Instant to
) {

    public static AuditFilter empty() {
        return new AuditFilter(null, null, null, null, null);
    }
}
