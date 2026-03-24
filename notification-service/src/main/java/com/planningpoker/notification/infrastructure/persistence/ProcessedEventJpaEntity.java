package com.planningpoker.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA entity for the {@code notification.processed_events} table.
 * Used for Kafka event idempotency — prevents duplicate processing.
 */
@Entity
@Table(name = "processed_events", schema = "notification")
public class ProcessedEventJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, length = 255)
    private String eventId;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    public ProcessedEventJpaEntity() {
    }

    public ProcessedEventJpaEntity(String eventId) {
        this.eventId = eventId;
    }

    @PrePersist
    void onPrePersist() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
