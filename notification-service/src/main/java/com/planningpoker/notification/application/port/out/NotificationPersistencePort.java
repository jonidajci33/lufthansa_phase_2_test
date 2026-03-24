package com.planningpoker.notification.application.port.out;

import com.planningpoker.notification.domain.EmailLog;
import com.planningpoker.notification.domain.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for notification and email log persistence.
 */
public interface NotificationPersistencePort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByUserId(UUID userId, int offset, int limit);

    long countByUserId(UUID userId);

    long countUnreadByUserId(UUID userId);

    void markAllReadByUserId(UUID userId);

    EmailLog saveEmailLog(EmailLog emailLog);

    boolean isEventProcessed(String eventId);

    void markEventProcessed(String eventId);
}
