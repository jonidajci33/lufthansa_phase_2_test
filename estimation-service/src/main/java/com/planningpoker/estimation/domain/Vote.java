package com.planningpoker.estimation.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a single vote cast by a participant on a story.
 */
public class Vote {

    private UUID id;
    private UUID storyId;
    private UUID userId;
    private String value;
    private BigDecimal numericValue;
    private boolean anonymous;
    private Instant createdAt;
    private Instant updatedAt;

    public Vote() {
    }

    public Vote(UUID id, UUID storyId, UUID userId, String value, BigDecimal numericValue,
                boolean anonymous, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.storyId = storyId;
        this.userId = userId;
        this.value = value;
        this.numericValue = numericValue;
        this.anonymous = anonymous;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Factory ───────────────────────────────────────────────────────

    public static Vote create(UUID storyId, UUID userId, String value, BigDecimal numericValue) {
        Instant now = Instant.now();
        return new Vote(UUID.randomUUID(), storyId, userId, value, numericValue, true, now, now);
    }

    // ── Getters & setters ────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStoryId() {
        return storyId;
    }

    public void setStoryId(UUID storyId) {
        this.storyId = storyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
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
        Vote that = (Vote) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vote{id=" + id + ", storyId=" + storyId + ", userId=" + userId + ", value='" + value + "'}";
    }
}
