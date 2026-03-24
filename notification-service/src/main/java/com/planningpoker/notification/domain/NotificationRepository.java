package com.planningpoker.notification.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for notification persistence.
 */
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByUserId(UUID userId, int offset, int limit);

    long countByUserId(UUID userId);

    long countUnreadByUserId(UUID userId);

    void markAllReadByUserId(UUID userId);
}
