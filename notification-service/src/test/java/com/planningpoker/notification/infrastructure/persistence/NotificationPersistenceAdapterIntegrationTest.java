package com.planningpoker.notification.infrastructure.persistence;

import com.planningpoker.notification.domain.EmailLog;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(NotificationPersistenceAdapter.class)
class NotificationPersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notification_test_db")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-schema.sql");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.schemas", () -> "notification");
        registry.add("spring.flyway.create-schemas", () -> "true");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "notification");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private NotificationPersistenceAdapter adapter;

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @Autowired
    private EmailLogJpaRepository emailLogJpaRepository;

    @Autowired
    private ProcessedEventJpaRepository processedEventJpaRepository;

    @BeforeEach
    void cleanUp() {
        processedEventJpaRepository.deleteAll();
        emailLogJpaRepository.deleteAll();
        notificationJpaRepository.deleteAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static Notification buildNotification(UUID userId, NotificationType type, String title, String message) {
        return Notification.create(userId, type, title, message);
    }

    private static Notification buildNotificationWithMetadata(UUID userId, NotificationType type,
                                                               String title, String message, String metadata) {
        return Notification.createWithMetadata(userId, type, title, message, metadata);
    }

    private static EmailLog buildEmailLog(String recipient, String subject, String template) {
        return EmailLog.create(recipient, subject, template);
    }

    // ── save + findById ─────────────────────────────────────────────

    @Test
    void shouldSaveAndFindNotificationById() {
        UUID userId = UUID.randomUUID();
        Notification notification = buildNotification(userId, NotificationType.WELCOME, "Welcome!", "Hello there");

        Notification saved = adapter.save(notification);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(NotificationType.WELCOME);
        assertThat(saved.getTitle()).isEqualTo("Welcome!");
        assertThat(saved.getMessage()).isEqualTo("Hello there");
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<Notification> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Welcome!");
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void shouldSaveNotificationWithMetadata() {
        UUID userId = UUID.randomUUID();
        String metadata = "{\"roomId\": \"abc-123\"}";
        Notification notification = buildNotificationWithMetadata(
                userId, NotificationType.INVITATION, "You are invited", "Join the room", metadata);

        Notification saved = adapter.save(notification);

        Optional<Notification> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMetadata()).isEqualTo(metadata);
    }

    @Test
    void shouldReturnEmptyForNonExistentNotification() {
        Optional<Notification> found = adapter.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    // ── findByUserId (pagination) ───────────────────────────────────

    @Test
    void shouldFindByUserIdWithPagination() {
        UUID userId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            adapter.save(buildNotification(userId, NotificationType.SYSTEM, "Notification " + i, null));
        }
        // Save one for a different user to ensure filtering works
        adapter.save(buildNotification(UUID.randomUUID(), NotificationType.SYSTEM, "Other user", null));

        List<Notification> firstPage = adapter.findByUserId(userId, 0, 3);
        assertThat(firstPage).hasSize(3);

        List<Notification> secondPage = adapter.findByUserId(userId, 3, 3);
        assertThat(secondPage).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListForUserWithNoNotifications() {
        List<Notification> result = adapter.findByUserId(UUID.randomUUID(), 0, 10);
        assertThat(result).isEmpty();
    }

    // ── countByUserId ───────────────────────────────────────────────

    @Test
    void shouldCountByUserId() {
        UUID userId = UUID.randomUUID();
        adapter.save(buildNotification(userId, NotificationType.WELCOME, "n1", null));
        adapter.save(buildNotification(userId, NotificationType.SYSTEM, "n2", null));
        adapter.save(buildNotification(UUID.randomUUID(), NotificationType.SYSTEM, "other", null));

        long count = adapter.countByUserId(userId);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroCountForUserWithNoNotifications() {
        long count = adapter.countByUserId(UUID.randomUUID());
        assertThat(count).isZero();
    }

    // ── countUnreadByUserId ─────────────────────────────────────────

    @Test
    void shouldCountUnreadByUserId() {
        UUID userId = UUID.randomUUID();
        // Save two unread
        adapter.save(buildNotification(userId, NotificationType.WELCOME, "Unread 1", null));
        adapter.save(buildNotification(userId, NotificationType.SYSTEM, "Unread 2", null));
        // Save one read
        Notification readNotification = buildNotification(userId, NotificationType.INVITATION, "Read 1", null);
        readNotification.markAsRead();
        adapter.save(readNotification);

        long unreadCount = adapter.countUnreadByUserId(userId);
        assertThat(unreadCount).isEqualTo(2);
    }

    // ── markAllReadByUserId ─────────────────────────────────────────

    @Test
    @Transactional
    void shouldMarkAllReadByUserId() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        adapter.save(buildNotification(userId, NotificationType.WELCOME, "n1", null));
        adapter.save(buildNotification(userId, NotificationType.SYSTEM, "n2", null));
        adapter.save(buildNotification(otherUserId, NotificationType.SYSTEM, "other", null));

        // Flush and commit so the @Modifying query can see the data
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        adapter.markAllReadByUserId(userId);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Verify in a new transaction
        TestTransaction.start();
        long unreadCount = adapter.countUnreadByUserId(userId);
        assertThat(unreadCount).isZero();

        // Other user's notifications should remain unread
        long otherUnreadCount = adapter.countUnreadByUserId(otherUserId);
        assertThat(otherUnreadCount).isEqualTo(1);
    }

    // ── saveEmailLog ────────────────────────────────────────────────

    @Test
    void shouldSaveEmailLog() {
        EmailLog emailLog = buildEmailLog("user@example.com", "Welcome to Planning Poker", "welcome-template");

        EmailLog saved = adapter.saveEmailLog(emailLog);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRecipient()).isEqualTo("user@example.com");
        assertThat(saved.getSubject()).isEqualTo("Welcome to Planning Poker");
        assertThat(saved.getTemplate()).isEqualTo("welcome-template");
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getSentAt()).isNull();
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldSaveEmailLogWithNullTemplate() {
        EmailLog emailLog = buildEmailLog("user@example.com", "No template", null);

        EmailLog saved = adapter.saveEmailLog(emailLog);

        assertThat(saved.getTemplate()).isNull();
    }

    // ── isEventProcessed + markEventProcessed ───────────────────────

    @Test
    void shouldReturnFalseForUnprocessedEvent() {
        boolean result = adapter.isEventProcessed("event-123");
        assertThat(result).isFalse();
    }

    @Test
    void shouldMarkEventProcessedAndDetectIt() {
        String eventId = "event-" + UUID.randomUUID();

        assertThat(adapter.isEventProcessed(eventId)).isFalse();

        adapter.markEventProcessed(eventId);

        assertThat(adapter.isEventProcessed(eventId)).isTrue();
    }

    @Test
    void shouldHandleMultipleEventIds() {
        String eventId1 = "event-1-" + UUID.randomUUID();
        String eventId2 = "event-2-" + UUID.randomUUID();

        adapter.markEventProcessed(eventId1);

        assertThat(adapter.isEventProcessed(eventId1)).isTrue();
        assertThat(adapter.isEventProcessed(eventId2)).isFalse();
    }
}
