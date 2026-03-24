package com.planningpoker.room.application.service;

import com.planningpoker.room.application.port.out.DeckTypePersistencePort;
import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.web.dto.CreateDeckTypeRequest;
import com.planningpoker.room.web.dto.DeckValueRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckTypeServiceTest {

    @Mock
    private DeckTypePersistencePort deckTypePersistencePort;

    @InjectMocks
    private DeckTypeService deckTypeService;

    // ── Helpers ───────────────────────────────────────────────────────

    private static DeckType sampleDeckType() {
        return new DeckType(UUID.randomUUID(), "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(), Instant.now());
    }

    // ═══════════════════════════════════════════════════════════════════
    // ListAll
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldListAllDeckTypes() {
        List<DeckType> deckTypes = List.of(sampleDeckType(), sampleDeckType());
        when(deckTypePersistencePort.findAll()).thenReturn(deckTypes);

        List<DeckType> result = deckTypeService.listAll();

        assertThat(result).hasSize(2);
        verify(deckTypePersistencePort).findAll();
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetById
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldGetDeckTypeById() {
        DeckType deckType = sampleDeckType();
        when(deckTypePersistencePort.findById(deckType.getId())).thenReturn(Optional.of(deckType));

        DeckType result = deckTypeService.getById(deckType.getId());

        assertThat(result).isEqualTo(deckType);
    }

    @Test
    void shouldThrowWhenDeckTypeNotFound() {
        UUID id = UUID.randomUUID();
        when(deckTypePersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckTypeService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Create custom deck type
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCreateCustomDeckTypeWithValues() {
        UUID createdBy = UUID.randomUUID();
        List<DeckValueRequest> values = List.of(
                new DeckValueRequest("1", BigDecimal.ONE),
                new DeckValueRequest("2", BigDecimal.valueOf(2))
        );
        CreateDeckTypeRequest request = new CreateDeckTypeRequest("Custom Deck", values);

        when(deckTypePersistencePort.save(any(DeckType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeckType result = deckTypeService.create(request, createdBy);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Custom Deck");
        assertThat(result.getCategory()).isEqualTo(DeckCategory.CUSTOM);
        assertThat(result.isSystem()).isFalse();
        assertThat(result.getCreatedBy()).isEqualTo(createdBy);
        assertThat(result.getValues()).hasSize(2);
        assertThat(result.getValues().get(0).getLabel()).isEqualTo("1");
        assertThat(result.getValues().get(1).getLabel()).isEqualTo("2");

        verify(deckTypePersistencePort).save(any(DeckType.class));
    }

    @Test
    void shouldRejectEmptyValues() {
        CreateDeckTypeRequest request = new CreateDeckTypeRequest("Bad Deck", List.of());

        assertThatThrownBy(() -> deckTypeService.create(request, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("INSUFFICIENT_VALUES");
                });
    }

    @Test
    void shouldRejectNullValues() {
        CreateDeckTypeRequest request = new CreateDeckTypeRequest("Bad Deck", null);

        assertThatThrownBy(() -> deckTypeService.create(request, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("INSUFFICIENT_VALUES");
                });
    }

    @Test
    void shouldRejectSingleValue() {
        List<DeckValueRequest> values = List.of(new DeckValueRequest("1", BigDecimal.ONE));
        CreateDeckTypeRequest request = new CreateDeckTypeRequest("Bad Deck", values);

        assertThatThrownBy(() -> deckTypeService.create(request, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("INSUFFICIENT_VALUES");
                });
    }
}
