package com.planningpoker.room.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Core domain entity representing a planning poker room.
 * <p>
 * This is a pure POJO — no framework annotations. All invariants are
 * enforced here so the domain model remains the single source of truth
 * for business rules.
 */
public class Room {

    private UUID id;
    private String name;
    private String description;
    private UUID moderatorId;
    private DeckType deckType;
    private String shortCode;
    private RoomStatus status;
    private int maxParticipants;
    private List<RoomParticipant> participants;
    private Instant createdAt;
    private Instant updatedAt;

    public Room() {
        this.status = RoomStatus.ACTIVE;
        this.maxParticipants = 50;
        this.participants = new ArrayList<>();
    }

    public Room(UUID id, String name, String description, UUID moderatorId,
                DeckType deckType, String shortCode, RoomStatus status,
                int maxParticipants, List<RoomParticipant> participants,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.moderatorId = moderatorId;
        this.deckType = deckType;
        this.shortCode = shortCode;
        this.status = status;
        this.maxParticipants = maxParticipants;
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Business methods ─────────────────────────────────────────────

    /**
     * Updates the room's editable fields.
     *
     * @param name            new name (may be null to keep current)
     * @param description     new description (may be null to keep current)
     * @param maxParticipants new max participants (null to keep current)
     */
    public void update(String name, String description, Integer maxParticipants) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (maxParticipants != null) {
            this.maxParticipants = maxParticipants;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Archives this room. Idempotent.
     */
    public void archive() {
        this.status = RoomStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks whether the given user is the moderator of this room.
     */
    public boolean isModeratedBy(UUID userId) {
        return Objects.equals(this.moderatorId, userId);
    }

    /**
     * Adds a participant to this room.
     *
     * @param participant the participant to add
     */
    public void addParticipant(RoomParticipant participant) {
        Objects.requireNonNull(participant, "participant must not be null");
        this.participants.add(participant);
    }

    /**
     * Removes a participant by user id. Idempotent.
     *
     * @param userId the user id to remove
     */
    public void removeParticipant(UUID userId) {
        this.participants.removeIf(p -> Objects.equals(p.getUserId(), userId));
    }

    /**
     * Returns {@code true} if the room has reached its participant capacity.
     */
    public boolean isFull() {
        long activeCount = participants.stream()
                .filter(p -> p.getLeftAt() == null)
                .count();
        return activeCount >= maxParticipants;
    }

    // ── Getters & setters ────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(UUID moderatorId) {
        this.moderatorId = moderatorId;
    }

    public DeckType getDeckType() {
        return deckType;
    }

    public void setDeckType(DeckType deckType) {
        this.deckType = deckType;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public List<RoomParticipant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public void setParticipants(List<RoomParticipant> participants) {
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
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
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", name='" + name + "', shortCode='" + shortCode + "', status=" + status + "}";
    }
}
