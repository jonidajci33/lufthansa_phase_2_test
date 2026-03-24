package com.planningpoker.identity.infrastructure.persistence;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String KEYCLOAK_ID = "kc-id-123";
    private static final String USERNAME = "johndoe";
    private static final String EMAIL = "john@example.com";
    private static final String DISPLAY_NAME = "John Doe";
    private static final String AVATAR_URL = "https://example.com/avatar.png";
    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-03-01T14:30:00Z");

    @Test
    void toDomain_mapsAllFields() {
        UserJpaEntity entity = buildEntity();
        User user = UserPersistenceMapper.toDomain(entity);

        assertThat(user.getId()).isEqualTo(ID);
        assertThat(user.getKeycloakId()).isEqualTo(KEYCLOAK_ID);
        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(user.getAvatarUrl()).isEqualTo(AVATAR_URL);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.PARTICIPANT, UserRole.ADMIN);
        assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void toEntity_mapsAllFields() {
        User user = buildDomainUser();
        UserJpaEntity entity = UserPersistenceMapper.toEntity(user);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getKeycloakId()).isEqualTo(KEYCLOAK_ID);
        assertThat(entity.getUsername()).isEqualTo(USERNAME);
        assertThat(entity.getEmail()).isEqualTo(EMAIL);
        assertThat(entity.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(entity.getAvatarUrl()).isEqualTo(AVATAR_URL);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getRoles()).containsExactlyInAnyOrder(UserRole.PARTICIPANT, UserRole.ADMIN);
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void roundTrip_preservesAllFields() {
        User original = buildDomainUser();
        User roundTripped = UserPersistenceMapper.toDomain(
                UserPersistenceMapper.toEntity(original));

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getKeycloakId()).isEqualTo(original.getKeycloakId());
        assertThat(roundTripped.getUsername()).isEqualTo(original.getUsername());
        assertThat(roundTripped.getEmail()).isEqualTo(original.getEmail());
        assertThat(roundTripped.getDisplayName()).isEqualTo(original.getDisplayName());
        assertThat(roundTripped.getAvatarUrl()).isEqualTo(original.getAvatarUrl());
        assertThat(roundTripped.isActive()).isEqualTo(original.isActive());
        assertThat(roundTripped.getRoles()).isEqualTo(original.getRoles());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTripped.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(UserPersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(UserPersistenceMapper.toEntity(null)).isNull();
    }

    @Test
    void toDomainList_emptyList_returnsEmpty() {
        assertThat(UserPersistenceMapper.toDomainList(List.of())).isEmpty();
    }

    @Test
    void toDomainList_nullList_returnsEmpty() {
        assertThat(UserPersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toEntityList_emptyList_returnsEmpty() {
        assertThat(UserPersistenceMapper.toEntityList(List.of())).isEmpty();
    }

    @Test
    void toEntityList_nullList_returnsEmpty() {
        assertThat(UserPersistenceMapper.toEntityList(null)).isEmpty();
    }

    @Test
    void toDomain_nullRoles_returnsEmptyRoles() {
        UserJpaEntity entity = buildEntity();
        entity.setRoles(null);

        User user = UserPersistenceMapper.toDomain(entity);

        assertThat(user.getRoles()).isEmpty();
    }

    @Test
    void toEntity_emptyRoles_returnsEmptyRoles() {
        User user = new User(
                ID, KEYCLOAK_ID, USERNAME, EMAIL, "John", "Doe", DISPLAY_NAME, AVATAR_URL,
                true, EnumSet.noneOf(UserRole.class), CREATED_AT, UPDATED_AT
        );

        UserJpaEntity entity = UserPersistenceMapper.toEntity(user);

        assertThat(entity.getRoles()).isEmpty();
    }

    @Test
    void toDomainList_multipleEntities_mapsAll() {
        UserJpaEntity entity1 = buildEntity();
        UserJpaEntity entity2 = buildEntity();
        entity2.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        entity2.setUsername("janedoe");

        List<User> users = UserPersistenceMapper.toDomainList(List.of(entity1, entity2));

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(ID);
        assertThat(users.get(1).getUsername()).isEqualTo("janedoe");
    }

    @Test
    void roundTrip_withNullOptionalFields_preservesNulls() {
        User original = new User(
                ID, KEYCLOAK_ID, USERNAME, EMAIL,
                null, null, null, null, false,
                EnumSet.noneOf(UserRole.class), CREATED_AT, UPDATED_AT
        );

        User roundTripped = UserPersistenceMapper.toDomain(
                UserPersistenceMapper.toEntity(original));

        assertThat(roundTripped.getDisplayName()).isNull();
        assertThat(roundTripped.getAvatarUrl()).isNull();
        assertThat(roundTripped.isActive()).isFalse();
        assertThat(roundTripped.getRoles()).isEmpty();
    }

    // ── Test data builders ─────────────────────────────────────────

    private UserJpaEntity buildEntity() {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(ID);
        entity.setKeycloakId(KEYCLOAK_ID);
        entity.setUsername(USERNAME);
        entity.setEmail(EMAIL);
        entity.setDisplayName(DISPLAY_NAME);
        entity.setAvatarUrl(AVATAR_URL);
        entity.setActive(true);
        entity.setRoles(EnumSet.of(UserRole.PARTICIPANT, UserRole.ADMIN));
        entity.setCreatedAt(CREATED_AT);
        entity.setUpdatedAt(UPDATED_AT);
        return entity;
    }

    private User buildDomainUser() {
        return new User(
                ID,
                KEYCLOAK_ID,
                USERNAME,
                EMAIL,
                "John",
                "Doe",
                DISPLAY_NAME,
                AVATAR_URL,
                true,
                EnumSet.of(UserRole.PARTICIPANT, UserRole.ADMIN),
                CREATED_AT,
                UPDATED_AT
        );
    }
}
