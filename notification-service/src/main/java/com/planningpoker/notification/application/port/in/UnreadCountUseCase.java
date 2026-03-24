package com.planningpoker.notification.application.port.in;

import java.util.UUID;

/**
 * Use case for getting the number of unread notifications for a user.
 */
public interface UnreadCountUseCase {

    long getUnreadCount(UUID userId);
}
