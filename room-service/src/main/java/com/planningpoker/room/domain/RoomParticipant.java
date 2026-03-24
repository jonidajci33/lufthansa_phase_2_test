package com.planningpoker.room.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a user's membership in a room.
 */
public class RoomParticipant {

    private UUID id;
    private UUID roomId;
    private UUID userId;
    private String username;
    private ParticipantRole role;
    private Instant joinedAt;
    private Instant leftAt;
    private boolean isConnected;

    public RoomParticipant() {
    }

    public RoomParticipant(UUID id, UUID roomId, UUID userId, String username, ParticipantRole role,
                           Instant joinedAt, Instant leftAt, boolean isConnected) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.isConnected = isConnected;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    // ── Object contract ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomParticipant that = (RoomParticipant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RoomParticipant{id=" + id + ", roomId=" + roomId + ", userId=" + userId + ", role=" + role + "}";
    }
}
