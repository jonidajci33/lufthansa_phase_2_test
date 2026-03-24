package com.planningpoker.identity.infrastructure.messaging;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.infrastructure.messaging.UserEventKafkaPublisher.UserDeactivatedPayload;
import com.planningpoker.identity.infrastructure.messaging.UserEventKafkaPublisher.UserRegisteredPayload;
import com.planningpoker.identity.infrastructure.messaging.UserEventKafkaPublisher.UserUpdatedPayload;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.EventTypes;
import com.planningpoker.shared.event.Topics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    @InjectMocks
    private UserEventKafkaPublisher publisher;

    @Captor
    private ArgumentCaptor<DomainEvent<?>> eventCaptor;

    // ── Helpers ───────────────────────────────────────────────────────

    private static User sampleUser() {
        Instant now = Instant.now();
        return new User(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "kc-abc-123",
                "johndoe",
                "john@example.com",
                "John",
                "Doe",
                "John Doe",
                "https://example.com/avatar.png",
                true,
                EnumSet.of(UserRole.PARTICIPANT),
                now,
                now
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishUserRegistered
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishUserRegisteredEvent() {
        User user = sampleUser();

        publisher.publishUserRegistered(user);

        verify(kafkaTemplate).send(
                eq(Topics.IDENTITY_USER_EVENTS),
                eq(user.getId().toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.USER_REGISTERED);
        assertThat(event.source()).isEqualTo("identity-service");
        assertThat(event.specVersion()).isEqualTo("1.0");
        assertThat(event.dataContentType()).isEqualTo("application/json");
        assertThat(event.id()).isNotBlank();
        assertThat(event.time()).isNotNull();

        assertThat(event.data()).isInstanceOf(UserRegisteredPayload.class);
        UserRegisteredPayload payload = (UserRegisteredPayload) event.data();
        assertThat(payload.userId()).isEqualTo(user.getId());
        assertThat(payload.username()).isEqualTo("johndoe");
        assertThat(payload.email()).isEqualTo("john@example.com");
        assertThat(payload.displayName()).isEqualTo("John Doe");
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishUserUpdated
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishUserUpdatedEvent() {
        User user = sampleUser();

        publisher.publishUserUpdated(user);

        verify(kafkaTemplate).send(
                eq(Topics.IDENTITY_USER_EVENTS),
                eq(user.getId().toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.USER_UPDATED);
        assertThat(event.source()).isEqualTo("identity-service");

        assertThat(event.data()).isInstanceOf(UserUpdatedPayload.class);
        UserUpdatedPayload payload = (UserUpdatedPayload) event.data();
        assertThat(payload.userId()).isEqualTo(user.getId());
        assertThat(payload.displayName()).isEqualTo("John Doe");
        assertThat(payload.avatarUrl()).isEqualTo("https://example.com/avatar.png");
    }

    // ═══════════════════════════════════════════════════════════════════
    // publishUserDeactivated
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldPublishUserDeactivatedEvent() {
        User user = sampleUser();

        publisher.publishUserDeactivated(user);

        verify(kafkaTemplate).send(
                eq(Topics.IDENTITY_USER_EVENTS),
                eq(user.getId().toString()),
                eventCaptor.capture()
        );

        DomainEvent<?> event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(EventTypes.USER_DEACTIVATED);
        assertThat(event.source()).isEqualTo("identity-service");

        assertThat(event.data()).isInstanceOf(UserDeactivatedPayload.class);
        UserDeactivatedPayload payload = (UserDeactivatedPayload) event.data();
        assertThat(payload.userId()).isEqualTo(user.getId());
        assertThat(payload.username()).isEqualTo("johndoe");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Payload record tests (for additional coverage)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCreateUserRegisteredPayloadWithAccessors() {
        UUID id = UUID.randomUUID();
        UserRegisteredPayload payload = new UserRegisteredPayload(id, "user1", "user1@test.com", "User One");

        assertThat(payload.userId()).isEqualTo(id);
        assertThat(payload.username()).isEqualTo("user1");
        assertThat(payload.email()).isEqualTo("user1@test.com");
        assertThat(payload.displayName()).isEqualTo("User One");
    }

    @Test
    void shouldCreateUserUpdatedPayloadWithAccessors() {
        UUID id = UUID.randomUUID();
        UserUpdatedPayload payload = new UserUpdatedPayload(id, "Updated Name", "https://avatar.com/new.png");

        assertThat(payload.userId()).isEqualTo(id);
        assertThat(payload.displayName()).isEqualTo("Updated Name");
        assertThat(payload.avatarUrl()).isEqualTo("https://avatar.com/new.png");
    }

    @Test
    void shouldCreateUserDeactivatedPayloadWithAccessors() {
        UUID id = UUID.randomUUID();
        UserDeactivatedPayload payload = new UserDeactivatedPayload(id, "deactivated-user");

        assertThat(payload.userId()).isEqualTo(id);
        assertThat(payload.username()).isEqualTo("deactivated-user");
    }
}
