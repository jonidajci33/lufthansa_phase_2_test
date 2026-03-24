package com.planningpoker.identity.infrastructure.persistence;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;

import java.util.EnumSet;
import java.util.List;

/**
 * Maps between {@link UserJpaEntity} (infrastructure) and {@link User} (domain).
 * Pure static utility -- no Spring annotations, no state.
 */
public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
        // utility class -- prevent instantiation
    }

    /**
     * Converts a JPA entity to a domain User.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain User, or null if entity is null
     */
    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getKeycloakId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDisplayName(),
                entity.getAvatarUrl(),
                entity.isActive(),
                entity.getRoles() != null && !entity.getRoles().isEmpty()
                        ? EnumSet.copyOf(entity.getRoles())
                        : EnumSet.noneOf(UserRole.class),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converts a domain User to a JPA entity.
     *
     * @param user the domain User (may be null)
     * @return the JPA entity, or null if user is null
     */
    public static UserJpaEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setKeycloakId(user.getKeycloakId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setDisplayName(user.getDisplayName());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setActive(user.isActive());
        entity.setRoles(user.getRoles().isEmpty()
                ? EnumSet.noneOf(UserRole.class)
                : EnumSet.copyOf(user.getRoles()));
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    /**
     * Converts a list of JPA entities to domain Users.
     *
     * @param entities the JPA entities (may be null or empty)
     * @return list of domain Users, never null
     */
    public static List<User> toDomainList(List<UserJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream()
                .map(UserPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a list of domain Users to JPA entities.
     *
     * @param users the domain Users (may be null or empty)
     * @return list of JPA entities, never null
     */
    public static List<UserJpaEntity> toEntityList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        return users.stream()
                .map(UserPersistenceMapper::toEntity)
                .toList();
    }
}
