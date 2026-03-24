package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.web.dto.CreateDeckTypeRequest;

import java.util.UUID;

/**
 * Primary port for creating a custom deck type.
 */
public interface CreateDeckTypeUseCase {

    DeckType create(CreateDeckTypeRequest request, UUID createdBy);
}
