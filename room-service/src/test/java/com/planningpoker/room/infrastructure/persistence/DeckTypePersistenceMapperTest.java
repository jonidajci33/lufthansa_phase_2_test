package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeckTypePersistenceMapperTest {

    // ── toDomain ──────────────────────────────────────────────────────

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(DeckTypePersistenceMapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        DeckTypeJpaEntity entity = createEntity();

        DeckType result = DeckTypePersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
        assertThat(result.getName()).isEqualTo(entity.getName());
        assertThat(result.getCategory()).isEqualTo(entity.getCategory());
        assertThat(result.isSystem()).isEqualTo(entity.isSystem());
        assertThat(result.getCreatedBy()).isEqualTo(entity.getCreatedBy());
        assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(result.getValues()).hasSize(entity.getValues().size());
    }

    // ── toEntity ──────────────────────────────────────────────────────

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertThat(DeckTypePersistenceMapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        DeckType domain = createDomain();

        DeckTypeJpaEntity result = DeckTypePersistenceMapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(domain.getId());
        assertThat(result.getName()).isEqualTo(domain.getName());
        assertThat(result.getCategory()).isEqualTo(domain.getCategory());
        assertThat(result.isSystem()).isEqualTo(domain.isSystem());
        assertThat(result.getCreatedBy()).isEqualTo(domain.getCreatedBy());
        assertThat(result.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(result.getValues()).hasSize(domain.getValues().size());
    }

    // ── round-trip ────────────────────────────────────────────────────

    @Test
    void roundTrip_shouldPreserveAllFields() {
        DeckType original = createDomain();

        DeckTypeJpaEntity entity = DeckTypePersistenceMapper.toEntity(original);
        DeckType roundTripped = DeckTypePersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getName()).isEqualTo(original.getName());
        assertThat(roundTripped.getCategory()).isEqualTo(original.getCategory());
        assertThat(roundTripped.isSystem()).isEqualTo(original.isSystem());
        assertThat(roundTripped.getCreatedBy()).isEqualTo(original.getCreatedBy());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTripped.getValues()).hasSize(original.getValues().size());

        DeckValue originalValue = original.getValues().get(0);
        DeckValue roundTrippedValue = roundTripped.getValues().get(0);
        assertThat(roundTrippedValue.getId()).isEqualTo(originalValue.getId());
        assertThat(roundTrippedValue.getLabel()).isEqualTo(originalValue.getLabel());
    }

    // ── list variant ──────────────────────────────────────────────────

    @Test
    void toDomainList_shouldReturnEmptyList_whenNull() {
        assertThat(DeckTypePersistenceMapper.toDomainList(null)).isEmpty();
    }

    @Test
    void toDomainList_shouldMapAllElements() {
        List<DeckTypeJpaEntity> entities = List.of(createEntity(), createEntity());

        List<DeckType> result = DeckTypePersistenceMapper.toDomainList(entities);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static DeckTypeJpaEntity createEntity() {
        DeckTypeJpaEntity entity = new DeckTypeJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Fibonacci");
        entity.setCategory(DeckCategory.FIBONACCI);
        entity.setSystem(true);
        entity.setCreatedBy(null);
        entity.setCreatedAt(Instant.now());

        DeckValueJpaEntity value = new DeckValueJpaEntity();
        value.setId(UUID.randomUUID());
        value.setDeckType(entity);
        value.setLabel("5");
        value.setNumericValue(new BigDecimal("5"));
        value.setSortOrder(3);

        entity.setValues(new ArrayList<>(List.of(value)));
        return entity;
    }

    private static DeckType createDomain() {
        DeckValue value = new DeckValue(UUID.randomUUID(), "8", new BigDecimal("8"), 4);
        return new DeckType(
                UUID.randomUUID(), "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(value), Instant.now()
        );
    }
}
