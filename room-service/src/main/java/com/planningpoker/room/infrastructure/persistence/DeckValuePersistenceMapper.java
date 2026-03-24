package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckValue;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link DeckValue} domain objects and {@link DeckValueJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, no instantiation.
 */
public final class DeckValuePersistenceMapper {

    private DeckValuePersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link DeckValueJpaEntity} to a {@link DeckValue} domain object.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain object, or null if the entity is null
     */
    public static DeckValue toDomain(DeckValueJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DeckValue(
                entity.getId(),
                entity.getLabel(),
                entity.getNumericValue(),
                entity.getSortOrder()
        );
    }

    /**
     * Converts a list of {@link DeckValueJpaEntity} to a list of {@link DeckValue} domain objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain objects, never null
     */
    public static List<DeckValue> toDomainList(List<DeckValueJpaEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DeckValuePersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a {@link DeckValue} domain object to a {@link DeckValueJpaEntity}.
     *
     * @param value  the domain object (may be null)
     * @param parent the parent deck type entity (required for the FK relationship)
     * @return the JPA entity, or null if the domain object is null
     */
    public static DeckValueJpaEntity toEntity(DeckValue value, DeckTypeJpaEntity parent) {
        if (value == null) {
            return null;
        }
        DeckValueJpaEntity entity = new DeckValueJpaEntity();
        entity.setId(value.getId());
        entity.setDeckType(parent);
        entity.setLabel(value.getLabel());
        entity.setNumericValue(value.getNumericValue());
        entity.setSortOrder(value.getSortOrder());
        return entity;
    }

    /**
     * Converts a list of {@link DeckValue} domain objects to a list of {@link DeckValueJpaEntity}.
     *
     * @param values the domain objects (may be null)
     * @param parent the parent deck type entity
     * @return an unmodifiable list of JPA entities, never null
     */
    public static List<DeckValueJpaEntity> toEntityList(List<DeckValue> values, DeckTypeJpaEntity parent) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(v -> toEntity(v, parent))
                .toList();
    }
}
