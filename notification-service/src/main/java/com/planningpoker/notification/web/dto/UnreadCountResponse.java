package com.planningpoker.notification.web.dto;

/**
 * Response DTO for unread notification count.
 */
public record UnreadCountResponse(
        long count
) {

    public static UnreadCountResponse of(long count) {
        return new UnreadCountResponse(count);
    }
}
