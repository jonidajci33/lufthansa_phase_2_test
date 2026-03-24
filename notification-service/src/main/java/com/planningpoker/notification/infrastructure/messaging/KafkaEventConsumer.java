package com.planningpoker.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.planningpoker.notification.application.service.NotificationEventHandler;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.Topics;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Kafka consumer that listens to domain event topics and routes them
 * to the {@link NotificationEventHandler} for processing.
 * <p>
 * Deserializes to raw {@link JsonNode} and manually extracts CloudEvents fields,
 * matching the pattern used by the audit-service consumer.
 */
@Component
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final NotificationEventHandler eventHandler;

    public KafkaEventConsumer(NotificationEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @PostConstruct
    void init() {
        log.info("[KafkaEventConsumer] Registered — listening to topics: {}, {}, {}",
                Topics.IDENTITY_USER_EVENTS, Topics.ROOM_EVENTS, Topics.ESTIMATION_VOTE_EVENTS);
    }

    @KafkaListener(
            topics = {
                    Topics.IDENTITY_USER_EVENTS,
                    Topics.ROOM_EVENTS,
                    Topics.ESTIMATION_VOTE_EVENTS
            },
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, JsonNode> record, Acknowledgment ack) {
        try {
            JsonNode value = record.value();
            if (value == null) {
                log.warn("[KafkaEventConsumer] Null message from topic={}, offset={}", record.topic(), record.offset());
                ack.acknowledge();
                return;
            }

            String eventType = extractString(value, "type");
            String eventId = extractString(value, "id");
            String source = extractString(value, "source");
            String correlationId = extractString(value, "correlationid");
            Instant time = extractInstant(value, "time");
            JsonNode data = value.get("data");

            log.info("[KafkaEventConsumer] Received event topic={}, type={}, id={}",
                    record.topic(), eventType, eventId);

            DomainEvent<JsonNode> event = new DomainEvent<>(
                    "1.0",
                    eventId,
                    source,
                    eventType,
                    time,
                    "application/json",
                    correlationId,
                    null,
                    data
            );

            eventHandler.handle(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[KafkaEventConsumer] Failed to process event from topic={}, offset={}: {}",
                    record.topic(), record.offset(), e.getMessage(), e);
            ack.acknowledge();
        }
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
