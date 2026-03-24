package com.planningpoker.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailLogTest {

    @Test
    void shouldCreatePendingEmailLog() {
        EmailLog log = EmailLog.create("user@example.com", "Welcome!", "welcome-email");

        assertThat(log.getId()).isNotNull();
        assertThat(log.getRecipient()).isEqualTo("user@example.com");
        assertThat(log.getSubject()).isEqualTo("Welcome!");
        assertThat(log.getTemplate()).isEqualTo("welcome-email");
        assertThat(log.getStatus()).isEqualTo("PENDING");
        assertThat(log.getSentAt()).isNull();
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldMarkAsSent() {
        EmailLog log = EmailLog.create("user@example.com", "Subject", "template");

        log.markSent();

        assertThat(log.getStatus()).isEqualTo("SENT");
        assertThat(log.getSentAt()).isNotNull();
    }

    @Test
    void shouldMarkAsFailed() {
        EmailLog log = EmailLog.create("user@example.com", "Subject", "template");

        log.markFailed("SMTP connection refused");

        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getErrorMessage()).isEqualTo("SMTP connection refused");
    }

    @Test
    void shouldPreserveAllFieldsViaConstructor() {
        UUID id = UUID.randomUUID();
        Instant sentAt = Instant.now();
        Instant createdAt = Instant.now().minusSeconds(60);

        EmailLog log = new EmailLog(id, "admin@example.com", "Alert", "alert-template",
                "SENT", sentAt, null, createdAt);

        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getRecipient()).isEqualTo("admin@example.com");
        assertThat(log.getSubject()).isEqualTo("Alert");
        assertThat(log.getTemplate()).isEqualTo("alert-template");
        assertThat(log.getStatus()).isEqualTo("SENT");
        assertThat(log.getSentAt()).isEqualTo(sentAt);
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldAllowDefaultConstructor() {
        EmailLog log = new EmailLog();

        assertThat(log.getId()).isNull();
        assertThat(log.getRecipient()).isNull();
        assertThat(log.getStatus()).isNull();
    }

    @Test
    void shouldSupportSetters() {
        EmailLog log = new EmailLog();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        log.setId(id);
        log.setRecipient("test@example.com");
        log.setSubject("Test Subject");
        log.setTemplate("test-template");
        log.setStatus("PENDING");
        log.setSentAt(now);
        log.setErrorMessage("some error");
        log.setCreatedAt(now);

        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getRecipient()).isEqualTo("test@example.com");
        assertThat(log.getSubject()).isEqualTo("Test Subject");
        assertThat(log.getTemplate()).isEqualTo("test-template");
        assertThat(log.getStatus()).isEqualTo("PENDING");
        assertThat(log.getSentAt()).isEqualTo(now);
        assertThat(log.getErrorMessage()).isEqualTo("some error");
        assertThat(log.getCreatedAt()).isEqualTo(now);
    }
}
