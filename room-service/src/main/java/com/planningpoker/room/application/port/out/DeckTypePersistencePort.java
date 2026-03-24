package com.planningpoker.room.application.port.out;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for deck type persistence.
 * Mirrors the domain {@link com.planningpoker.room.domain.DeckTypeRepository}
 * contract. Infrastructure adapters implement this interface.
 */
public interface DeckTypePersistencePort {

    Optional<DeckType> findById(UUID id);

    List<DeckType> findAll();

    List<DeckType> findByCategory(DeckCategory category);

    DeckType save(DeckType deckType);
}
