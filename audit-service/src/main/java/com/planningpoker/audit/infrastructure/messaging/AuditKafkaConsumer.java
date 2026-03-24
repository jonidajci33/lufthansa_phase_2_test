package com.planningpoker.audit.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.audit.application.port.in.RecordAuditUseCase;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditOperation;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.Topics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka consumer that listens to ALL domain event topics and records
 * each event as an append-only audit trail entry.
 */
@Component
public class AuditKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditKafkaConsumer.class);

    private final RecordAuditUseCase recordAuditUseCase;
    private final ObjectMapper objectMapper;

    public AuditKafkaConsumer(RecordAuditUseCase recordAuditUseCase, ObjectMapper objectMapper) {
        this.recordAuditUseCase = recordAuditUseCase;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        log.info("[AuditKafkaConsumer] Registered — listening to topics: {}, {}, {}, {}",
                Topics.IDENTITY_USER_EVENTS, Topics.ROOM_EVENTS,
                Topics.ESTIMATION_STORY_EVENTS, Topics.ESTIMATION_VOTE_EVENTS);
    }

    @KafkaListener(
            topics = {
                    Topics.IDENTITY_USER_EVENTS,
                    Topics.ROOM_EVENTS,
                    Topics.ESTIMATION_STORY_EVENTS,
                    Topics.ESTIMATION_VOTE_EVENTS
            },
            groupId = "audit-service"
    )
    public void consume(ConsumerRecord<String, JsonNode> record, Acknowledgment ack) {
        log.info("[AuditKafkaConsumer] Received event from topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());
        try {
            JsonNode value = record.value();

            String eventType = extractString(value, "type");
            String eventId = extractString(value, "id");
            String source = extractString(value, "source");
            String correlationId = extractString(value, "correlationid");
            Instant time = extractInstant(value, "time");
            JsonNode data = value.get("data");

            String entityType = extractEntityType(eventType);
            AuditOperation operation = extractOperation(eventType);
            UUID entityId = extractEntityId(record.key());
            UUID userId = extractUserId(data);
            String newState = data != null ? data.toString() : null;

            AuditEntry entry = AuditEntry.create(
                    entityType,
                    entityId,
                    operation,
                    userId,
                    source != null ? source : record.topic(),
                    time,
                    null,
                    newState,
                    correlationId,
                    eventId
            );

            recordAuditUseCase.record(entry);
        } catch (Exception e) {
            log.error("Failed to process audit event from topic={}, offset={}: {}",
                    record.topic(), record.offset(), e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * Extracts entity type from event type string.
     * Format: "{domain}.{entity}.{action}.v1" -> "{entity}"
     * Example: "identity.user.registered.v1" -> "user"
     *          "room.room.created.v1" -> "room"
     *          "estimation.story.created.v1" -> "story"
     */
    String extractEntityType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return "unknown";
        }
        String[] parts = eventType.split("\\.");
        if (parts.length >= 3) {
            return parts[1];
        }
        return eventType;
    }

    /**
     * Maps the action part of the event type to an AuditOperation.
     * Looks for "created", "registered" -> CREATED; "updated" -> UPDATED;
     * "deleted", "deactivated" -> DELETED. Anything else defaults to UPDATED.
     */
    AuditOperation extractOperation(String eventType) {
        if (eventType == null) {
            return AuditOperation.CREATED;
        }
        String lower = eventType.toLowerCase();
        if (lower.contains("created") || lower.contains("registered")
                || lower.contains("started") || lower.contains("submitted")
                || lower.contains("joined") || lower.contains("invited")) {
            return AuditOperation.CREATED;
        }
        if (lower.contains("deleted") || lower.contains("deactivated")) {
            return AuditOperation.DELETED;
        }
        if (lower.contains("updated") || lower.contains("finished")) {
            return AuditOperation.UPDATED;
        }
        return AuditOperation.UPDATED;
    }

    private UUID extractEntityId(String key) {
        if (key == null || key.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID();
        }
    }

    private UUID extractUserId(JsonNode data) {
        if (data == null) return null;
        JsonNode userIdNode = data.get("userId");
        if (userIdNode == null) {
            userIdNode = data.get("user_id");
        }
        if (userIdNode == null) {
            userIdNode = data.get("id");
        }
        if (userIdNode != null && userIdNode.isTextual()) {
            try {
                return UUID.fromString(userIdNode.asText());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private String extractString(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child != null && child.isTextual()) ? child.asText() : null;
    }

    private Instant extractInstant(JsonNode node, String field) {
        JsonNode child = node.get(field);
        if (child != null && child.isTextual()) {
            try {
                return Instant.parse(child.asText());
            } catch (Exception e) {
                return Instant.now();
            }
        }
        return Instant.now();
    }
}
