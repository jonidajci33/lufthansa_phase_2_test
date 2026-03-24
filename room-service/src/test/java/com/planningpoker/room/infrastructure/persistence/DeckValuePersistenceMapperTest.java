package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeckValuePersistenceMapperTest {

    // ── toDomain ──────────────────────────────────────────────────────

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(DeckValuePersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        DeckTypeJpaEntity parent = createParentDeckType();
        DeckValueJpaEntity entity = createEntity(parent);

        DeckValue result = DeckValuePersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getLabel()).isEqualTo(entity.getLabel());
        assertThat(result.getNumericValue()).isEqualByComparingTo(entity.getNumericValue());
        assertThat(result.getSortOrder()).isEqualTo(entity.getSortOrder());
    }

    // ── toEntity ──────────────────────────────────────────────────────

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertThat(DeckValuePersistenceMapper.toEntity(null, createParentDeckType())).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        DeckTypeJpaEntity parent = createParentDeckType();
        DeckValue domain = new DeckValue(UUID.randomUUID(), "8", new BigDecimal("8"), 5);

        DeckValueJpaEntity result = DeckValuePersistenceMapper.toEntity(domain, parent);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(domain.getId());
        assertThat(result.getLabel()).isEqualTo(domain.getLabel());
        assertThat(result.getNumericValue()).isEqualByComparingTo(domain.getNumericValue());
        assertThat(result.getSortOrder()).isEqualTo(domain.getSortOrder());
        assertThat(result.getDeckType()).isSameAs(parent);
    }

    // ── round-trip ────────────────────────────────────────────────────

    @Test
    void roundTrip_shouldPreserveAllFields() {
        DeckValue original = new DeckValue(UUID.randomUUID(), "13", new BigDecimal("13"), 6);
        DeckTypeJpaEntity parent = createParentDeckType();

        DeckValueJpaEntity entity = DeckValuePersistenceMapper.toEntity(original, parent);
        DeckValue roundTripped = DeckValuePersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getLabel()).isEqualTo(original.getLabel());
        assertThat(roundTripped.getNumericValue()).isEqualByComparingTo(original.getNumericValue());
        assertThat(roundTripped.getSortOrder()).isEqualTo(original.getSortOrder());
    }

    // ── list variants ─────────────────────────────────────────────────

    @Test
    void toDomainList_shouldReturnEmptyList_whenNull() {
        assertThat(DeckValuePersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toEntityList_shouldReturnEmptyList_whenNull() {
        assertThat(DeckValuePersistenceMapper.toEntityList(null, createParentDeckType())).isEmpty();
    }

    @Test
    void toDomainList_shouldMapAllElements() {
        DeckTypeJpaEntity parent = createParentDeckType();
        List<DeckValueJpaEntity> entities = List.of(
                createEntity(parent),
                createEntity(parent)
        );

        List<DeckValue> result = DeckValuePersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static DeckTypeJpaEntity createParentDeckType() {
        DeckTypeJpaEntity entity = new DeckTypeJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Fibonacci");
        entity.setCategory(DeckCategory.FIBONACCI);
        entity.setSystem(true);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private static DeckValueJpaEntity createEntity(DeckTypeJpaEntity parent) {
        DeckValueJpaEntity entity = new DeckValueJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setDeckType(parent);
        entity.setLabel("5");
        entity.setNumericValue(new BigDecimal("5"));
        entity.setSortOrder(3);
        return entity;
    }
}
