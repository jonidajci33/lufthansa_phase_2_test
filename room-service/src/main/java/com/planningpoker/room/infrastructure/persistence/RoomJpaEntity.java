package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.RoomStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code room.rooms} table.
 * Serves as the persistence representation of the {@link Room} domain object.
 */
@Entity
@Table(name = "rooms", schema = "room")
public class RoomJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "moderator_id", nullable = false)
    private UUID moderatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_type_id")
    private DeckTypeJpaEntity deckType;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomParticipantJpaEntity> participants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomJpaEntity() {
        // JPA requires a no-arg constructor
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────

    @PrePersist
    void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = Instant.now();
    }

    // ── Package-private setters (used by persistence mappers) ────────

    void setId(UUID id) {
        this.id = id;
    }

    void setName(String name) {
        this.name = name;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setModeratorId(UUID moderatorId) {
        this.moderatorId = moderatorId;
    }

    void setDeckType(DeckTypeJpaEntity deckType) {
        this.deckType = deckType;
    }

    void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    void setStatus(RoomStatus status) {
        this.status = status;
    }

    void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    void setParticipants(List<RoomParticipantJpaEntity> participants) {
        this.participants = participants;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── Getters ───────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getModeratorId() {
        return moderatorId;
    }

    public DeckTypeJpaEntity getDeckType() {
        return deckType;
    }

    public String getShortCode() {
        return shortCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public List<RoomParticipantJpaEntity> getParticipants() {
        return participants;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
