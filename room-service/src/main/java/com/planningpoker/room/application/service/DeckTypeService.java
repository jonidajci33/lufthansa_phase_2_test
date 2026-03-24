package com.planningpoker.room.application.service;

import com.planningpoker.room.application.port.in.CreateDeckTypeUseCase;
import com.planningpoker.room.application.port.in.ListDeckTypesUseCase;
import com.planningpoker.room.application.port.out.DeckTypePersistencePort;
import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import com.planningpoker.room.web.dto.CreateDeckTypeRequest;
import com.planningpoker.room.web.dto.DeckValueRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates deck-type-related use cases.
 * <p>
 * Handles listing available decks and creating custom decks.
 */
@Service
@Transactional
public class DeckTypeService implements ListDeckTypesUseCase, CreateDeckTypeUseCase {

    private final DeckTypePersistencePort deckTypePersistencePort;

    public DeckTypeService(DeckTypePersistencePort deckTypePersistencePort) {
        this.deckTypePersistencePort = deckTypePersistencePort;
    }

    // -- ListDeckTypesUseCase ---------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DeckType> listAll() {
        return deckTypePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DeckType getById(UUID id) {
        return deckTypePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeckType", id));
    }

    // -- CreateDeckTypeUseCase --------------------------------------------

    @Override
    public DeckType create(CreateDeckTypeRequest request, UUID createdBy) {
        if (request.values() == null || request.values().size() < 2) {
            throw new BusinessException("INSUFFICIENT_VALUES",
                    "A custom deck must have at least 2 values");
        }

        Instant now = Instant.now();

        List<DeckValue> values = new ArrayList<>();
        for (int i = 0; i < request.values().size(); i++) {
            DeckValueRequest entry = request.values().get(i);
            DeckValue value = new DeckValue();
            value.setId(UUID.randomUUID());
            value.setLabel(entry.label());
            value.setNumericValue(entry.numericValue());
            value.setSortOrder(i);
            values.add(value);
        }

        DeckType deckType = new DeckType();
        deckType.setId(UUID.randomUUID());
        deckType.setName(request.name());
        deckType.setCategory(DeckCategory.CUSTOM);
        deckType.setSystem(false);
        deckType.setCreatedBy(createdBy);
        deckType.setValues(values);
        deckType.setCreatedAt(now);

        return deckTypePersistencePort.save(deckType);
    }
}
