package com.planningpoker.audit.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.audit.application.port.in.RecordAuditUseCase;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditOperation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditKafkaConsumerTest {

    @Mock
    private RecordAuditUseCase recordAuditUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    @Captor
    private ArgumentCaptor<AuditEntry> entryCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuditKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new AuditKafkaConsumer(recordAuditUseCase, objectMapper);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private ConsumerRecord<String, JsonNode> buildRecord(String topic, String key, String json) throws Exception {
        JsonNode value = objectMapper.readTree(json);
        return new ConsumerRecord<>(topic, 0, 0L, key, value);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Consume events
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldConsumeUserRegisteredAndCreateAuditEntry() throws Exception {
        UUID userId = UUID.randomUUID();
        String json = """
                {
                    "id": "evt-001",
                    "source": "identity-service",
                    "type": "identity.user.registered.v1",
                    "time": "2026-03-18T10:00:00Z",
                    "correlationid": "corr-001",
                    "data": {"id": "%s", "username": "john"}
                }
                """.formatted(userId);

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", userId.toString(), json);

        consumer.consume(record, acknowledgment);

        verify(recordAuditUseCase).record(entryCaptor.capture());
        AuditEntry entry = entryCaptor.getValue();

        assertThat(entry.getEntityType()).isEqualTo("user");
        assertThat(entry.getEntityId()).isEqualTo(userId);
        assertThat(entry.getOperation()).isEqualTo(AuditOperation.CREATED);
        assertThat(entry.getSourceService()).isEqualTo("identity-service");
        assertThat(entry.getEventId()).isEqualTo("evt-001");
        assertThat(entry.getCorrelationId()).isEqualTo("corr-001");
        assertThat(entry.getNewState()).contains("john");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldConsumeRoomCreatedEvent() throws Exception {
        UUID roomId = UUID.randomUUID();
        String json = """
                {
                    "id": "evt-002",
                    "source": "room-service",
                    "type": "room.room.created.v1",
                    "time": "2026-03-18T10:00:00Z",
                    "correlationid": "corr-002",
                    "data": {"roomId": "%s", "name": "Sprint Room"}
                }
                """.formatted(roomId);

        ConsumerRecord<String, JsonNode> record = buildRecord("room.events", roomId.toString(), json);

        consumer.consume(record, acknowledgment);

        verify(recordAuditUseCase).record(entryCaptor.capture());
        AuditEntry entry = entryCaptor.getValue();

        assertThat(entry.getEntityType()).isEqualTo("room");
        assertThat(entry.getEntityId()).isEqualTo(roomId);
        assertThat(entry.getOperation()).isEqualTo(AuditOperation.CREATED);
        assertThat(entry.getSourceService()).isEqualTo("room-service");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldDeduplicateEventsViaService() throws Exception {
        UUID entityId = UUID.randomUUID();
        String json = """
                {
                    "id": "evt-dup",
                    "source": "identity-service",
                    "type": "identity.user.registered.v1",
                    "time": "2026-03-18T10:00:00Z",
                    "data": {"id": "%s"}
                }
                """.formatted(entityId);

        when(recordAuditUseCase.record(any())).thenReturn(null);

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", entityId.toString(), json);
        consumer.consume(record, acknowledgment);

        verify(recordAuditUseCase).record(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldRecordUnknownEventTypeAsUpdated() throws Exception {
        UUID entityId = UUID.randomUUID();
        String json = """
                {
                    "id": "evt-003",
                    "source": "some-service",
                    "type": "custom.entity.something.v1",
                    "time": "2026-03-18T10:00:00Z",
                    "data": {"id": "%s"}
                }
                """.formatted(entityId);

        ConsumerRecord<String, JsonNode> record = buildRecord("custom.events", entityId.toString(), json);

        consumer.consume(record, acknowledgment);

        verify(recordAuditUseCase).record(entryCaptor.capture());
        AuditEntry entry = entryCaptor.getValue();

        assertThat(entry.getEntityType()).isEqualTo("entity");
        assertThat(entry.getOperation()).isEqualTo(AuditOperation.UPDATED);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleMalformedEventGracefully() throws Exception {
        // Malformed: missing required fields, but JSON is valid
        String json = """
                {
                    "id": "evt-bad",
                    "type": "",
                    "data": null
                }
                """;

        ConsumerRecord<String, JsonNode> record = buildRecord("identity.user.events", null, json);

        // Should not throw, should still acknowledge
        consumer.consume(record, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    // ═══════════════════════════════════════════════════════════════════
    // extractEntityType
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldExtractEntityTypeFromEventType() {
        assertThat(consumer.extractEntityType("identity.user.registered.v1")).isEqualTo("user");
        assertThat(consumer.extractEntityType("room.room.created.v1")).isEqualTo("room");
        assertThat(consumer.extractEntityType("estimation.story.created.v1")).isEqualTo("story");
        assertThat(consumer.extractEntityType("estimation.voting.started.v1")).isEqualTo("voting");
        assertThat(consumer.extractEntityType(null)).isEqualTo("unknown");
        assertThat(consumer.extractEntityType("")).isEqualTo("unknown");
    }

    // ═══════════════════════════════════════════════════════════════════
    // extractOperation
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldExtractOperationFromEventType() {
        assertThat(consumer.extractOperation("identity.user.registered.v1")).isEqualTo(AuditOperation.CREATED);
        assertThat(consumer.extractOperation("room.room.created.v1")).isEqualTo(AuditOperation.CREATED);
        assertThat(consumer.extractOperation("room.room.updated.v1")).isEqualTo(AuditOperation.UPDATED);
        assertThat(consumer.extractOperation("room.room.deleted.v1")).isEqualTo(AuditOperation.DELETED);
        assertThat(consumer.extractOperation("identity.user.deactivated.v1")).isEqualTo(AuditOperation.DELETED);
        assertThat(consumer.extractOperation("estimation.voting.started.v1")).isEqualTo(AuditOperation.CREATED);
        assertThat(consumer.extractOperation("estimation.vote.submitted.v1")).isEqualTo(AuditOperation.CREATED);
        assertThat(consumer.extractOperation("estimation.voting.finished.v1")).isEqualTo(AuditOperation.UPDATED);
    }
}
