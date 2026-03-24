package com.planningpoker.estimation.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a single card in a deck type.
 * Read-only replicated data from the Room Service.
 */
public class DeckValue {

    private UUID id;
    private String label;
    private BigDecimal numericValue;
    private int sortOrder;

    public DeckValue() {
    }

    public DeckValue(UUID id, String label, BigDecimal numericValue, int sortOrder) {
        this.id = id;
        this.label = label;
        this.numericValue = numericValue;
        this.sortOrder = sortOrder;
    }

    // ── Getters ───────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    // ── Object contract ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeckValue that = (DeckValue) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DeckValue{id=" + id + ", label='" + label + "', numericValue=" + numericValue + ", sortOrder=" + sortOrder + "}";
    }
}
