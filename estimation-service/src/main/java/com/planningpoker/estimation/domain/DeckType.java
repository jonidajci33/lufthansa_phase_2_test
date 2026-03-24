package com.planningpoker.estimation.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a deck of estimation cards.
 * Read-only replicated data from the Room Service.
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

    // ── Getters ───────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeckCategory getCategory() {
        return category;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public List<DeckValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    public Instant getCreatedAt() {
        return createdAt;
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
