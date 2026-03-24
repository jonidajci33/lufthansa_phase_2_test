package com.planningpoker.notification.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailLogJpaEntityTest {

    @Test
    void shouldSetAndGetAllFields() {
        EmailLogJpaEntity entity = new EmailLogJpaEntity();
        UUID id = UUID.randomUUID();
        Instant sentAt = Instant.now();
        Instant createdAt = Instant.now().minusSeconds(10);

        entity.setId(id);
        entity.setRecipient("user@example.com");
        entity.setSubject("Welcome");
        entity.setTemplate("welcome-email");
        entity.setStatus("SENT");
        entity.setSentAt(sentAt);
        entity.setErrorMessage(null);
        entity.setCreatedAt(createdAt);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getRecipient()).isEqualTo("user@example.com");
        assertThat(entity.getSubject()).isEqualTo("Welcome");
        assertThat(entity.getTemplate()).isEqualTo("welcome-email");
        assertThat(entity.getStatus()).isEqualTo("SENT");
        assertThat(entity.getSentAt()).isEqualTo(sentAt);
        assertThat(entity.getErrorMessage()).isNull();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldDefaultStatusToPending() {
        EmailLogJpaEntity entity = new EmailLogJpaEntity();

        assertThat(entity.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldSetCreatedAtOnPrePersistWhenNull() {
        EmailLogJpaEntity entity = new EmailLogJpaEntity();
        assertThat(entity.getCreatedAt()).isNull();

        Instant before = Instant.now();
        entity.onPrePersist();
        Instant after = Instant.now();

        assertThat(entity.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void shouldNotOverrideCreatedAtOnPrePersistWhenAlreadySet() {
        EmailLogJpaEntity entity = new EmailLogJpaEntity();
        Instant explicit = Instant.parse("2026-01-01T00:00:00Z");
        entity.setCreatedAt(explicit);

        entity.onPrePersist();

        assertThat(entity.getCreatedAt()).isEqualTo(explicit);
    }

    @Test
    void shouldStoreErrorMessage() {
        EmailLogJpaEntity entity = new EmailLogJpaEntity();
        entity.setStatus("FAILED");
        entity.setErrorMessage("SMTP timeout");

        assertThat(entity.getStatus()).isEqualTo("FAILED");
        assertThat(entity.getErrorMessage()).isEqualTo("SMTP timeout");
    }
}
