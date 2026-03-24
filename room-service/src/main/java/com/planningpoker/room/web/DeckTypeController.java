package com.planningpoker.room.web;

import com.planningpoker.room.application.port.in.CreateDeckTypeUseCase;
import com.planningpoker.room.application.port.in.ListDeckTypesUseCase;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.web.dto.CreateDeckTypeRequest;
import com.planningpoker.room.web.dto.DeckTypeResponse;
import com.planningpoker.room.web.mapper.DeckTypeRestMapper;
import com.planningpoker.shared.security.JwtClaimExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for deck type operations.
 */
@RestController
@RequestMapping("/api/v1/deck-types")
@Tag(name = "Deck Types")
public class DeckTypeController {

    private final ListDeckTypesUseCase listDeckTypesUseCase;
    private final CreateDeckTypeUseCase createDeckTypeUseCase;

    public DeckTypeController(ListDeckTypesUseCase listDeckTypesUseCase,
                              CreateDeckTypeUseCase createDeckTypeUseCase) {
        this.listDeckTypesUseCase = listDeckTypesUseCase;
        this.createDeckTypeUseCase = createDeckTypeUseCase;
    }

    @GetMapping
    @Operation(summary = "List all deck types", description = "Returns all available deck types (system and custom).")
    @ApiResponse(responseCode = "200", description = "List of deck types")
    public ResponseEntity<List<DeckTypeResponse>> list() {
        List<DeckType> deckTypes = listDeckTypesUseCase.listAll();
        List<DeckTypeResponse> response = deckTypes.stream()
                .map(DeckTypeRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create custom deck type", description = "Creates a new custom deck type with the specified values.")
    @ApiResponse(responseCode = "201", description = "Custom deck type created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<DeckTypeResponse> create(@Valid @RequestBody CreateDeckTypeRequest request) {
        UUID createdBy = UUID.fromString(JwtClaimExtractor.currentUserId());
        DeckType deckType = createDeckTypeUseCase.create(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(DeckTypeRestMapper.toResponse(deckType));
    }
}
