package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import com.planningpoker.room.web.dto.DeckTypeResponse;
import com.planningpoker.room.web.dto.DeckValueResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from {@link DeckType} domain objects to {@link DeckTypeResponse} DTOs.
 * <p>
 * Pure utility class — no framework imports, no instantiation.
 */
public final class DeckTypeRestMapper {

    private DeckTypeRestMapper() {
        // utility class
    }

    /**
     * Converts a {@link DeckType} domain object to a {@link DeckTypeResponse} DTO.
     *
     * @param deckType the domain object (may be null)
     * @return the response DTO, or null if the domain object is null
     */
    public static DeckTypeResponse toResponse(DeckType deckType) {
        if (deckType == null) {
            return null;
        }
        List<DeckValueResponse> valueResponses = deckType.getValues() != null
                ? deckType.getValues().stream()
                    .map(DeckTypeRestMapper::toValueResponse)
                    .toList()
                : List.of();

        return new DeckTypeResponse(
                deckType.getId(),
                deckType.getName(),
                deckType.getCategory(),
                deckType.isSystem(),
                valueResponses
        );
    }

    /**
     * Converts a list of {@link DeckType} domain objects to a list of {@link DeckTypeResponse} DTOs.
     *
     * @param deckTypes the domain objects (may be null)
     * @return an unmodifiable list of response DTOs, never null
     */
    public static List<DeckTypeResponse> toResponseList(List<DeckType> deckTypes) {
        if (deckTypes == null) {
            return Collections.emptyList();
        }
        return deckTypes.stream()
                .map(DeckTypeRestMapper::toResponse)
                .toList();
    }

    /**
     * Converts a single {@link DeckValue} domain object to a {@link DeckValueResponse} DTO.
     */
    private static DeckValueResponse toValueResponse(DeckValue value) {
        if (value == null) {
            return null;
        }
        return new DeckValueResponse(
                value.getId(),
                value.getLabel(),
                value.getNumericValue(),
                value.getSortOrder()
        );
    }
}
