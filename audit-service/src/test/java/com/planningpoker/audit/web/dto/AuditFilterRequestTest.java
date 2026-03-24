package com.planningpoker.audit.web.dto;

import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.AuditOperation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditFilterRequestTest {

    @Test
    void shouldConvertToFilterWithAllFields() {
        UUID userId = UUID.randomUUID();
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-12-31T23:59:59Z");

        AuditFilterRequest request = new AuditFilterRequest("user", "CREATED", userId, from, to);

        AuditFilter filter = request.toDomain();

        assertThat(filter.entityType()).isEqualTo("user");
        assertThat(filter.operation()).isEqualTo(AuditOperation.CREATED);
        assertThat(filter.userId()).isEqualTo(userId);
        assertThat(filter.from()).isEqualTo(from);
        assertThat(filter.to()).isEqualTo(to);
    }

    @Test
    void shouldConvertToFilterWithNullOperation() {
        AuditFilterRequest request = new AuditFilterRequest("room", null, null, null, null);

        AuditFilter filter = request.toDomain();

        assertThat(filter.entityType()).isEqualTo("room");
        assertThat(filter.operation()).isNull();
        assertThat(filter.userId()).isNull();
        assertThat(filter.from()).isNull();
        assertThat(filter.to()).isNull();
    }

    @Test
    void shouldConvertToFilterWithBlankOperation() {
        AuditFilterRequest request = new AuditFilterRequest("user", "  ", null, null, null);

        AuditFilter filter = request.toDomain();

        assertThat(filter.operation()).isNull();
    }

    @Test
    void shouldConvertToFilterWithLowercaseOperation() {
        AuditFilterRequest request = new AuditFilterRequest("user", "deleted", null, null, null);

        AuditFilter filter = request.toDomain();

        assertThat(filter.operation()).isEqualTo(AuditOperation.DELETED);
    }

    @Test
    void shouldConvertToFilterWithMixedCaseOperation() {
        AuditFilterRequest request = new AuditFilterRequest("user", "Updated", null, null, null);

        AuditFilter filter = request.toDomain();

        assertThat(filter.operation()).isEqualTo(AuditOperation.UPDATED);
    }

    @Test
    void shouldThrowForInvalidOperationString() {
        AuditFilterRequest request = new AuditFilterRequest("user", "INVALID", null, null, null);

        assertThatThrownBy(request::toDomain)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldConvertToFilterWithAllNulls() {
        AuditFilterRequest request = new AuditFilterRequest(null, null, null, null, null);

        AuditFilter filter = request.toDomain();

        assertThat(filter.entityType()).isNull();
        assertThat(filter.operation()).isNull();
        assertThat(filter.userId()).isNull();
        assertThat(filter.from()).isNull();
        assertThat(filter.to()).isNull();
    }
}
