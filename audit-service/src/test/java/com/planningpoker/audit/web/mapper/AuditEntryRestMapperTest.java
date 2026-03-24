package com.planningpoker.audit.web.mapper;

import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditOperation;
import com.planningpoker.audit.web.dto.AuditEntryResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEntryRestMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditEntry entry = new AuditEntry(
                42L,
                "Story",
                entityId,
                AuditOperation.CREATED,
                userId,
                "estimation-service",
                Instant.parse("2026-01-15T10:00:00Z"),
                null,
                "{\"title\":\"Login Page\"}",
                "corr-123",
                "evt-abc"
        );

        AuditEntryResponse result = AuditEntryRestMapper.toResponse(entry);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.entityType()).isEqualTo("Story");
        assertThat(result.entityId()).isEqualTo(entityId);
        assertThat(result.operation()).isEqualTo(AuditOperation.CREATED);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.sourceService()).isEqualTo("estimation-service");
        assertThat(result.timestamp()).isEqualTo(Instant.parse("2026-01-15T10:00:00Z"));
        assertThat(result.previousState()).isNull();
        assertThat(result.newState()).isEqualTo("{\"title\":\"Login Page\"}");
        assertThat(result.correlationId()).isEqualTo("corr-123");
        assertThat(result.eventId()).isEqualTo("evt-abc");
    }

    @Test
    void toResponse_returnsNullForNullInput() {
        assertThat(AuditEntryRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_handlesNullOptionalFields() {
        AuditEntry entry = new AuditEntry(
                1L, "Room", UUID.randomUUID(), AuditOperation.DELETED,
                null, "room-service", Instant.now(),
                null, null, null, null
        );

        AuditEntryResponse result = AuditEntryRestMapper.toResponse(entry);

        assertThat(result.userId()).isNull();
        assertThat(result.previousState()).isNull();
        assertThat(result.newState()).isNull();
        assertThat(result.correlationId()).isNull();
        assertThat(result.eventId()).isNull();
    }

    @Test
    void toResponseList_mapsAllElements() {
        AuditEntry e1 = new AuditEntry(1L, "Story", UUID.randomUUID(), AuditOperation.CREATED,
                UUID.randomUUID(), "estimation-service", Instant.now(),
                null, "{}", "c1", "e1");
        AuditEntry e2 = new AuditEntry(2L, "Room", UUID.randomUUID(), AuditOperation.UPDATED,
                UUID.randomUUID(), "room-service", Instant.now(),
                "{}", "{}", "c2", "e2");

        List<AuditEntryResponse> result = AuditEntryRestMapper.toResponseList(List.of(e1, e2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
    }

    @Test
    void toResponseList_returnsEmptyForNull() {
        assertThat(AuditEntryRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_returnsEmptyForEmptyList() {
        assertThat(AuditEntryRestMapper.toResponseList(Collections.emptyList())).isEmpty();
    }
}
