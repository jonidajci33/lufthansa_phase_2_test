package com.planningpoker.notification.infrastructure.persistence;

import com.planningpoker.notification.domain.Notification;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link Notification} domain objects and {@link NotificationJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class NotificationPersistenceMapper {

    private NotificationPersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link NotificationJpaEntity} to a domain {@link Notification}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain notification, or {@code null} if the input is null
     */
    public static Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Notification(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getMetadata(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }

    /**
     * Converts a domain {@link Notification} to a {@link NotificationJpaEntity}.
     *
     * @param notification the domain notification (may be null)
     * @return the JPA entity, or {@code null} if the input is null
     */
    public static NotificationJpaEntity toEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId());
        entity.setType(notification.getType());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setMetadata(notification.getMetadata());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        return entity;
    }

    /**
     * Converts a list of {@link NotificationJpaEntity} to domain {@link Notification} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain notifications; empty list if input is null or empty
     */
    public static List<Notification> toDomainList(List<NotificationJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(NotificationPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a list of domain {@link Notification} objects to {@link NotificationJpaEntity} instances.
     *
     * @param notifications the domain notifications (may be null)
     * @return an unmodifiable list of JPA entities; empty list if input is null or empty
     */
    public static List<NotificationJpaEntity> toEntityList(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }
        return notifications.stream()
                .map(NotificationPersistenceMapper::toEntity)
                .toList();
    }
}
