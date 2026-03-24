package com.planningpoker.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    // ── markAsRead ─────────────────────────────────────────────────────

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = createUnreadNotification();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void shouldBeIdempotentWhenMarkingAlreadyReadNotification() {
        Notification notification = createUnreadNotification();
        notification.markAsRead();

        // Mark as read a second time — should not throw or change state
        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void shouldNotBeReadByDefault() {
        Notification notification = Notification.create(
                UUID.randomUUID(),
                NotificationType.WELCOME,
                "Welcome",
                "Hello!"
        );

        assertThat(notification.isRead()).isFalse();
    }

    // ── create factory ─────────────────────────────────────────────────

    @Test
    void shouldCreateNotificationWithCorrectDefaults() {
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.create(
                userId,
                NotificationType.INVITATION,
                "Invitation",
                "You have been invited"
        );

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getType()).isEqualTo(NotificationType.INVITATION);
        assertThat(notification.getTitle()).isEqualTo("Invitation");
        assertThat(notification.getMessage()).isEqualTo("You have been invited");
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPreserveAllFieldsViaConstructor() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        Notification notification = new Notification(
                id, userId, NotificationType.SYSTEM, "System Alert", "Maintenance window", null, true, now
        );

        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getType()).isEqualTo(NotificationType.SYSTEM);
        assertThat(notification.getTitle()).isEqualTo("System Alert");
        assertThat(notification.getMessage()).isEqualTo("Maintenance window");
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getCreatedAt()).isEqualTo(now);
    }

    // ── NotificationType enum ──────────────────────────────────────────

    @Test
    void shouldHaveExpectedNotificationTypes() {
        assertThat(NotificationType.values()).containsExactlyInAnyOrder(
                NotificationType.WELCOME,
                NotificationType.INVITATION,
                NotificationType.VOTING_STARTED,
                NotificationType.VOTING_FINISHED,
                NotificationType.SYSTEM
        );
    }

    // ── EmailLog ───────────────────────────────────────────────────────

    @Test
    void shouldCreateEmailLogWithPendingStatus() {
        EmailLog log = EmailLog.create("user@example.com", "Subject", "template-name");

        assertThat(log.getId()).isNotNull();
        assertThat(log.getRecipient()).isEqualTo("user@example.com");
        assertThat(log.getSubject()).isEqualTo("Subject");
        assertThat(log.getTemplate()).isEqualTo("template-name");
        assertThat(log.getStatus()).isEqualTo("PENDING");
        assertThat(log.getSentAt()).isNull();
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldMarkEmailLogAsSent() {
        EmailLog log = EmailLog.create("user@example.com", "Subject", "template");

        log.markSent();

        assertThat(log.getStatus()).isEqualTo("SENT");
        assertThat(log.getSentAt()).isNotNull();
    }

    @Test
    void shouldMarkEmailLogAsFailed() {
        EmailLog log = EmailLog.create("user@example.com", "Subject", "template");

        log.markFailed("Connection refused");

        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getErrorMessage()).isEqualTo("Connection refused");
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private static Notification createUnreadNotification() {
        return new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationType.WELCOME,
                "Welcome!",
                "Welcome to the app",
                null,
                false,
                Instant.now()
        );
    }
}
