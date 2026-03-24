package com.planningpoker.notification.web.mapper;

import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.web.dto.NotificationResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from domain {@link Notification} to {@link NotificationResponse} DTO.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class NotificationRestMapper {

    private NotificationRestMapper() {
        // utility class
    }

    /**
     * Converts a domain {@link Notification} to a {@link NotificationResponse} DTO.
     *
     * @param notification the domain notification (may be null)
     * @return the response DTO, or {@code null} if the input is null
     */
    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getMetadata(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    /**
     * Converts a list of domain {@link Notification} objects to {@link NotificationResponse} DTOs.
     *
     * @param notifications the domain notifications (may be null)
     * @return an unmodifiable list of response DTOs; empty list if input is null or empty
     */
    public static List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }
        return notifications.stream()
                .map(NotificationRestMapper::toResponse)
                .toList();
    }
}
