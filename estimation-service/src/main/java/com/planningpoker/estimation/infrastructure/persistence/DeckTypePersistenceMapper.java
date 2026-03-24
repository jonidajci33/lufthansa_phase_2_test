package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.DeckType;
import com.planningpoker.estimation.domain.DeckValue;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from {@link DeckTypeJpaEntity} to domain {@link DeckType}.
 * <p>
 * Read-only (toDomain only) — deck types are replicated from the Room Service.
 * Delegates value mapping to {@link DeckValuePersistenceMapper}.
 * Pure utility class — no framework imports, null-safe.
 */
public final class DeckTypePersistenceMapper {

    private DeckTypePersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link DeckTypeJpaEntity} to a domain {@link DeckType}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain deck type, or {@code null} if the input is null
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
     * Converts a list of {@link DeckTypeJpaEntity} to domain {@link DeckType} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain deck types; empty list if input is null or empty
     */
    public static List<DeckType> toDomainList(List<DeckTypeJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DeckTypePersistenceMapper::toDomain)
                .toList();
    }
}
