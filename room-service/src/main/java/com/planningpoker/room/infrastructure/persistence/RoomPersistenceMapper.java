package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;

import java.util.ArrayList;
import java.util.List;

/**
 * Static mapper between {@link Room} domain objects and {@link RoomJpaEntity} persistence entities.
 * <p>
 * Delegates to {@link DeckTypePersistenceMapper} for nested deck types
 * and {@link RoomParticipantPersistenceMapper} for nested participants.
 * Pure utility class — no framework imports, no instantiation.
 */
public final class RoomPersistenceMapper {

    private RoomPersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link RoomJpaEntity} to a {@link Room} domain object.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain object, or null if the entity is null
     */
    public static Room toDomain(RoomJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<RoomParticipant> domainParticipants =
                RoomParticipantPersistenceMapper.toDomainList(entity.getParticipants());

        return new Room(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getModeratorId(),
                DeckTypePersistenceMapper.toDomain(entity.getDeckType()),
                entity.getShortCode(),
                entity.getStatus(),
                entity.getMaxParticipants(),
                domainParticipants,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converts a {@link Room} domain object to a {@link RoomJpaEntity}.
     *
     * @param room the domain object (may be null)
     * @return the JPA entity, or null if the domain object is null
     */
    public static RoomJpaEntity toEntity(Room room) {
        if (room == null) {
            return null;
        }
        RoomJpaEntity entity = new RoomJpaEntity();
        entity.setId(room.getId());
        entity.setName(room.getName());
        entity.setDescription(room.getDescription());
        entity.setModeratorId(room.getModeratorId());
        entity.setShortCode(room.getShortCode());
        entity.setStatus(room.getStatus());
        entity.setMaxParticipants(room.getMaxParticipants());
        entity.setCreatedAt(room.getCreatedAt());
        entity.setUpdatedAt(room.getUpdatedAt());

        if (room.getDeckType() != null) {
            entity.setDeckType(DeckTypePersistenceMapper.toEntity(room.getDeckType()));
        }

        List<RoomParticipantJpaEntity> participantEntities = new ArrayList<>();
        for (RoomParticipant participant : room.getParticipants()) {
            participantEntities.add(RoomParticipantPersistenceMapper.toEntity(participant, entity));
        }
        entity.setParticipants(participantEntities);

        return entity;
    }
}
