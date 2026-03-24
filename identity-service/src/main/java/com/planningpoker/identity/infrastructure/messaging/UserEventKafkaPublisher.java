package com.planningpoker.identity.infrastructure.messaging;

import com.planningpoker.identity.application.port.out.UserEventPublisherPort;
import com.planningpoker.identity.domain.User;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.EventTypes;
import com.planningpoker.shared.event.Topics;
import com.planningpoker.shared.observability.CorrelationIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka-based implementation of {@link UserEventPublisherPort}.
 * Publishes domain events to the {@code identity.user.events} topic.
 */
@Component
public class UserEventKafkaPublisher implements UserEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(UserEventKafkaPublisher.class);
    private static final String SOURCE = "identity-service";

    private final KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    public UserEventKafkaPublisher(KafkaTemplate<String, DomainEvent<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishUserRegistered(User user) {
        var payload = new UserRegisteredPayload(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName()
        );

        DomainEvent<UserRegisteredPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.USER_REGISTERED,
                payload,
                CorrelationIdUtil.current()
        );

        send(user.getId().toString(), event);
        log.info("Published USER_REGISTERED event for user={}", user.getId());
    }

    @Override
    public void publishUserUpdated(User user) {
        var payload = new UserUpdatedPayload(
                user.getId(),
                user.getDisplayName(),
                user.getAvatarUrl()
        );

        DomainEvent<UserUpdatedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.USER_UPDATED,
                payload,
                CorrelationIdUtil.current()
        );

        send(user.getId().toString(), event);
        log.info("Published USER_UPDATED event for user={}", user.getId());
    }

    @Override
    public void publishUserDeactivated(User user) {
        var payload = new UserDeactivatedPayload(
                user.getId(),
                user.getUsername()
        );

        DomainEvent<UserDeactivatedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.USER_DEACTIVATED,
                payload,
                CorrelationIdUtil.current()
        );

        send(user.getId().toString(), event);
        log.info("Published USER_DEACTIVATED event for user={}", user.getId());
    }

    private void send(String key, DomainEvent<?> event) {
        kafkaTemplate.send(Topics.IDENTITY_USER_EVENTS, key, event);
    }

    // ── Event payload DTOs ────────────────────────────────────────────

    public record UserRegisteredPayload(
            UUID userId,
            String username,
            String email,
            String displayName
    ) {}

    public record UserUpdatedPayload(
            UUID userId,
            String displayName,
            String avatarUrl
    ) {}

    public record UserDeactivatedPayload(
            UUID userId,
            String username
    ) {}
}
