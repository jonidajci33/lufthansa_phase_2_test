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

    @Test
    void shouldRejectNullOperation() {
        assertThatThrownBy(() -> AuditEntry.create(
                "user", ENTITY_ID, null, USER_ID,
                "identity-service", Instant.now(), null, null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("operation");
    }

    @Test
    void shouldRejectNullSourceService() {
        assertThatThrownBy(() -> AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.CREATED, USER_ID,
                null, Instant.now(), null, null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceService");
    }

    @Test
    void shouldRejectBlankSourceService() {
        assertThatThrownBy(() -> AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.CREATED, USER_ID,
                "  ", Instant.now(), null, null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceService");
    }

    @Test
    void shouldRejectNullEntityType() {
        assertThatThrownBy(() -> AuditEntry.create(
                null, ENTITY_ID, AuditOperation.CREATED, USER_ID,
                "identity-service", Instant.now(), null, null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entityType");
    }

    @Test
    void shouldDefaultTimestampToNowWhenNull() {
        Instant before = Instant.now();

        AuditEntry entry = AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.CREATED, USER_ID,
                "identity-service", null, null, null, null, null
        );

        assertThat(entry.getTimestamp()).isAfterOrEqualTo(before);
    }

    @Test
    void shouldPreserveExplicitTimestamp() {
        Instant explicit = Instant.parse("2026-01-15T10:00:00Z");

        AuditEntry entry = AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.CREATED, USER_ID,
                "identity-service", explicit, null, null, null, null
        );

        assertThat(entry.getTimestamp()).isEqualTo(explicit);
    }

    @Test
    void shouldAllowNullUserId() {
        AuditEntry entry = AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.CREATED, null,
                "identity-service", Instant.now(), null, null, null, null
        );

        assertThat(entry.getUserId()).isNull();
    }

    @Test
    void shouldPreservePreviousState() {
        String previousState = "{\"name\":\"old\"}";
        String newState = "{\"name\":\"new\"}";

        AuditEntry entry = AuditEntry.create(
                "user", ENTITY_ID, AuditOperation.UPDATED, USER_ID,
                "identity-service", Instant.now(), previousState, newState,
                "corr-1", "evt-1"
        );

        assertThat(entry.getPreviousState()).isEqualTo(previousState);
        assertThat(entry.getNewState()).isEqualTo(newState);
    }

    @Test
    void shouldCreateEntryWithIdViaConstructor() {
        AuditEntry entry = new AuditEntry(
                42L, "room", ENTITY_ID, AuditOperation.DELETED, USER_ID,
                "room-service", Instant.now(), null, null, "corr-2", "evt-2"
        );

        assertThat(entry.getId()).isEqualTo(42L);
        assertThat(entry.getOperation()).isEqualTo(AuditOperation.DELETED);
    }
}
