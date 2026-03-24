package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.InvitationStatus;
import com.planningpoker.room.domain.InvitationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code room.invitations} table.
 * Serves as the persistence representation of the {@link Invitation} domain object.
 */
@Entity
@Table(name = "invitations", schema = "room")
public class InvitationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private InvitationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvitationStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected InvitationJpaEntity() {
        // JPA requires a no-arg constructor
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // ── Package-private setters (used by persistence mappers) ────────

    void setId(UUID id) {
        this.id = id;
    }

    void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    void setInvitedBy(UUID invitedBy) {
        this.invitedBy = invitedBy;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setToken(String token) {
        this.token = token;
    }

    void setType(InvitationType type) {
        this.type = type;
    }

    void setStatus(InvitationStatus status) {
        this.status = status;
    }

    void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getInvitedBy() {
        return invitedBy;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public InvitationType getType() {
        return type;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
