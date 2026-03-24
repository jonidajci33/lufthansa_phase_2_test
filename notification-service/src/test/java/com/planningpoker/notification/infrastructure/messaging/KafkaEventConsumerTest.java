package com.planningpoker.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.notification.application.service.NotificationEventHandler;
import com.planningpoker.shared.event.DomainEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaEventConsumerTest {

    @Mock
    private NotificationEventHandler eventHandler;

    @Mock
    private Acknowledgment acknowledgment;

    @Captor
    private ArgumentCaptor<DomainEvent<JsonNode>> eventCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private KafkaEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaEventConsumer(eventHandler);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private ConsumerRecord<String, JsonNode> buildRecord(String topic, String key, String json) throws Exception {
        JsonNode value = objectMapper.readTree(json);
        return new ConsumerRecord<>(topic, 0, 0L, key, value);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Happy path — full event
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldConsumeValidEventAndDelegateToHandler() throws Exception {
        String json = """
                {
                    "id": "evt-001",
                    "source": "identity-service",
                    "type": "identity.user.registered.v1",
                    "time": "2026-03-18T10:00:00Z",
                    "correlationid": "corr-001",
                    "data": {"userId": "11111111-1111-1111-1111-111111111111", "username": "john", "email": "john@example.com"}
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-1", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();

        assertThat(captured.id()).isEqualTo("evt-001");
        assertThat(captured.source()).isEqualTo("identity-service");
        assertThat(captured.type()).isEqualTo("identity.user.registered.v1");
        assertThat(captured.correlationId()).isEqualTo("corr-001");
        assertThat(captured.time()).isEqualTo(Instant.parse("2026-03-18T10:00:00Z"));
        assertThat(captured.data()).isNotNull();
        assertThat(captured.data().get("username").asText()).isEqualTo("john");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldConsumeRoomEvent() throws Exception {
        String json = """
                {
                    "id": "evt-002",
                    "source": "room-service",
                    "type": "room.invitation.created.v1",
                    "time": "2026-03-18T11:00:00Z",
                    "correlationid": "corr-002",
                    "data": {"roomId": "22222222-2222-2222-2222-222222222222", "invitedBy": "33333333-3333-3333-3333-333333333333"}
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("room.events", "key-2", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();
        assertThat(captured.type()).isEqualTo("room.invitation.created.v1");
        assertThat(captured.source()).isEqualTo("room-service");
        verify(acknowledgment).acknowledge();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Null value
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldHandleNullValueGracefully() {
        ConsumerRecord<String, JsonNode> record = new ConsumerRecord<>("identity.user.events", 0, 0L, "key", null);

        consumer.consume(record, acknowledgment);

        verify(eventHandler, never()).handle(any());
        verify(acknowledgment).acknowledge();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Missing fields
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldHandleMissingOptionalFields() throws Exception {
        String json = """
                {
                    "type": "identity.user.registered.v1",
                    "data": {"userId": "11111111-1111-1111-1111-111111111111"}
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-3", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();

        assertThat(captured.id()).isNull();
        assertThat(captured.source()).isNull();
        assertThat(captured.correlationId()).isNull();
        assertThat(captured.time()).isNotNull(); // defaults to Instant.now()
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleMissingDataField() throws Exception {
        String json = """
                {
                    "id": "evt-003",
                    "source": "test-service",
                    "type": "test.event.v1"
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-4", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();
        assertThat(captured.data()).isNull();
        verify(acknowledgment).acknowledge();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Invalid time format
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldDefaultToNowWhenTimeIsInvalid() throws Exception {
        String json = """
                {
                    "id": "evt-004",
                    "source": "test-service",
                    "type": "test.event.v1",
                    "time": "not-a-valid-instant",
                    "data": {}
                }
                """;

        Instant before = Instant.now();
        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-5", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();
        assertThat(captured.time()).isAfterOrEqualTo(before);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldDefaultToNowWhenTimeIsMissing() throws Exception {
        String json = """
                {
                    "id": "evt-005",
                    "source": "test-service",
                    "type": "test.event.v1",
                    "data": {}
                }
                """;

        Instant before = Instant.now();
        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-6", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();
        assertThat(captured.time()).isAfterOrEqualTo(before);
        verify(acknowledgment).acknowledge();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Handler throws exception
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldAcknowledgeEvenWhenHandlerThrows() throws Exception {
        String json = """
                {
                    "id": "evt-006",
                    "source": "test-service",
                    "type": "identity.user.registered.v1",
                    "time": "2026-03-18T10:00:00Z",
                    "data": {"userId": "11111111-1111-1111-1111-111111111111"}
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-7", json);
        doThrow(new RuntimeException("handler error")).when(eventHandler).handle(any());

        consumer.consume(record, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Non-textual fields
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldHandleNonTextualFieldsGracefully() throws Exception {
        String json = """
                {
                    "id": 12345,
                    "source": 999,
                    "type": "identity.user.registered.v1",
                    "time": 123456789,
                    "correlationid": true,
                    "data": {"userId": "11111111-1111-1111-1111-111111111111"}
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", "key-8", json);

        consumer.consume(record, acknowledgment);

        verify(eventHandler).handle(eventCaptor.capture());
        DomainEvent<JsonNode> captured = eventCaptor.getValue();
        // Non-textual fields should be extracted as null
        assertThat(captured.id()).isNull();
        assertThat(captured.source()).isNull();
        assertThat(captured.correlationId()).isNull();
        verify(acknowledgment).acknowledge();
    }
}
