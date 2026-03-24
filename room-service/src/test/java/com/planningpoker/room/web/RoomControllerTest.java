package com.planningpoker.room.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.room.application.port.in.CreateRoomUseCase;
import com.planningpoker.room.application.port.in.DeleteRoomUseCase;
import com.planningpoker.room.application.port.in.GetRoomUseCase;
import com.planningpoker.room.application.port.in.InviteUserUseCase;
import com.planningpoker.room.application.port.in.JoinRoomUseCase;
import com.planningpoker.room.application.port.in.ListParticipantsUseCase;
import com.planningpoker.room.application.port.in.ListUserRoomsUseCase;
import com.planningpoker.room.application.port.in.RemoveParticipantUseCase;
import com.planningpoker.room.application.port.in.UpdateRoomUseCase;
import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import com.planningpoker.room.infrastructure.config.SecurityConfig;
import com.planningpoker.room.web.dto.CreateRoomRequest;
import com.planningpoker.room.web.dto.UpdateRoomRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateRoomUseCase createRoomUseCase;

    @MockitoBean
    private UpdateRoomUseCase updateRoomUseCase;

    @MockitoBean
    private DeleteRoomUseCase deleteRoomUseCase;

    @MockitoBean
    private GetRoomUseCase getRoomUseCase;

    @MockitoBean
    private ListUserRoomsUseCase listUserRoomsUseCase;

    @MockitoBean
    private JoinRoomUseCase joinRoomUseCase;

    @MockitoBean
    private ListParticipantsUseCase listParticipantsUseCase;

    @MockitoBean
    private InviteUserUseCase inviteUserUseCase;

    @MockitoBean
    private RemoveParticipantUseCase removeParticipantUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID ROOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MODERATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String USER_SUB = MODERATOR_ID.toString();
    private static final UUID DECK_TYPE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static Room sampleRoom() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        DeckType deckType = new DeckType(DECK_TYPE_ID, "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(), now);
        RoomParticipant moderator = new RoomParticipant(
                UUID.randomUUID(), ROOM_ID, MODERATOR_ID, "moderator", ParticipantRole.MODERATOR,
                now, null, true
        );
        return new Room(ROOM_ID, "Sprint Planning", "Weekly sprint",
                MODERATOR_ID, deckType, "ABCD1234", RoomStatus.ACTIVE,
                50, new ArrayList<>(List.of(moderator)), now, now);
    }

    // ── POST /api/v1/rooms ───────────────────────────────────────────

    @Test
    void shouldCreateRoomAndReturn201() throws Exception {
        Room room = sampleRoom();
        given(createRoomUseCase.create(any(CreateRoomRequest.class), any(UUID.class), any(String.class))).willReturn(room);

        CreateRoomRequest request = new CreateRoomRequest("Sprint Planning", "Weekly sprint", DECK_TYPE_ID, 50);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ROOM_ID.toString())))
                .andExpect(jsonPath("$.name", is("Sprint Planning")))
                .andExpect(jsonPath("$.description", is("Weekly sprint")))
                .andExpect(jsonPath("$.moderatorId", is(MODERATOR_ID.toString())))
                .andExpect(jsonPath("$.shortCode", is("ABCD1234")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.deckType", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("", "desc", DECK_TYPE_ID, 50);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field", is("name")));
    }

    @Test
    void shouldReturn400WhenDeckTypeIdIsNull() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("Room", "desc", null, 50);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("Room", null, DECK_TYPE_ID, null);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/rooms ────────────────────────────────────────────

    @Test
    void shouldListRoomsWithPagination() throws Exception {
        Room room = sampleRoom();
        Page<Room> page = new Page<>(List.of(room), 1L);
        given(listUserRoomsUseCase.listForUser(any(UUID.class), eq(0), eq(20)))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/rooms")
                        .param("offset", "0")
                        .param("limit", "20")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Sprint Planning")))
                .andExpect(jsonPath("$.meta.total", is(1)))
                .andExpect(jsonPath("$.meta.limit", is(20)))
                .andExpect(jsonPath("$.meta.offset", is(0)))
                .andExpect(jsonPath("$.meta.hasNext", is(false)));
    }

    // ── GET /api/v1/rooms/{id} ───────────────────────────────────────

    @Test
    void shouldGetRoomById() throws Exception {
        Room room = sampleRoom();
        given(getRoomUseCase.getById(ROOM_ID)).willReturn(room);

        mockMvc.perform(get("/api/v1/rooms/{id}", ROOM_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ROOM_ID.toString())))
                .andExpect(jsonPath("$.name", is("Sprint Planning")));
    }

    @Test
    void shouldReturn404WhenRoomNotFound() throws Exception {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        given(getRoomUseCase.getById(missingId))
                .willThrow(new ResourceNotFoundException("Room", missingId));

        mockMvc.perform(get("/api/v1/rooms/{id}", missingId)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("ROOM_NOT_FOUND")));
    }

    // ── PUT /api/v1/rooms/{id} ───────────────────────────────────────

    @Test
    void shouldUpdateRoom() throws Exception {
        Room updatedRoom = sampleRoom();
        given(updateRoomUseCase.update(eq(ROOM_ID), any(UpdateRoomRequest.class), any(UUID.class)))
                .willReturn(updatedRoom);

        UpdateRoomRequest request = new UpdateRoomRequest("Updated Name", "Updated desc", 100);

        mockMvc.perform(put("/api/v1/rooms/{id}", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ROOM_ID.toString())));
    }

    // ── DELETE /api/v1/rooms/{id} ────────────────────────────────────

    @Test
    void shouldDeleteRoomAndReturn204() throws Exception {
        willDoNothing().given(deleteRoomUseCase).delete(eq(ROOM_ID), any(UUID.class));

        mockMvc.perform(delete("/api/v1/rooms/{id}", ROOM_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNoContent());
    }

    // ── POST /api/v1/rooms/{id}/share-link ────────────────────────────

    @Test
    void shouldGenerateShareLinkForModerator() throws Exception {
        Room room = sampleRoom();
        given(getRoomUseCase.getById(ROOM_ID)).willReturn(room);

        mockMvc.perform(post("/api/v1/rooms/{id}/share-link", ROOM_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode", is("ABCD1234")))
                .andExpect(jsonPath("$.shareLink").value(org.hamcrest.Matchers.containsString("/rooms/join/ABCD1234")));
    }

    @Test
    void shouldReturn400WhenNonModeratorGeneratesShareLink() throws Exception {
        Room room = sampleRoom();
        String nonModeratorSub = UUID.randomUUID().toString();
        given(getRoomUseCase.getById(ROOM_ID)).willReturn(room);

        mockMvc.perform(post("/api/v1/rooms/{id}/share-link", ROOM_ID)
                        .with(JwtTestHelper.withUser(nonModeratorSub)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("NOT_MODERATOR")));
    }

    @Test
    void shouldReturn404WhenRoomNotFoundForShareLink() throws Exception {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        given(getRoomUseCase.getById(missingId))
                .willThrow(new ResourceNotFoundException("Room", missingId));

        mockMvc.perform(post("/api/v1/rooms/{id}/share-link", missingId)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/rooms/{roomId}/participants/{userId} ──────────

    @Test
    void shouldRemoveParticipantAndReturn204() throws Exception {
        UUID targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        willDoNothing().given(removeParticipantUseCase)
                .remove(eq(ROOM_ID), eq(targetUserId), any(UUID.class));

        mockMvc.perform(delete("/api/v1/rooms/{roomId}/participants/{userId}", ROOM_ID, targetUserId)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401WhenRemovingWithoutAuth() throws Exception {
        UUID targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000010");

        mockMvc.perform(delete("/api/v1/rooms/{roomId}/participants/{userId}", ROOM_ID, targetUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenNonModeratorRemoves() throws Exception {
        UUID targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        String nonModeratorSub = UUID.randomUUID().toString();

        willThrow(new BusinessException(HttpStatus.FORBIDDEN, "NOT_MODERATOR",
                "Only the room moderator can remove participants"))
                .given(removeParticipantUseCase)
                .remove(eq(ROOM_ID), eq(targetUserId), any(UUID.class));

        mockMvc.perform(delete("/api/v1/rooms/{roomId}/participants/{userId}", ROOM_ID, targetUserId)
                        .with(JwtTestHelper.withUser(nonModeratorSub)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("NOT_MODERATOR")));
    }
}
