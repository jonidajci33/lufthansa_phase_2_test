package com.planningpoker.room.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code room.deck_values} table.
 */
@Entity
@Table(name = "deck_values", schema = "room")
public class DeckValueJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_type_id", nullable = false)
    private DeckTypeJpaEntity deckType;

    @Column(name = "label", nullable = false, length = 20)
    private String label;

    @Column(name = "numeric_value")
    private BigDecimal numericValue;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected DeckValueJpaEntity() {
        // JPA requires a no-arg constructor
    }

    // ── Package-private setters (used by persistence mappers) ────────

    void setId(UUID id) {
        this.id = id;
    }

    void setDeckType(DeckTypeJpaEntity deckType) {
        this.deckType = deckType;
    }

    void setLabel(String label) {
        this.label = label;
    }

    void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ── Getters ───────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public DeckTypeJpaEntity getDeckType() {
        return deckType;
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
}
