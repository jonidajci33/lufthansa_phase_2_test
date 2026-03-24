package com.planningpoker.room.web;

import com.planningpoker.room.application.port.in.GetRoomUseCase;
import com.planningpoker.room.application.port.in.ListParticipantsUseCase;
import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import com.planningpoker.room.infrastructure.config.SecurityConfig;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalRoomController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class InternalRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRoomUseCase getRoomUseCase;

    @MockitoBean
    private ListParticipantsUseCase listParticipantsUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID ROOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MODERATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static Room sampleRoom() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        DeckType deckType = new DeckType(UUID.randomUUID(), "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(), now);
        RoomParticipant moderator = new RoomParticipant(
                UUID.randomUUID(), ROOM_ID, MODERATOR_ID, "moderator", ParticipantRole.MODERATOR,
                now, null, true
        );
        return new Room(ROOM_ID, "Sprint Planning", "Weekly sprint",
                MODERATOR_ID, deckType, "ABCD1234", RoomStatus.ACTIVE,
                50, new ArrayList<>(List.of(moderator)), now, now);
    }

    // ── GET /internal/rooms/{id} ─────────────────────────────────────

    @Test
    void shouldGetInternalRoomWithoutAuth() throws Exception {
        Room room = sampleRoom();
        given(getRoomUseCase.getById(ROOM_ID)).willReturn(room);

        mockMvc.perform(get("/internal/rooms/{id}", ROOM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ROOM_ID.toString())))
                .andExpect(jsonPath("$.name", is("Sprint Planning")))
                .andExpect(jsonPath("$.moderatorId", is(MODERATOR_ID.toString())))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.deckType.name", is("Fibonacci")));
    }

    @Test
    void shouldReturn404WhenInternalRoomNotFound() throws Exception {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        given(getRoomUseCase.getById(missingId))
                .willThrow(new ResourceNotFoundException("Room", missingId));

        mockMvc.perform(get("/internal/rooms/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("ROOM_NOT_FOUND")));
    }

    // ── GET /internal/rooms/{id}/participants ─────────────────────────

    @Test
    void shouldGetInternalParticipantsWithoutAuth() throws Exception {
        Room room = sampleRoom();
        given(listParticipantsUseCase.listParticipants(ROOM_ID))
                .willReturn(room.getParticipants());

        mockMvc.perform(get("/internal/rooms/{id}/participants", ROOM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(MODERATOR_ID.toString())))
                .andExpect(jsonPath("$[0].role", is("MODERATOR")));
    }
}
