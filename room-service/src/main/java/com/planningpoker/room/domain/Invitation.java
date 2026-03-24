package com.planningpoker.room.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an invitation to join a room.
 * <p>
 * Invitations can be sent via email or generated as shareable links.
 * They expire after a configured period and can be cancelled by the
 * room moderator.
 */
public class Invitation {

    private UUID id;
    private UUID roomId;
    private UUID invitedBy;
    private String email;
    private String token;
    private InvitationType type;
    private InvitationStatus status;
    private Instant expiresAt;
    private Instant acceptedAt;
    private Instant createdAt;

    public Invitation() {
        this.status = InvitationStatus.PENDING;
    }

    public Invitation(UUID id, UUID roomId, UUID invitedBy, String email,
                      String token, InvitationType type, InvitationStatus status,
                      Instant expiresAt, Instant acceptedAt, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.invitedBy = invitedBy;
        this.email = email;
        this.token = token;
        this.type = type;
        this.status = status;
        this.expiresAt = expiresAt;
        this.acceptedAt = acceptedAt;
        this.createdAt = createdAt;
    }

    // ── Business methods ─────────────────────────────────────────────

    /**
     * Marks this invitation as accepted.
     *
     * @throws IllegalStateException if the invitation is not in PENDING status
     */
    public void accept() {
        if (this.status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is not in PENDING status; current status: " + this.status);
        }
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    /**
     * Returns {@code true} if the invitation has passed its expiry time.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Cancels this invitation. Idempotent.
     */
    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
    }

    // ── Getters & setters ────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public UUID getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UUID invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public InvitationType getType() {
        return type;
    }

    public void setType(InvitationType type) {
        this.type = type;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // ── Object contract ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invitation that = (Invitation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Invitation{id=" + id + ", roomId=" + roomId + ", type=" + type + ", status=" + status + "}";
    }
}
