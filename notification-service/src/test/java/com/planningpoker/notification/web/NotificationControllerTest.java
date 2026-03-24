package com.planningpoker.notification.web;

import com.planningpoker.notification.application.port.in.GetNotificationsUseCase;
import com.planningpoker.notification.application.port.in.MarkReadUseCase;
import com.planningpoker.notification.application.port.in.UnreadCountUseCase;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import com.planningpoker.notification.infrastructure.config.SecurityConfig;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetNotificationsUseCase getNotificationsUseCase;

    @MockitoBean
    private MarkReadUseCase markReadUseCase;

    @MockitoBean
    private UnreadCountUseCase unreadCountUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final String USER_SUB = "00000000-0000-0000-0000-000000000001";
    private static final UUID USER_ID = UUID.fromString(USER_SUB);
    private static final UUID NOTIFICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    private static Notification sampleNotification() {
        return new Notification(
                NOTIFICATION_ID,
                USER_ID,
                NotificationType.WELCOME,
                "Welcome!",
                "Welcome to Planning Poker",
                null,
                false,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    // ── GET /api/v1/notifications ─────────────────────────────────────

    @Test
    void shouldListNotificationsWithPagination() throws Exception {
        Notification notification = sampleNotification();
        given(getNotificationsUseCase.getNotifications(USER_ID, 0, 20))
                .willReturn(List.of(notification));
        given(getNotificationsUseCase.getTotalCount(USER_ID)).willReturn(1L);

        mockMvc.perform(get("/api/v1/notifications")
                        .param("offset", "0")
                        .param("limit", "20")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(NOTIFICATION_ID.toString())))
                .andExpect(jsonPath("$.data[0].title", is("Welcome!")))
                .andExpect(jsonPath("$.data[0].type", is("WELCOME")))
                .andExpect(jsonPath("$.data[0].isRead", is(false)))
                .andExpect(jsonPath("$.meta.total", is(1)))
                .andExpect(jsonPath("$.meta.limit", is(20)))
                .andExpect(jsonPath("$.meta.offset", is(0)))
                .andExpect(jsonPath("$.meta.hasNext", is(false)));
    }

    // ── PUT /api/v1/notifications/{id}/read ───────────────────────────

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        willDoNothing().given(markReadUseCase).markAsRead(NOTIFICATION_ID);

        mockMvc.perform(put("/api/v1/notifications/{id}/read", NOTIFICATION_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenMarkingNonExistentNotification() throws Exception {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        willThrow(new ResourceNotFoundException("Notification", missingId))
                .given(markReadUseCase).markAsRead(missingId);

        mockMvc.perform(put("/api/v1/notifications/{id}/read", missingId)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOTIFICATION_NOT_FOUND")));
    }

    // ── PUT /api/v1/notifications/read-all ────────────────────────────

    @Test
    void shouldMarkAllNotificationsAsRead() throws Exception {
        willDoNothing().given(markReadUseCase).markAllAsRead(USER_ID);

        mockMvc.perform(put("/api/v1/notifications/read-all")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk());
    }

    // ── GET /api/v1/notifications/unread-count ────────────────────────

    @Test
    void shouldReturnUnreadCount() throws Exception {
        given(unreadCountUseCase.getUnreadCount(USER_ID)).willReturn(7L);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(7)));
    }

    // ── 401 Unauthorized ──────────────────────────────────────────────

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
