package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import com.planningpoker.room.web.dto.DeckTypeResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeckTypeRestMapperTest {

    @Test
    void toResponse_shouldReturnNull_whenDomainIsNull() {
        assertThat(DeckTypeRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        DeckType domain = createDomain();

        DeckTypeResponse result = DeckTypeRestMapper.toResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(domain.getId());
        assertThat(result.name()).isEqualTo(domain.getName());
        assertThat(result.category()).isEqualTo(domain.getCategory());
        assertThat(result.isSystem()).isEqualTo(domain.isSystem());
        assertThat(result.values()).hasSize(2);
        assertThat(result.values().get(0).label()).isEqualTo("5");
        assertThat(result.values().get(0).numericValue()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(result.values().get(1).label()).isEqualTo("8");
    }

    @Test
    void toResponse_shouldHandleEmptyValues() {
        DeckType domain = new DeckType(
                UUID.randomUUID(), "Empty", DeckCategory.CUSTOM,
                false, UUID.randomUUID(), List.of(), Instant.now()
        );

        DeckTypeResponse result = DeckTypeRestMapper.toResponse(domain);

        assertThat(result.values()).isEmpty();
    }

    @Test
    void toResponseList_shouldReturnEmptyList_whenNull() {
        assertThat(DeckTypeRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_shouldMapAllElements() {
        List<DeckType> domains = List.of(createDomain(), createDomain());

        List<DeckTypeResponse> result = DeckTypeRestMapper.toResponseList(domains);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static DeckType createDomain() {
        DeckValue v1 = new DeckValue(UUID.randomUUID(), "5", new BigDecimal("5"), 3);
        DeckValue v2 = new DeckValue(UUID.randomUUID(), "8", new BigDecimal("8"), 4);
        return new DeckType(
                UUID.randomUUID(), "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(v1, v2), Instant.now()
        );
    }
}
