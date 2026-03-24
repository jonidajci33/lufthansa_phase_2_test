package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.DeckValue;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from {@link DeckValueJpaEntity} to domain {@link DeckValue}.
 * <p>
 * Read-only (toDomain only) — deck values are replicated from the Room Service.
 * Pure utility class — no framework imports, null-safe.
 */
public final class DeckValuePersistenceMapper {

    private DeckValuePersistenceMapper() {
        // utility class
    }

    /**
     * Converts a {@link DeckValueJpaEntity} to a domain {@link DeckValue}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain deck value, or {@code null} if the input is null
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
     * Converts a list of {@link DeckValueJpaEntity} to domain {@link DeckValue} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain deck values; empty list if input is null or empty
     */
    public static List<DeckValue> toDomainList(List<DeckValueJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DeckValuePersistenceMapper::toDomain)
                .toList();
    }
}
