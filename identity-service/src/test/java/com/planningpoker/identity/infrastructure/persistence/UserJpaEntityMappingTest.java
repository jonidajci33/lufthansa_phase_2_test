package com.planningpoker.identity.infrastructure.persistence;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserJpaEntityMappingTest {

    @Test
    void shouldMapDomainToJpaAndBack() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        User original = new User(
                id,
                "kc-mapping-test",
                "mappinguser",
                "mapping@example.com",
                "Mapping",
                "User",
                "Mapping User",
                "https://avatar.example.com/mapping.png",
                true,
                EnumSet.of(UserRole.PARTICIPANT, UserRole.ADMIN),
                now,
                now
        );

        // Domain -> JPA entity (via mapper)
        UserJpaEntity entity = UserPersistenceMapper.toEntity(original);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getKeycloakId()).isEqualTo("kc-mapping-test");
        assertThat(entity.getUsername()).isEqualTo("mappinguser");
        assertThat(entity.getEmail()).isEqualTo("mapping@example.com");
        assertThat(entity.getDisplayName()).isEqualTo("Mapping User");
        assertThat(entity.getAvatarUrl()).isEqualTo("https://avatar.example.com/mapping.png");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getRoles()).containsExactlyInAnyOrder(UserRole.PARTICIPANT, UserRole.ADMIN);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);

        // JPA entity -> Domain (via mapper)
        User roundTripped = UserPersistenceMapper.toDomain(entity);

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
    void shouldHandleEmptyRolesOnRoundTrip() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User original = new User(
                id,
                "kc-empty-roles",
                "noroles",
                "noroles@example.com",
                null,
                null,
                null,
                null,
                false,
                EnumSet.noneOf(UserRole.class),
                now,
                now
        );

        UserJpaEntity entity = UserPersistenceMapper.toEntity(original);
        User roundTripped = UserPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getRoles()).isEmpty();
        assertThat(roundTripped.isActive()).isFalse();
        assertThat(roundTripped.getDisplayName()).isNull();
        assertThat(roundTripped.getAvatarUrl()).isNull();
    }
}
