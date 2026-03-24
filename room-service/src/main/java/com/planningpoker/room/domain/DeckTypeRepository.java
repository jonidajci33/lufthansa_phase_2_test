package com.planningpoker.room.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain-level repository port for {@link DeckType} persistence.
 * <p>
 * This is a pure interface with ZERO framework dependencies. Infrastructure
 * adapters implement it in the adapter layer.
 */
public interface DeckTypeRepository {

    Optional<DeckType> findById(UUID id);

    List<DeckType> findAll();

    List<DeckType> findByCategory(DeckCategory category);

    DeckType save(DeckType deckType);
}
