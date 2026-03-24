package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.DeckType;

import java.util.List;
import java.util.UUID;

/**
 * Primary port for browsing available deck types.
 */
public interface ListDeckTypesUseCase {

    List<DeckType> listAll();

    DeckType getById(UUID id);
}
