package com.planningpoker.audit.application.port.in;

import com.planningpoker.audit.domain.AuditEntry;

/**
 * Inbound port for recording audit entries from consumed events.
 */
public interface RecordAuditUseCase {

    /**
     * Records an audit entry. Deduplicates by eventId — if an entry with the same
     * eventId already exists, the call is silently ignored.
     *
     * @param entry the audit entry to record
     * @return the persisted entry, or null if deduplicated
     */
    AuditEntry record(AuditEntry entry);
}
