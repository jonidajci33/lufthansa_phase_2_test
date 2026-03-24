package com.planningpoker.estimation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.estimation.application.port.in.CreateStoryUseCase;
import com.planningpoker.estimation.application.port.in.DeleteStoryUseCase;
import com.planningpoker.estimation.application.port.in.GetStoryUseCase;
import com.planningpoker.estimation.application.port.in.ListStoriesUseCase;
import com.planningpoker.estimation.application.port.in.ReorderStoriesUseCase;
import com.planningpoker.estimation.application.port.in.UpdateStoryUseCase;
import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.infrastructure.config.SecurityConfig;
import com.planningpoker.estimation.web.dto.CreateStoryRequest;
import com.planningpoker.estimation.web.dto.ReorderStoriesRequest;
import com.planningpoker.estimation.web.dto.UpdateStoryRequest;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateStoryUseCase createStoryUseCase;

    @MockitoBean
    private GetStoryUseCase getStoryUseCase;

    @MockitoBean
    private ListStoriesUseCase listStoriesUseCase;

    @MockitoBean
    private UpdateStoryUseCase updateStoryUseCase;

    @MockitoBean
    private DeleteStoryUseCase deleteStoryUseCase;

    @MockitoBean
    private ReorderStoriesUseCase reorderStoriesUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID STORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ROOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String USER_SUB = USER_ID.toString();

    private static Story sampleStory() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Story(STORY_ID, ROOM_ID, "Login Page", "Estimate the login page",
                StoryStatus.PENDING, 0, null, false,
                new ArrayList<>(), now, now);
    }

    // ── POST /api/v1/stories ─────────────────────────────────────────

    @Test
    void shouldCreateStoryAndReturn201() throws Exception {
        Story story = sampleStory();
        given(createStoryUseCase.create(any(CreateStoryRequest.class), any(UUID.class))).willReturn(story);

        CreateStoryRequest request = new CreateStoryRequest(ROOM_ID, "Login Page", "Estimate the login page");

        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(STORY_ID.toString())))
                .andExpect(jsonPath("$.roomId", is(ROOM_ID.toString())))
                .andExpect(jsonPath("$.title", is("Login Page")))
                .andExpect(jsonPath("$.description", is("Estimate the login page")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(ROOM_ID, "", "desc");

        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field", is("title")));
    }

    @Test
    void shouldReturn400WhenRoomIdIsNull() throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(null, "Title", "desc");

        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
    }

    // ── GET /api/v1/rooms/{roomId}/stories ───────────────────────────

    @Test
    void shouldListStoriesWithPagination() throws Exception {
        Story story = sampleStory();
        Page<Story> page = new Page<>(List.of(story), 1L);
        given(listStoriesUseCase.listByRoom(eq(ROOM_ID), eq(0), eq(20)))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/rooms/{roomId}/stories", ROOM_ID)
                        .param("offset", "0")
                        .param("limit", "20")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("Login Page")))
                .andExpect(jsonPath("$.meta.total", is(1)))
                .andExpect(jsonPath("$.meta.limit", is(20)))
                .andExpect(jsonPath("$.meta.offset", is(0)))
                .andExpect(jsonPath("$.meta.hasNext", is(false)));
    }

    // ── GET /api/v1/stories/{id} ─────────────────────────────────────

    @Test
    void shouldGetStoryById() throws Exception {
        Story story = sampleStory();
        given(getStoryUseCase.getById(STORY_ID)).willReturn(story);

        mockMvc.perform(get("/api/v1/stories/{id}", STORY_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(STORY_ID.toString())))
                .andExpect(jsonPath("$.title", is("Login Page")));
    }

    @Test
    void shouldReturn404WhenStoryNotFound() throws Exception {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        given(getStoryUseCase.getById(missingId))
                .willThrow(new ResourceNotFoundException("Story", missingId));

        mockMvc.perform(get("/api/v1/stories/{id}", missingId)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("STORY_NOT_FOUND")));
    }

    // ── PUT /api/v1/stories/{id} ─────────────────────────────────────

    @Test
    void shouldUpdateStory() throws Exception {
        Story updatedStory = sampleStory();
        given(updateStoryUseCase.update(eq(STORY_ID), any(UpdateStoryRequest.class), any(UUID.class)))
                .willReturn(updatedStory);

        UpdateStoryRequest request = new UpdateStoryRequest("Updated Title", "Updated Desc");

        mockMvc.perform(put("/api/v1/stories/{id}", STORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(STORY_ID.toString())));
    }

    // ── DELETE /api/v1/stories/{id} ──────────────────────────────────

    @Test
    void shouldDeleteStoryAndReturn204() throws Exception {
        willDoNothing().given(deleteStoryUseCase).delete(eq(STORY_ID), any(UUID.class));

        mockMvc.perform(delete("/api/v1/stories/{id}", STORY_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNoContent());
    }

    // ── PATCH /api/v1/rooms/{roomId}/stories/reorder ─────────────────

    @Test
    void shouldReorderStories() throws Exception {
        UUID storyId1 = UUID.randomUUID();
        UUID storyId2 = UUID.randomUUID();
        ReorderStoriesRequest request = new ReorderStoriesRequest(List.of(storyId2, storyId1));

        willDoNothing().given(reorderStoriesUseCase)
                .reorder(eq(ROOM_ID), any(List.class), any(UUID.class));

        mockMvc.perform(patch("/api/v1/rooms/{roomId}/stories/reorder", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk());
    }

    // ── Authentication ───────────────────────────────────────────────

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        CreateStoryRequest request = new CreateStoryRequest(ROOM_ID, "Title", null);

        mockMvc.perform(post("/api/v1/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
