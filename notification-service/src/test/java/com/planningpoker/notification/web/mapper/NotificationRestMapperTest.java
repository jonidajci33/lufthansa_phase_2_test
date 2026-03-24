package com.planningpoker.notification.web.mapper;

import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import com.planningpoker.notification.web.dto.NotificationResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRestMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        Notification notification = new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationType.INVITATION,
                "You have been invited",
                "Join the planning poker session",
                null,
                false,
                Instant.parse("2026-01-15T10:00:00Z")
        );

        NotificationResponse result = NotificationRestMapper.toResponse(notification);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(notification.getId());
        assertThat(result.userId()).isEqualTo(notification.getUserId());
        assertThat(result.type()).isEqualTo("INVITATION");
        assertThat(result.title()).isEqualTo("You have been invited");
        assertThat(result.message()).isEqualTo("Join the planning poker session");
        assertThat(result.isRead()).isFalse();
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2026-01-15T10:00:00Z"));
    }

    @Test
    void toResponse_returnsNullForNullInput() {
        assertThat(NotificationRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_convertsTypeEnumToString() {
        Notification notification = Notification.create(
                UUID.randomUUID(), NotificationType.WELCOME, "Welcome", "Hello");

        NotificationResponse result = NotificationRestMapper.toResponse(notification);

        assertThat(result.type()).isEqualTo("WELCOME");
    }

    @Test
    void toResponseList_mapsAllElements() {
        Notification n1 = Notification.create(UUID.randomUUID(), NotificationType.WELCOME, "Title 1", "Msg 1");
        Notification n2 = Notification.create(UUID.randomUUID(), NotificationType.SYSTEM, "Title 2", "Msg 2");

        List<NotificationResponse> result = NotificationRestMapper.toResponseList(List.of(n1, n2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Title 1");
        assertThat(result.get(1).title()).isEqualTo("Title 2");
    }

    @Test
    void toResponseList_returnsEmptyForNull() {
        assertThat(NotificationRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_returnsEmptyForEmptyList() {
        assertThat(NotificationRestMapper.toResponseList(Collections.emptyList())).isEmpty();
    }
}
