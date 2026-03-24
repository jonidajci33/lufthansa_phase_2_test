package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.ParticipantRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code room.room_participants} table.
 */
@Entity
@Table(name = "room_participants", schema = "room")
public class RoomParticipantJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomJpaEntity room;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "is_connected", nullable = false)
    private boolean isConnected;

    protected RoomParticipantJpaEntity() {
        // JPA requires a no-arg constructor
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────

    @PrePersist
    void onPrePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    // ── Package-private setters (used by persistence mappers) ────────

    void setId(UUID id) {
        this.id = id;
    }

    void setRoom(RoomJpaEntity room) {
        this.room = room;
    }

    void setUserId(UUID userId) {
        this.userId = userId;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setRole(ParticipantRole role) {
        this.role = role;
    }

    void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    // ── Getters ───────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public RoomJpaEntity getRoom() {
        return room;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
