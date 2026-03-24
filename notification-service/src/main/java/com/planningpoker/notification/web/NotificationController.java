package com.planningpoker.notification.web;

import com.planningpoker.notification.application.port.in.GetNotificationsUseCase;
import com.planningpoker.notification.application.port.in.MarkReadUseCase;
import com.planningpoker.notification.application.port.in.UnreadCountUseCase;
import com.planningpoker.notification.web.dto.NotificationResponse;
import com.planningpoker.notification.web.dto.UnreadCountResponse;
import com.planningpoker.notification.web.mapper.NotificationRestMapper;
import com.planningpoker.shared.error.PageResponse;
import com.planningpoker.shared.security.JwtClaimExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkReadUseCase markReadUseCase;
    private final UnreadCountUseCase unreadCountUseCase;

    public NotificationController(GetNotificationsUseCase getNotificationsUseCase,
                                  MarkReadUseCase markReadUseCase,
                                  UnreadCountUseCase unreadCountUseCase) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.markReadUseCase = markReadUseCase;
        this.unreadCountUseCase = unreadCountUseCase;
    }

    @GetMapping
    @Operation(summary = "List notifications", description = "Returns a paginated list of the current user's notifications.")
    @ApiResponse(responseCode = "200", description = "Paginated list of notifications")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        UUID userId = UUID.fromString(JwtClaimExtractor.currentUserId());

        List<NotificationResponse> data = NotificationRestMapper.toResponseList(
                getNotificationsUseCase.getNotifications(userId, offset, limit));

        long total = getNotificationsUseCase.getTotalCount(userId);

        return ResponseEntity.ok(PageResponse.of(data, total, limit, offset));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read.")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        markReadUseCase.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all of the current user's notifications as read.")
    @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<Void> markAllAsRead() {
        UUID userId = UUID.fromString(JwtClaimExtractor.currentUserId());
        markReadUseCase.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Returns the number of unread notifications for the current user.")
    @ApiResponse(responseCode = "200", description = "Unread notification count")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UnreadCountResponse> unreadCount() {
        UUID userId = UUID.fromString(JwtClaimExtractor.currentUserId());
        long count = unreadCountUseCase.getUnreadCount(userId);
        return ResponseEntity.ok(UnreadCountResponse.of(count));
    }
}
