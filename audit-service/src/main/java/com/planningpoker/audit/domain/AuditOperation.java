package com.planningpoker.audit.domain;

/**
 * The type of CUD operation that produced the audit entry.
 */
public enum AuditOperation {
    CREATED,
    UPDATED,
    DELETED
}
