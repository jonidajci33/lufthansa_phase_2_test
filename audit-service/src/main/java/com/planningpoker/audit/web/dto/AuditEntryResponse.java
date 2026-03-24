package com.planningpoker.audit.web.dto;

import com.planningpoker.audit.domain.AuditOperation;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a single audit entry.
 */
public record AuditEntryResponse(
        Long id,
        String entityType,
        UUID entityId,
        AuditOperation operation,
        UUID userId,
        String sourceService,
        Instant timestamp,
        String previousState,
        String newState,
        String correlationId,
        String eventId
) {
}
