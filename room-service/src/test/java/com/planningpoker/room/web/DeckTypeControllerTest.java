package com.planningpoker.room.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.room.application.port.in.CreateDeckTypeUseCase;
import com.planningpoker.room.application.port.in.ListDeckTypesUseCase;
import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
import com.planningpoker.room.infrastructure.config.SecurityConfig;
import com.planningpoker.room.web.dto.CreateDeckTypeRequest;
import com.planningpoker.room.web.dto.DeckValueRequest;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeckTypeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DeckTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ListDeckTypesUseCase listDeckTypesUseCase;

    @MockitoBean
    private CreateDeckTypeUseCase createDeckTypeUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID DECK_TYPE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String USER_SUB = UUID.randomUUID().toString();

    private static DeckType sampleDeckType() {
        DeckValue v1 = new DeckValue(UUID.randomUUID(), "1", BigDecimal.ONE, 0);
        DeckValue v2 = new DeckValue(UUID.randomUUID(), "2", BigDecimal.valueOf(2), 1);
        return new DeckType(DECK_TYPE_ID, "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(v1, v2), Instant.parse("2026-01-01T00:00:00Z"));
    }

    // ── GET /api/v1/deck-types ───────────────────────────────────────

    @Test
    void shouldListDeckTypes() throws Exception {
        given(listDeckTypesUseCase.listAll()).willReturn(List.of(sampleDeckType()));

        mockMvc.perform(get("/api/v1/deck-types")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(DECK_TYPE_ID.toString())))
                .andExpect(jsonPath("$[0].name", is("Fibonacci")))
                .andExpect(jsonPath("$[0].category", is("FIBONACCI")))
                .andExpect(jsonPath("$[0].isSystem", is(true)))
                .andExpect(jsonPath("$[0].values", hasSize(2)));
    }

    // ── POST /api/v1/deck-types ──────────────────────────────────────

    @Test
    void shouldCreateCustomDeckTypeAndReturn201() throws Exception {
        DeckType created = new DeckType(UUID.randomUUID(), "Custom", DeckCategory.CUSTOM,
                false, UUID.randomUUID(),
                List.of(new DeckValue(UUID.randomUUID(), "S", null, 0),
                        new DeckValue(UUID.randomUUID(), "M", null, 1)),
                Instant.now());
        given(createDeckTypeUseCase.create(any(CreateDeckTypeRequest.class), any(UUID.class)))
                .willReturn(created);

        CreateDeckTypeRequest request = new CreateDeckTypeRequest("Custom",
                List.of(new DeckValueRequest("S", null), new DeckValueRequest("M", null)));

        mockMvc.perform(post("/api/v1/deck-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Custom")))
                .andExpect(jsonPath("$.category", is("CUSTOM")))
                .andExpect(jsonPath("$.isSystem", is(false)))
                .andExpect(jsonPath("$.values", hasSize(2)));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateDeckTypeRequest request = new CreateDeckTypeRequest("",
                List.of(new DeckValueRequest("S", null)));

        mockMvc.perform(post("/api/v1/deck-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
    }

    @Test
    void shouldReturn400WhenValuesAreEmpty() throws Exception {
        CreateDeckTypeRequest request = new CreateDeckTypeRequest("Custom", List.of());

        mockMvc.perform(post("/api/v1/deck-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
    }
}
