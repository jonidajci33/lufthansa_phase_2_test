package com.planningpoker.notification.infrastructure.persistence;

import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPersistenceMapperTest {

    // ── toDomain ─────────────────────────────────────────────────────

    @Test
    void toDomain_mapsAllFields() {
        NotificationJpaEntity entity = notificationEntity();

        Notification result = NotificationPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getUserId()).isEqualTo(entity.getUserId());
        assertThat(result.getType()).isEqualTo(entity.getType());
        assertThat(result.getTitle()).isEqualTo(entity.getTitle());
        assertThat(result.getMessage()).isEqualTo(entity.getMessage());
        assertThat(result.isRead()).isEqualTo(entity.isRead());
        assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void toDomain_returnsNullForNullInput() {
        assertThat(NotificationPersistenceMapper.toDomain(null)).isNull();
    }

    // ── toEntity ─────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        Notification notification = domainNotification();

        NotificationJpaEntity result = NotificationPersistenceMapper.toEntity(notification);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(notification.getId());
        assertThat(result.getUserId()).isEqualTo(notification.getUserId());
        assertThat(result.getType()).isEqualTo(notification.getType());
        assertThat(result.getTitle()).isEqualTo(notification.getTitle());
        assertThat(result.getMessage()).isEqualTo(notification.getMessage());
        assertThat(result.isRead()).isEqualTo(notification.isRead());
        assertThat(result.getCreatedAt()).isEqualTo(notification.getCreatedAt());
    }

    @Test
    void toEntity_returnsNullForNullInput() {
        assertThat(NotificationPersistenceMapper.toEntity(null)).isNull();
    }

    // ── Round-trip ───────────────────────────────────────────────────

    @Test
    void roundTrip_domainToEntityAndBack() {
        Notification original = domainNotification();

        NotificationJpaEntity entity = NotificationPersistenceMapper.toEntity(original);
        Notification roundTripped = NotificationPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getUserId()).isEqualTo(original.getUserId());
        assertThat(roundTripped.getType()).isEqualTo(original.getType());
        assertThat(roundTripped.getTitle()).isEqualTo(original.getTitle());
        assertThat(roundTripped.getMessage()).isEqualTo(original.getMessage());
        assertThat(roundTripped.isRead()).isEqualTo(original.isRead());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    // ── List variants ────────────────────────────────────────────────

    @Test
    void toDomainList_mapsAllElements() {
        List<NotificationJpaEntity> entities = List.of(notificationEntity(), notificationEntity());

        List<Notification> result = NotificationPersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    @Test
    void toDomainList_returnsEmptyForNull() {
        assertThat(NotificationPersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toDomainList_returnsEmptyForEmptyList() {
        assertThat(NotificationPersistenceMapper.toDomainList(Collections.emptyList())).isEmpty();
    }

    @Test
    void toEntityList_mapsAllElements() {
        List<Notification> notifications = List.of(domainNotification(), domainNotification());

        List<NotificationJpaEntity> result = NotificationPersistenceMapper.toEntityList(notifications);

        assertThat(result).hasSize(2);
    }

    @Test
    void toEntityList_returnsEmptyForNull() {
        assertThat(NotificationPersistenceMapper.toEntityList(null)).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static NotificationJpaEntity notificationEntity() {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setType(NotificationType.INVITATION);
        entity.setTitle("You have been invited");
        entity.setMessage("Join the planning poker session");
        entity.setRead(false);
        entity.setCreatedAt(Instant.parse("2026-01-15T10:00:00Z"));
        return entity;
    }

    private static Notification domainNotification() {
        return new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationType.VOTING_FINISHED,
                "Voting finished",
                "The voting round has completed",
                null,
                true,
                Instant.parse("2026-01-15T10:00:00Z")
        );
    }
}
