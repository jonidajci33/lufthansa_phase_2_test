package com.planningpoker.notification.application.service;

import com.planningpoker.notification.application.port.in.GetNotificationsUseCase;
import com.planningpoker.notification.application.port.in.MarkReadUseCase;
import com.planningpoker.notification.application.port.in.UnreadCountUseCase;
import com.planningpoker.notification.application.port.out.NotificationPersistencePort;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates all notification-related use cases.
 */
@Service
@Transactional
public class NotificationService implements GetNotificationsUseCase, MarkReadUseCase, UnreadCountUseCase {

    private final NotificationPersistencePort persistencePort;

    public NotificationService(NotificationPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    // ── GetNotificationsUseCase ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(UUID userId, int offset, int limit) {
        return persistencePort.findByUserId(userId, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCount(UUID userId) {
        return persistencePort.countByUserId(userId);
    }

    // ── MarkReadUseCase ───────────────────────────────────────────────

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = persistencePort.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        notification.markAsRead();
        persistencePort.save(notification);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        persistencePort.markAllReadByUserId(userId);
    }

    // ── UnreadCountUseCase ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return persistencePort.countUnreadByUserId(userId);
    }
}
