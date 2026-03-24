package com.planningpoker.audit.web.mapper;

import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.web.dto.AuditEntryResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from domain {@link AuditEntry} to {@link AuditEntryResponse} DTO.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class AuditEntryRestMapper {

    private AuditEntryRestMapper() {
        // utility class
    }

    /**
     * Converts a domain {@link AuditEntry} to an {@link AuditEntryResponse} DTO.
     *
     * @param entry the domain audit entry (may be null)
     * @return the response DTO, or {@code null} if the input is null
     */
    public static AuditEntryResponse toResponse(AuditEntry entry) {
        if (entry == null) {
            return null;
        }
        return new AuditEntryResponse(
                entry.getId(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getOperation(),
                entry.getUserId(),
                entry.getSourceService(),
                entry.getTimestamp(),
                entry.getPreviousState(),
                entry.getNewState(),
                entry.getCorrelationId(),
                entry.getEventId()
        );
    }

    /**
     * Converts a list of domain {@link AuditEntry} objects to {@link AuditEntryResponse} DTOs.
     *
     * @param entries the domain audit entries (may be null)
     * @return an unmodifiable list of response DTOs; empty list if input is null or empty
     */
    public static List<AuditEntryResponse> toResponseList(List<AuditEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        return entries.stream()
                .map(AuditEntryRestMapper::toResponse)
                .toList();
    }
}
