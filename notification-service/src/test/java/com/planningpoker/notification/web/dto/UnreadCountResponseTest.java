package com.planningpoker.notification.web.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnreadCountResponseTest {

    @Test
    void shouldCreateViaConstructor() {
        UnreadCountResponse response = new UnreadCountResponse(5);

        assertThat(response.count()).isEqualTo(5);
    }

    @Test
    void shouldCreateViaFactoryMethod() {
        UnreadCountResponse response = UnreadCountResponse.of(42);

        assertThat(response.count()).isEqualTo(42);
    }

    @Test
    void shouldHandleZeroCount() {
        UnreadCountResponse response = UnreadCountResponse.of(0);

        assertThat(response.count()).isZero();
    }
}
