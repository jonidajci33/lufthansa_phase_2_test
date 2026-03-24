package com.planningpoker.identity.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Core domain entity representing a user in the Planning Poker system.
 * <p>
 * This is a pure POJO — no framework annotations. All invariants are
 * enforced here so the domain model remains the single source of truth
 * for business rules.
 */
public class User {

    private UUID id;
    private String keycloakId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private boolean isActive;
    private final Set<UserRole> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
        this.roles = EnumSet.noneOf(UserRole.class);
        this.isActive = true;
    }

    public User(UUID id, String keycloakId, String username, String email,
                String firstName, String lastName,
                String displayName, String avatarUrl, boolean isActive,
                Set<UserRole> roles, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.keycloakId = keycloakId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.roles = roles != null ? EnumSet.copyOf(roles) : EnumSet.noneOf(UserRole.class);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Business methods ─────────────────────────────────────────────

    /**
     * Deactivates this user account. Idempotent.
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the user's profile information.
     *
     * @param displayName new display name (may be null to keep current)
     * @param avatarUrl   new avatar URL (may be null to keep current)
     */
    public void updateProfile(String displayName, String avatarUrl) {
        if (displayName != null) {
            this.displayName = displayName;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Grants a role to this user. Idempotent.
     */
    public void addRole(UserRole role) {
        Objects.requireNonNull(role, "role must not be null");
        this.roles.add(role);
    }

    /**
     * Revokes a role from this user. Idempotent.
     */
    public void removeRole(UserRole role) {
        Objects.requireNonNull(role, "role must not be null");
        this.roles.remove(role);
    }

    // ── Getters & setters ────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── Object contract ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
