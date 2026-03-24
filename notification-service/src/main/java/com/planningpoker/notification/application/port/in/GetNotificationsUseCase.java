package com.planningpoker.notification.application.port.in;

import com.planningpoker.notification.domain.Notification;

import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving a user's notifications with pagination.
 */
public interface GetNotificationsUseCase {

    List<Notification> getNotifications(UUID userId, int offset, int limit);

    long getTotalCount(UUID userId);
}
