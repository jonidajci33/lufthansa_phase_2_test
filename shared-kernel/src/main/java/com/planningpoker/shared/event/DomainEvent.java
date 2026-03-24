package com.planningpoker.shared.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * CloudEvents v1.0-aligned event envelope for inter-service Kafka messaging.
 * All domain events across services use this envelope.
 */
public record DomainEvent<T>(

        @JsonProperty("specversion")
        String specVersion,

        @JsonProperty("id")
        String id,

        @JsonProperty("source")
        String source,

        @JsonProperty("type")
        String type,

        @JsonProperty("time")
        Instant time,

        @JsonProperty("datacontenttype")
        String dataContentType,

        @JsonProperty("correlationid")
        String correlationId,

        @JsonProperty("causationid")
        String causationId,

        @JsonProperty("data")
        T data
) {

    /**
     * Create a new domain event with auto-generated id and timestamp.
     */
    public static <T> DomainEvent<T> create(String source, String type, T data, String correlationId) {
        return new DomainEvent<>(
                "1.0",
                UUID.randomUUID().toString(),
                source,
                type,
                Instant.now(),
                "application/json",
                correlationId,
                null,
                data
        );
    }

    /**
     * Create a caused-by event (links to the event that triggered this one).
     */
    public static <T> DomainEvent<T> createCausedBy(String source, String type, T data,
                                                     String correlationId, String causationId) {
        return new DomainEvent<>(
                "1.0",
                UUID.randomUUID().toString(),
                source,
                type,
                Instant.now(),
                "application/json",
                correlationId,
                causationId,
                data
        );
    }
}
