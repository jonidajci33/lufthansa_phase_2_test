package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link DeckType} domain objects and {@link DeckTypeJpaEntity} persistence entities.
 * <p>
 * Delegates to {@link DeckValuePersistenceMapper} for nested deck values.
 * Pure utility class — no framework imports, no instantiation.
 */
public final class DeckTypePersistenceMapper {

    private DeckTypePersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link DeckTypeJpaEntity} to a {@link DeckType} domain object.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain object, or null if the entity is null
     */
    public static DeckType toDomain(DeckTypeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<DeckValue> domainValues = DeckValuePersistenceMapper.toDomainList(entity.getValues());

        return new DeckType(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.isSystem(),
                entity.getCreatedBy(),
                domainValues,
                entity.getCreatedAt()
        );
    }

    /**
     * Converts a list of {@link DeckTypeJpaEntity} to a list of {@link DeckType} domain objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain objects, never null
     */
    public static List<DeckType> toDomainList(List<DeckTypeJpaEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DeckTypePersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a {@link DeckType} domain object to a {@link DeckTypeJpaEntity}.
     *
     * @param deckType the domain object (may be null)
     * @return the JPA entity, or null if the domain object is null
     */
    public static DeckTypeJpaEntity toEntity(DeckType deckType) {
        if (deckType == null) {
            return null;
        }
        DeckTypeJpaEntity entity = new DeckTypeJpaEntity();
        entity.setId(deckType.getId());
        entity.setName(deckType.getName());
        entity.setCategory(deckType.getCategory());
        entity.setSystem(deckType.isSystem());
        entity.setCreatedBy(deckType.getCreatedBy());
        entity.setCreatedAt(deckType.getCreatedAt());

        List<DeckValueJpaEntity> valueEntities = new ArrayList<>();
        if (deckType.getValues() != null) {
            for (DeckValue value : deckType.getValues()) {
                valueEntities.add(DeckValuePersistenceMapper.toEntity(value, entity));
            }
        }
        entity.setValues(valueEntities);

        return entity;
    }
}
