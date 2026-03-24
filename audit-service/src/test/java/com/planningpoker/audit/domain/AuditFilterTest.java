package com.planningpoker.audit.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditFilterTest {

    @Test
    void shouldCreateEmptyFilter() {
        AuditFilter filter = AuditFilter.empty();

        assertThat(filter.entityType()).isNull();
        assertThat(filter.operation()).isNull();
        assertThat(filter.userId()).isNull();
        assertThat(filter.from()).isNull();
        assertThat(filter.to()).isNull();
    }

    @Test
    void shouldCreateFilterWithAllFields() {
        UUID userId = UUID.randomUUID();
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-12-31T23:59:59Z");

        AuditFilter filter = new AuditFilter("user", AuditOperation.CREATED, userId, from, to);

        assertThat(filter.entityType()).isEqualTo("user");
        assertThat(filter.operation()).isEqualTo(AuditOperation.CREATED);
        assertThat(filter.userId()).isEqualTo(userId);
        assertThat(filter.from()).isEqualTo(from);
        assertThat(filter.to()).isEqualTo(to);
    }

    @Test
    void shouldCreateFilterWithPartialFields() {
        AuditFilter filter = new AuditFilter("room", null, null, null, null);

        assertThat(filter.entityType()).isEqualTo("room");
        assertThat(filter.operation()).isNull();
        assertThat(filter.userId()).isNull();
    }
}
