package com.planningpoker.audit.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a single audit trail entry.
 * Audit entries are append-only — once created they are never modified or deleted.
 */
public class AuditEntry {

    private final Long id;
    private final String entityType;
    private final UUID entityId;
    private final AuditOperation operation;
    private final UUID userId;
    private final String sourceService;
    private final Instant timestamp;
    private final String previousState;
    private final String newState;
    private final String correlationId;
    private final String eventId;

    public AuditEntry(Long id,
                      String entityType,
                      UUID entityId,
                      AuditOperation operation,
                      UUID userId,
                      String sourceService,
                      Instant timestamp,
                      String previousState,
                      String newState,
                      String correlationId,
                      String eventId) {
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("entityType must not be blank");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("entityId must not be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("operation must not be null");
        }
        if (sourceService == null || sourceService.isBlank()) {
            throw new IllegalArgumentException("sourceService must not be blank");
        }

        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.userId = userId;
        this.sourceService = sourceService;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.previousState = previousState;
        this.newState = newState;
        this.correlationId = correlationId;
        this.eventId = eventId;
    }

    /**
     * Factory method for creating a new audit entry (no id yet).
     */
    public static AuditEntry create(String entityType,
                                     UUID entityId,
                                     AuditOperation operation,
                                     UUID userId,
                                     String sourceService,
                                     Instant timestamp,
                                     String previousState,
                                     String newState,
                                     String correlationId,
                                     String eventId) {
        return new AuditEntry(null, entityType, entityId, operation, userId,
                sourceService, timestamp, previousState, newState, correlationId, eventId);
    }

    public Long getId() { return id; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public AuditOperation getOperation() { return operation; }
    public UUID getUserId() { return userId; }
    public String getSourceService() { return sourceService; }
    public Instant getTimestamp() { return timestamp; }
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
    public String getCorrelationId() { return correlationId; }
    public String getEventId() { return eventId; }
}
