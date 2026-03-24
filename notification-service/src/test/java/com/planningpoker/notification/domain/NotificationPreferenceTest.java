package com.planningpoker.notification.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPreferenceTest {

    @Test
    void shouldCreatePreferenceViaConstructor() {
        UUID userId = UUID.randomUUID();

        NotificationPreference pref = new NotificationPreference(userId, "EMAIL", true);

        assertThat(pref.getUserId()).isEqualTo(userId);
        assertThat(pref.getChannel()).isEqualTo("EMAIL");
        assertThat(pref.isEnabled()).isTrue();
    }

    @Test
    void shouldCreateDisabledPreference() {
        UUID userId = UUID.randomUUID();

        NotificationPreference pref = new NotificationPreference(userId, "IN_APP", false);

        assertThat(pref.getChannel()).isEqualTo("IN_APP");
        assertThat(pref.isEnabled()).isFalse();
    }

    @Test
    void shouldAllowDefaultConstructor() {
        NotificationPreference pref = new NotificationPreference();

        assertThat(pref.getUserId()).isNull();
        assertThat(pref.getChannel()).isNull();
        assertThat(pref.isEnabled()).isFalse();
    }

    @Test
    void shouldSupportSetters() {
        NotificationPreference pref = new NotificationPreference();
        UUID userId = UUID.randomUUID();

        pref.setUserId(userId);
        pref.setChannel("EMAIL");
        pref.setEnabled(true);

        assertThat(pref.getUserId()).isEqualTo(userId);
        assertThat(pref.getChannel()).isEqualTo("EMAIL");
        assertThat(pref.isEnabled()).isTrue();
    }

    @Test
    void shouldToggleEnabled() {
        UUID userId = UUID.randomUUID();
        NotificationPreference pref = new NotificationPreference(userId, "EMAIL", true);

        pref.setEnabled(false);

        assertThat(pref.isEnabled()).isFalse();
    }
}
