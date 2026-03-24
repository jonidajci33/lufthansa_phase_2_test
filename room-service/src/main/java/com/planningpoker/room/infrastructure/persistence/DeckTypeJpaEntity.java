package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code room.deck_types} table.
 */
@Entity
@Table(name = "deck_types", schema = "room")
public class DeckTypeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private DeckCategory category;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem;

    @Column(name = "created_by")
    private UUID createdBy;

    @OneToMany(mappedBy = "deckType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DeckValueJpaEntity> values = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DeckTypeJpaEntity() {
        // JPA requires a no-arg constructor
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // ── Package-private setters (used by persistence mappers) ────────

    void setId(UUID id) {
        this.id = id;
    }

    void setName(String name) {
        this.name = name;
    }

    void setCategory(DeckCategory category) {
        this.category = category;
    }

    void setSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    void setValues(List<DeckValueJpaEntity> values) {
        this.values = values;
    }

    void setCreatedAt(Instant createdAt) {
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

    public List<DeckValueJpaEntity> getValues() {
        return values;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
