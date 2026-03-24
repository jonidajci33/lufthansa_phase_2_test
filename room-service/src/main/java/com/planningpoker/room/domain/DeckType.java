package com.planningpoker.room.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a deck of estimation cards.
 * <p>
 * System decks (Fibonacci, T-Shirt, etc.) are seeded at startup.
 * Users may create custom decks with at least 2 values.
 */
public class DeckType {

    private UUID id;
    private String name;
    private DeckCategory category;
    private boolean isSystem;
    private UUID createdBy;
    private List<DeckValue> values;
    private Instant createdAt;

    public DeckType() {
        this.values = new ArrayList<>();
    }

    public DeckType(UUID id, String name, DeckCategory category, boolean isSystem,
                    UUID createdBy, List<DeckValue> values, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.isSystem = isSystem;
        this.createdBy = createdBy;
        this.values = values != null ? new ArrayList<>(values) : new ArrayList<>();
        this.createdAt = createdAt;
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

    public DeckCategory getCategory() {
        return category;
    }

    public void setCategory(DeckCategory category) {
        this.category = category;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public List<DeckValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void setValues(List<DeckValue> values) {
        this.values = values != null ? new ArrayList<>(values) : new ArrayList<>();
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
        DeckType that = (DeckType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DeckType{id=" + id + ", name='" + name + "', category=" + category + "}";
    }
}
