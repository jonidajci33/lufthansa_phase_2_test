package com.planningpoker.notification.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for notification data.
 */
public record NotificationResponse(
        UUID id,
        UUID userId,
        String type,
        String title,
        String message,
        String metadata,
        boolean isRead,
        Instant createdAt
) {
}
