package com.planningpoker.room.infrastructure.messaging;

import com.planningpoker.room.application.port.out.RoomEventPublisherPort;
import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
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
 * Kafka-based implementation of {@link RoomEventPublisherPort}.
 * Publishes domain events to the {@code room.events} topic.
 */
@Component
public class RoomEventKafkaPublisher implements RoomEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RoomEventKafkaPublisher.class);
    private static final String SOURCE = "room-service";

    private final KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    public RoomEventKafkaPublisher(KafkaTemplate<String, DomainEvent<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishRoomCreated(Room room) {
        var payload = new RoomCreatedPayload(
                room.getId(),
                room.getName(),
                room.getModeratorId(),
                room.getShortCode(),
                room.getMaxParticipants()
        );

        DomainEvent<RoomCreatedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.ROOM_CREATED,
                payload,
                CorrelationIdUtil.current()
        );

        send(room.getId().toString(), event);
        log.info("Published ROOM_CREATED event for room={}", room.getId());
    }

    @Override
    public void publishRoomUpdated(Room room) {
        var payload = new RoomUpdatedPayload(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getStatus().name(),
                room.getMaxParticipants()
        );

        DomainEvent<RoomUpdatedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.ROOM_UPDATED,
                payload,
                CorrelationIdUtil.current()
        );

        send(room.getId().toString(), event);
        log.info("Published ROOM_UPDATED event for room={}", room.getId());
    }

    @Override
    public void publishRoomDeleted(Room room) {
        var payload = new RoomDeletedPayload(
                room.getId(),
                room.getName()
        );

        DomainEvent<RoomDeletedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.ROOM_DELETED,
                payload,
                CorrelationIdUtil.current()
        );

        send(room.getId().toString(), event);
        log.info("Published ROOM_DELETED event for room={}", room.getId());
    }

    @Override
    public void publishUserInvited(Invitation invitation, String roomName) {
        var payload = new UserInvitedPayload(
                invitation.getId(),
                invitation.getRoomId(),
                invitation.getInvitedBy(),
                invitation.getEmail(),
                invitation.getToken(),
                invitation.getType().name(),
                roomName
        );

        DomainEvent<UserInvitedPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.USER_INVITED,
                payload,
                CorrelationIdUtil.current()
        );

        send(invitation.getRoomId().toString(), event);
        log.info("Published USER_INVITED event for invitation={} in room={}", invitation.getId(), invitation.getRoomId());
    }

    @Override
    public void publishUserJoinedRoom(Room room, RoomParticipant participant) {
        var payload = new UserJoinedRoomPayload(
                room.getId(),
                participant.getUserId(),
                participant.getRole().name()
        );

        DomainEvent<UserJoinedRoomPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.USER_JOINED_ROOM,
                payload,
                CorrelationIdUtil.current()
        );

        send(room.getId().toString(), event);
        log.info("Published USER_JOINED_ROOM event for user={} in room={}", participant.getUserId(), room.getId());
    }

    @Override
    public void publishUserLeftRoom(Room room, RoomParticipant participant) {
        var payload = new UserLeftRoomPayload(
                room.getId(),
                participant.getUserId(),
                participant.getRole().name()
        );

        DomainEvent<UserLeftRoomPayload> event = DomainEvent.create(
                SOURCE,
                EventTypes.USER_LEFT_ROOM,
                payload,
                CorrelationIdUtil.current()
        );

        send(room.getId().toString(), event);
        log.info("Published USER_LEFT_ROOM event for user={} in room={}", participant.getUserId(), room.getId());
    }

    private void send(String key, DomainEvent<?> event) {
        kafkaTemplate.send(Topics.ROOM_EVENTS, key, event);
    }

    // ── Event payload DTOs ────────────────────────────────────────────

    public record RoomCreatedPayload(
            UUID roomId,
            String name,
            UUID moderatorId,
            String shortCode,
            int maxParticipants
    ) {}

    public record RoomUpdatedPayload(
            UUID roomId,
            String name,
            String description,
            String status,
            int maxParticipants
    ) {}

    public record RoomDeletedPayload(
            UUID roomId,
            String name
    ) {}

    public record UserInvitedPayload(
            UUID invitationId,
            UUID roomId,
            UUID invitedBy,
            String email,
            String token,
            String type,
            String roomName
    ) {}

    public record UserJoinedRoomPayload(
            UUID roomId,
            UUID userId,
            String role
    ) {}

    public record UserLeftRoomPayload(
            UUID roomId,
            UUID userId,
            String role
    ) {}
}
