package com.planningpoker.identity.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    // ── Factory helpers ───────────────────────────────────────────────

    private static User newDefaultUser() {
        return new User();
    }

    private static User newFullUser() {
        Instant now = Instant.now();
        User user = new User(
                UUID.randomUUID(),
                "kc-123",
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
        return user;
    }

    // ── Constructor / default values ──────────────────────────────────

    @Test
    void shouldCreateUserWithDefaultValues() {
        User user = newDefaultUser();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).isEmpty();
        assertThat(user.getId()).isNull();
        assertThat(user.getKeycloakId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getDisplayName()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void shouldCreateUserWithAllArgsConstructor() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Set<UserRole> roles = EnumSet.of(UserRole.PARTICIPANT, UserRole.ADMIN);

        User user = new User(id, "kc-1", "alice", "alice@test.com",
                "Alice", "TestLast", "Alice", "https://img.com/a.png", true, roles, now, now);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getKeycloakId()).isEqualTo("kc-1");
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@test.com");
        assertThat(user.getDisplayName()).isEqualTo("Alice");
        assertThat(user.getAvatarUrl()).isEqualTo("https://img.com/a.png");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.PARTICIPANT, UserRole.ADMIN);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldHandleNullRolesInConstructor() {
        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), "kc-1", "bob", "bob@test.com",
                "Bob", null, "Bob", null, true, null, now, now);

        assertThat(user.getRoles()).isNotNull().isEmpty();
    }

    // ── deactivate() ──────────────────────────────────────────────────

    @Test
    void shouldDeactivateUser() {
        User user = newFullUser();
        assertThat(user.isActive()).isTrue();

        Instant beforeDeactivate = Instant.now();
        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeDeactivate);
    }

    @Test
    void shouldNotFailWhenDeactivatingAlreadyInactiveUser() {
        User user = newFullUser();
        user.deactivate();
        assertThat(user.isActive()).isFalse();

        Instant afterFirstDeactivate = user.getUpdatedAt();

        // Deactivating again should be idempotent — no exception
        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(afterFirstDeactivate);
    }

    // ── updateProfile() ───────────────────────────────────────────────

    @Test
    void shouldUpdateProfile() {
        User user = newFullUser();
        String originalDisplayName = user.getDisplayName();
        String originalAvatarUrl = user.getAvatarUrl();

        Instant beforeUpdate = Instant.now();
        user.updateProfile("New Name", "https://example.com/new-avatar.png");

        assertThat(user.getDisplayName()).isEqualTo("New Name");
        assertThat(user.getDisplayName()).isNotEqualTo(originalDisplayName);
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.png");
        assertThat(user.getAvatarUrl()).isNotEqualTo(originalAvatarUrl);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void shouldUpdateProfileWithNullDisplayNameIgnored() {
        User user = newFullUser();
        String originalDisplayName = user.getDisplayName();

        user.updateProfile(null, "https://example.com/new-avatar.png");

        assertThat(user.getDisplayName()).isEqualTo(originalDisplayName);
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.png");
    }

    @Test
    void shouldUpdateProfileWithNullAvatarUrlIgnored() {
        User user = newFullUser();
        String originalAvatarUrl = user.getAvatarUrl();

        user.updateProfile("New Name", null);

        assertThat(user.getDisplayName()).isEqualTo("New Name");
        assertThat(user.getAvatarUrl()).isEqualTo(originalAvatarUrl);
    }

    @Test
    void shouldUpdateProfileWithBothNullsKeepingExistingValues() {
        User user = newFullUser();
        String originalDisplayName = user.getDisplayName();
        String originalAvatarUrl = user.getAvatarUrl();

        Instant beforeUpdate = Instant.now();
        user.updateProfile(null, null);

        assertThat(user.getDisplayName()).isEqualTo(originalDisplayName);
        assertThat(user.getAvatarUrl()).isEqualTo(originalAvatarUrl);
        // updatedAt should still be bumped even when no fields actually change
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    // ── addRole() ─────────────────────────────────────────────────────

    @Test
    void shouldAddRole() {
        User user = newDefaultUser();
        assertThat(user.getRoles()).isEmpty();

        user.addRole(UserRole.ADMIN);

        assertThat(user.getRoles()).containsExactly(UserRole.ADMIN);
    }

    @Test
    void shouldNotAddDuplicateRole() {
        User user = newDefaultUser();
        user.addRole(UserRole.ADMIN);
        user.addRole(UserRole.ADMIN);

        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles()).containsExactly(UserRole.ADMIN);
    }

    @Test
    void shouldAddMultipleDistinctRoles() {
        User user = newDefaultUser();
        user.addRole(UserRole.PARTICIPANT);
        user.addRole(UserRole.ADMIN);

        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.PARTICIPANT, UserRole.ADMIN);
    }

    @Test
    void shouldThrowWhenAddingNullRole() {
        User user = newDefaultUser();

        assertThatThrownBy(() -> user.addRole(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("role must not be null");
    }

    // ── removeRole() ──────────────────────────────────────────────────

    @Test
    void shouldRemoveRole() {
        User user = newDefaultUser();
        user.addRole(UserRole.ADMIN);
        user.addRole(UserRole.PARTICIPANT);

        user.removeRole(UserRole.ADMIN);

        assertThat(user.getRoles()).containsExactly(UserRole.PARTICIPANT);
    }

    @Test
    void shouldNotFailWhenRemovingAbsentRole() {
        User user = newDefaultUser();
        assertThat(user.getRoles()).isEmpty();

        // Removing a role that is not present should be idempotent — no exception
        user.removeRole(UserRole.ADMIN);

        assertThat(user.getRoles()).isEmpty();
    }

    @Test
    void shouldThrowWhenRemovingNullRole() {
        User user = newDefaultUser();

        assertThatThrownBy(() -> user.removeRole(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("role must not be null");
    }

    // ── getRoles() returns unmodifiable set ───────────────────────────

    @Test
    void shouldReturnUnmodifiableRolesSet() {
        User user = newDefaultUser();
        user.addRole(UserRole.PARTICIPANT);

        Set<UserRole> roles = user.getRoles();

        assertThatThrownBy(() -> roles.add(UserRole.ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ── equals / hashCode / toString ──────────────────────────────────

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user1 = new User(id, "kc-1", "alice", "alice@test.com",
                "Alice", null, "Alice", null, true, null, now, now);
        User user2 = new User(id, "kc-2", "bob", "bob@test.com",
                "Bob", null, "Bob", null, false, null, now, now);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Instant now = Instant.now();
        User user1 = new User(UUID.randomUUID(), "kc-1", "alice", "alice@test.com",
                "Alice", null, "Alice", null, true, null, now, now);
        User user2 = new User(UUID.randomUUID(), "kc-1", "alice", "alice@test.com",
                "Alice", null, "Alice", null, true, null, now, now);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void shouldIncludeKeyFieldsInToString() {
        User user = newFullUser();

        String str = user.toString();

        assertThat(str).contains("User{");
        assertThat(str).contains(user.getUsername());
        assertThat(str).contains(user.getEmail());
    }
}
