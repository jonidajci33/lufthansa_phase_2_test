package com.planningpoker.notification.web.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationResponseTest {

    @Test
    void shouldCreateResponseWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-18T10:00:00Z");

        NotificationResponse response = new NotificationResponse(
                id, userId, "WELCOME", "Welcome!",
                "Welcome to the app", null, false, createdAt
        );

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.type()).isEqualTo("WELCOME");
        assertThat(response.title()).isEqualTo("Welcome!");
        assertThat(response.message()).isEqualTo("Welcome to the app");
        assertThat(response.metadata()).isNull();
        assertThat(response.isRead()).isFalse();
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldHandleReadNotification() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse(
                id, userId, "VOTING_STARTED", "Voting Started",
                "Voting started on story", "{\"roomId\":\"123\"}", true, Instant.now()
        );

        assertThat(response.isRead()).isTrue();
        assertThat(response.metadata()).isEqualTo("{\"roomId\":\"123\"}");
    }
}
