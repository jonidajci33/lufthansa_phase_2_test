package com.planningpoker.audit.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditEntryTest {

    private static final UUID ENTITY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void shouldCreateAuditEntryViaFactory() {
        AuditEntry entry = AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.CREATED, USER_ID,
                "identity-service", Instant.now(), null, "{\"name\":\"test\"}",
                "corr-1", "evt-1"
        );

        assertThat(entry.getId()).isNull();
        assertThat(entry.getEntityType()).isEqualTo("user");
        assertThat(entry.getEntityId()).isEqualTo(ENTITY_ID);
        assertThat(entry.getOperation()).isEqualTo(AuditOperation.CREATED);
        assertThat(entry.getUserId()).isEqualTo(USER_ID);
        assertThat(entry.getSourceService()).isEqualTo("identity-service");
        assertThat(entry.getTimestamp()).isNotNull();
        assertThat(entry.getNewState()).isEqualTo("{\"name\":\"test\"}");
        assertThat(entry.getCorrelationId()).isEqualTo("corr-1");
        assertThat(entry.getEventId()).isEqualTo("evt-1");
    }

    @Test
    void shouldRejectBlankEntityType() {
        assertThatThrownBy(() -> AuditEntry.create(
                "", ENTITY_ID, AuditOperation.CREATED, USER_ID,
                "identity-service", Instant.now(), null, null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entityType");
    }

    @Test
    void shouldRejectNullEntityId() {
        assertThatThrownBy(() -> AuditEntry.create(
                "user", null, AuditOperation.CREATED, USER_ID,
                "identity-service", Instant.now(), null, null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entityId");
    }
}
