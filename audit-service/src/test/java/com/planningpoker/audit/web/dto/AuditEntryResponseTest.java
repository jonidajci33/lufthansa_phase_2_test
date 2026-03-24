package com.planningpoker.audit.web.dto;

import com.planningpoker.audit.domain.AuditOperation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEntryResponseTest {

    @Test
    void shouldCreateResponseWithAllFields() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant timestamp = Instant.parse("2026-03-18T10:00:00Z");

        AuditEntryResponse response = new AuditEntryResponse(
                1L, "user", entityId, AuditOperation.CREATED, userId,
                "identity-service", timestamp, null, "{\"name\":\"John\"}",
                "corr-1", "evt-1"
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.entityType()).isEqualTo("user");
        assertThat(response.entityId()).isEqualTo(entityId);
        assertThat(response.operation()).isEqualTo(AuditOperation.CREATED);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.sourceService()).isEqualTo("identity-service");
        assertThat(response.timestamp()).isEqualTo(timestamp);
        assertThat(response.previousState()).isNull();
        assertThat(response.newState()).isEqualTo("{\"name\":\"John\"}");
        assertThat(response.correlationId()).isEqualTo("corr-1");
        assertThat(response.eventId()).isEqualTo("evt-1");
    }

    @Test
    void shouldAllowNullableFields() {
        UUID entityId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        AuditEntryResponse response = new AuditEntryResponse(
                2L, "room", entityId, AuditOperation.DELETED, null,
                "room-service", timestamp, null, null, null, null
        );

        assertThat(response.userId()).isNull();
        assertThat(response.previousState()).isNull();
        assertThat(response.newState()).isNull();
        assertThat(response.correlationId()).isNull();
        assertThat(response.eventId()).isNull();
    }
}
