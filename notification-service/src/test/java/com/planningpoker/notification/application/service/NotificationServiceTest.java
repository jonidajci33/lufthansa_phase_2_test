package com.planningpoker.notification.application.service;

import com.planningpoker.notification.application.port.out.NotificationPersistencePort;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @InjectMocks
    private NotificationService notificationService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NOTIFICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    // ── getNotifications ──────────────────────────────────────────────

    @Test
    void shouldReturnPaginatedNotificationsForUser() {
        Notification n1 = sampleNotification(NOTIFICATION_ID, "Title 1");
        Notification n2 = sampleNotification(UUID.randomUUID(), "Title 2");
        when(persistencePort.findByUserId(USER_ID, 0, 20)).thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.getNotifications(USER_ID, 0, 20);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Title 1");
        verify(persistencePort).findByUserId(USER_ID, 0, 20);
    }

    @Test
    void shouldReturnEmptyListWhenNoNotificationsExist() {
        when(persistencePort.findByUserId(USER_ID, 0, 20)).thenReturn(List.of());

        List<Notification> result = notificationService.getNotifications(USER_ID, 0, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTotalCountForUser() {
        when(persistencePort.countByUserId(USER_ID)).thenReturn(42L);

        long count = notificationService.getTotalCount(USER_ID);

        assertThat(count).isEqualTo(42L);
    }

    // ── markAsRead ────────────────────────────────────────────────────

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = sampleNotification(NOTIFICATION_ID, "Title");
        when(persistencePort.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(NOTIFICATION_ID);

        verify(persistencePort).findById(NOTIFICATION_ID);
        verify(persistencePort).save(any(Notification.class));
    }

    @Test
    void shouldThrowWhenMarkingNonExistentNotification() {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(persistencePort.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(missingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(persistencePort, never()).save(any());
    }

    // ── markAllAsRead ─────────────────────────────────────────────────

    @Test
    void shouldMarkAllNotificationsAsRead() {
        notificationService.markAllAsRead(USER_ID);

        verify(persistencePort).markAllReadByUserId(USER_ID);
    }

    // ── getUnreadCount ────────────────────────────────────────────────

    @Test
    void shouldReturnUnreadCount() {
        when(persistencePort.countUnreadByUserId(USER_ID)).thenReturn(5L);

        long count = notificationService.getUnreadCount(USER_ID);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void shouldReturnZeroWhenNoUnreadNotifications() {
        when(persistencePort.countUnreadByUserId(USER_ID)).thenReturn(0L);

        long count = notificationService.getUnreadCount(USER_ID);

        assertThat(count).isZero();
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private static Notification sampleNotification(UUID id, String title) {
        return new Notification(
                id,
                USER_ID,
                NotificationType.WELCOME,
                title,
                "Message body",
                null,
                false,
                Instant.now()
        );
    }
}
