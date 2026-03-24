package com.planningpoker.notification.domain;

import java.util.UUID;

/**
 * Domain entity representing a user's notification channel preference.
 */
public class NotificationPreference {

    private UUID userId;
    private String channel; // EMAIL, IN_APP
    private boolean enabled;

    public NotificationPreference() {
    }

    public NotificationPreference(UUID userId, String channel, boolean enabled) {
        this.userId = userId;
        this.channel = channel;
        this.enabled = enabled;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
