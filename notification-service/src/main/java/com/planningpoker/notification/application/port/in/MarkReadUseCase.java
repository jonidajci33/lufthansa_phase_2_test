package com.planningpoker.notification.application.port.in;

import java.util.UUID;

/**
 * Use case for marking notifications as read.
 */
public interface MarkReadUseCase {

    void markAsRead(UUID notificationId);

    void markAllAsRead(UUID userId);
}
