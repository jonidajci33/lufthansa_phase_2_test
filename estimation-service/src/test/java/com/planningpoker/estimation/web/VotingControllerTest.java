package com.planningpoker.estimation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.estimation.application.port.in.FinishVotingUseCase;
import com.planningpoker.estimation.application.port.in.GetVotesUseCase;
import com.planningpoker.estimation.application.port.in.StartVotingUseCase;
import com.planningpoker.estimation.application.port.in.SubmitVoteUseCase;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.estimation.infrastructure.config.SecurityConfig;
import com.planningpoker.estimation.web.dto.SubmitVoteRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotingController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class VotingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StartVotingUseCase startVotingUseCase;

    @MockitoBean
    private FinishVotingUseCase finishVotingUseCase;

    @MockitoBean
    private SubmitVoteUseCase submitVoteUseCase;

    @MockitoBean
    private GetVotesUseCase getVotesUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID STORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ROOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String USER_SUB = USER_ID.toString();

    private static Story votingStory() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Story(STORY_ID, ROOM_ID, "Login Page", "Desc",
                StoryStatus.VOTING, 0, null, false,
                new ArrayList<>(), now, now);
    }

    private static Vote sampleVote() {
        return new Vote(UUID.randomUUID(), STORY_ID, USER_ID, "5",
                new BigDecimal("5"), true, Instant.now(), Instant.now());
    }

    // ── POST /api/v1/stories/{storyId}/voting/start ──────────────────

    @Test
    void shouldStartVotingAndReturn200() throws Exception {
        Story story = votingStory();
        given(startVotingUseCase.startVoting(eq(STORY_ID), any(UUID.class)))
                .willReturn(story);

        mockMvc.perform(post("/api/v1/stories/{storyId}/voting/start", STORY_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(STORY_ID.toString())))
                .andExpect(jsonPath("$.status", is("VOTING")));
    }

    // ── POST /api/v1/stories/{storyId}/voting/finish ─────────────────

    @Test
    void shouldFinishVotingAndReturnResult() throws Exception {
        VotingResult result = new VotingResult(
                STORY_ID, new BigDecimal("5.00"), 3, true);

        given(finishVotingUseCase.finishVoting(eq(STORY_ID), any(UUID.class)))
                .willReturn(result);

        mockMvc.perform(post("/api/v1/stories/{storyId}/voting/finish", STORY_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storyId", is(STORY_ID.toString())))
                .andExpect(jsonPath("$.averageScore", is(5.0)))
                .andExpect(jsonPath("$.consensusReached", is(true)))
                .andExpect(jsonPath("$.totalVotes", is(3)));
    }

    // ── POST /api/v1/stories/{storyId}/votes ─────────────────────────

    @Test
    void shouldSubmitVoteAndReturn201() throws Exception {
        Vote vote = sampleVote();
        given(submitVoteUseCase.submitVote(any(SubmitVoteRequest.class), eq(STORY_ID), any(UUID.class))).willReturn(vote);

        SubmitVoteRequest request = new SubmitVoteRequest("5", new BigDecimal("5"));

        mockMvc.perform(post("/api/v1/stories/{storyId}/votes", STORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(USER_ID.toString())))
                .andExpect(jsonPath("$.value", is("5")))
                .andExpect(jsonPath("$.numericValue", is(5)));
    }

    @Test
    void shouldReturn400WhenVoteValueIsBlank() throws Exception {
        SubmitVoteRequest request = new SubmitVoteRequest("", new BigDecimal("5"));

        mockMvc.perform(post("/api/v1/stories/{storyId}/votes", STORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field", is("value")));
    }

    // ── GET /api/v1/stories/{storyId}/votes ──────────────────────────

    @Test
    void shouldGetVotesAndReturn200() throws Exception {
        Vote vote = sampleVote();
        given(getVotesUseCase.getVotes(STORY_ID)).willReturn(List.of(vote));

        mockMvc.perform(get("/api/v1/stories/{storyId}/votes", STORY_ID)
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].value", is("5")));
    }

    // ── Authentication ───────────────────────────────────────────────

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/stories/{storyId}/voting/start", STORY_ID))
                .andExpect(status().isUnauthorized());
    }
}
