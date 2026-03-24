package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.RoomParticipant;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link RoomParticipant} domain objects and {@link RoomParticipantJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, no instantiation.
 */
public final class RoomParticipantPersistenceMapper {

    private RoomParticipantPersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link RoomParticipantJpaEntity} to a {@link RoomParticipant} domain object.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain object, or null if the entity is null
     */
    public static RoomParticipant toDomain(RoomParticipantJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new RoomParticipant(
                entity.getId(),
                entity.getRoom().getId(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getRole(),
                entity.getJoinedAt(),
                entity.getLeftAt(),
                entity.isConnected()
        );
    }

    /**
     * Converts a list of {@link RoomParticipantJpaEntity} to a list of {@link RoomParticipant} domain objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain objects, never null
     */
    public static List<RoomParticipant> toDomainList(List<RoomParticipantJpaEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(RoomParticipantPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a {@link RoomParticipant} domain object to a {@link RoomParticipantJpaEntity}.
     *
     * @param participant the domain object (may be null)
     * @param parentRoom  the parent room entity (required for the FK relationship)
     * @return the JPA entity, or null if the domain object is null
     */
    public static RoomParticipantJpaEntity toEntity(RoomParticipant participant, RoomJpaEntity parentRoom) {
        if (participant == null) {
            return null;
        }
        RoomParticipantJpaEntity entity = new RoomParticipantJpaEntity();
        entity.setId(participant.getId());
        entity.setRoom(parentRoom);
        entity.setUserId(participant.getUserId());
        entity.setUsername(participant.getUsername());
        entity.setRole(participant.getRole());
        entity.setJoinedAt(participant.getJoinedAt());
        entity.setLeftAt(participant.getLeftAt());
        entity.setConnected(participant.isConnected());
        return entity;
    }

    /**
     * Converts a list of {@link RoomParticipant} domain objects to a list of {@link RoomParticipantJpaEntity}.
     *
     * @param participants the domain objects (may be null)
     * @param parentRoom   the parent room entity
     * @return a mutable list of JPA entities, never null
     */
    public static List<RoomParticipantJpaEntity> toEntityList(List<RoomParticipant> participants, RoomJpaEntity parentRoom) {
        if (participants == null) {
            return Collections.emptyList();
        }
        return participants.stream()
                .map(p -> toEntity(p, parentRoom))
                .toList();
    }
}
