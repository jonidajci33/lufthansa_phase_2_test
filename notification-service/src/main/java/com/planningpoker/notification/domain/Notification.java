package com.planningpoker.notification.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an in-app notification for a user.
 */
public class Notification {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private String metadata;
    private boolean isRead;
    private Instant createdAt;

    public Notification() {
    }

    public Notification(UUID id, UUID userId, NotificationType type, String title,
                        String message, String metadata, boolean isRead, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    /**
     * Marks this notification as read. Idempotent — calling on an already-read
     * notification is a no-op.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    // ── Factory ────────────────────────────────────────────────────────

    public static Notification create(UUID userId, NotificationType type, String title, String message) {
        return new Notification(
                UUID.randomUUID(),
                userId,
                type,
                title,
                message,
                null,
                false,
                Instant.now()
        );
    }

    public static Notification createWithMetadata(UUID userId, NotificationType type, String title,
                                                   String message, String metadata) {
        return new Notification(
                UUID.randomUUID(),
                userId,
                type,
                title,
                message,
                metadata,
                false,
                Instant.now()
        );
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
