package com.planningpoker.estimation.infrastructure.persistence;

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
 * JPA entity mapped to the {@code estimation.deck_values} table.
 * Read-only replicated data from the Room Service.
 */
@Entity
@Table(name = "deck_values", schema = "estimation")
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
