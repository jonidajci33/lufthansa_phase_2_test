package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.Invitation;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link Invitation} domain objects and {@link InvitationJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, no instantiation.
 */
public final class InvitationPersistenceMapper {

    private InvitationPersistenceMapper() {
        // utility class
    }

    /**
     * Converts an {@link InvitationJpaEntity} to an {@link Invitation} domain object.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain object, or null if the entity is null
     */
    public static Invitation toDomain(InvitationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Invitation(
                entity.getId(),
                entity.getRoomId(),
                entity.getInvitedBy(),
                entity.getEmail(),
                entity.getToken(),
                entity.getType(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getAcceptedAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Converts a list of {@link InvitationJpaEntity} to a list of {@link Invitation} domain objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain objects, never null
     */
    public static List<Invitation> toDomainList(List<InvitationJpaEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(InvitationPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts an {@link Invitation} domain object to an {@link InvitationJpaEntity}.
     *
     * @param invitation the domain object (may be null)
     * @return the JPA entity, or null if the domain object is null
     */
    public static InvitationJpaEntity toEntity(Invitation invitation) {
        if (invitation == null) {
            return null;
        }
        InvitationJpaEntity entity = new InvitationJpaEntity();
        entity.setId(invitation.getId());
        entity.setRoomId(invitation.getRoomId());
        entity.setInvitedBy(invitation.getInvitedBy());
        entity.setEmail(invitation.getEmail());
        entity.setToken(invitation.getToken());
        entity.setType(invitation.getType());
        entity.setStatus(invitation.getStatus());
        entity.setExpiresAt(invitation.getExpiresAt());
        entity.setAcceptedAt(invitation.getAcceptedAt());
        entity.setCreatedAt(invitation.getCreatedAt());
        return entity;
    }
}
