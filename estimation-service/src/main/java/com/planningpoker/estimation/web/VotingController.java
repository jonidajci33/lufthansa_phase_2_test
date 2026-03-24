package com.planningpoker.estimation.web;

import com.planningpoker.estimation.application.port.in.FinishVotingUseCase;
import com.planningpoker.estimation.application.port.in.GetVotesUseCase;
import com.planningpoker.estimation.application.port.in.StartVotingUseCase;
import com.planningpoker.estimation.application.port.in.SubmitVoteUseCase;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.estimation.web.dto.StoryResponse;
import com.planningpoker.estimation.web.dto.SubmitVoteRequest;
import com.planningpoker.estimation.web.dto.VoteResponse;
import com.planningpoker.estimation.web.dto.VotingResultResponse;
import com.planningpoker.estimation.web.mapper.StoryRestMapper;
import com.planningpoker.estimation.web.mapper.VoteRestMapper;
import com.planningpoker.estimation.web.mapper.VotingResultRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for voting operations on a story.
 */
@RestController
@RequestMapping("/api/v1/stories/{storyId}")
@Tag(name = "Voting")
public class VotingController {

    private final StartVotingUseCase startVotingUseCase;
    private final FinishVotingUseCase finishVotingUseCase;
    private final SubmitVoteUseCase submitVoteUseCase;
    private final GetVotesUseCase getVotesUseCase;

    public VotingController(StartVotingUseCase startVotingUseCase,
                            FinishVotingUseCase finishVotingUseCase,
                            SubmitVoteUseCase submitVoteUseCase,
                            GetVotesUseCase getVotesUseCase) {
        this.startVotingUseCase = startVotingUseCase;
        this.finishVotingUseCase = finishVotingUseCase;
        this.submitVoteUseCase = submitVoteUseCase;
        this.getVotesUseCase = getVotesUseCase;
    }

    @PostMapping("/voting/start")
    @Operation(summary = "Start voting", description = "Starts a voting round on a story. Only the room moderator can start voting.")
    @ApiResponse(responseCode = "200", description = "Voting started successfully")
    @ApiResponse(responseCode = "400", description = "Story is not in PENDING status")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<StoryResponse> startVoting(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable UUID storyId) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        Story story = startVotingUseCase.startVoting(storyId, requesterId);
        return ResponseEntity.ok(StoryRestMapper.toResponse(story));
    }

    @PostMapping("/voting/finish")
    @Operation(summary = "Finish voting", description = "Finishes voting and calculates results. Only the room moderator can finish voting.")
    @ApiResponse(responseCode = "200", description = "Voting finished, results returned")
    @ApiResponse(responseCode = "400", description = "Story is not in VOTING status")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<VotingResultResponse> finishVoting(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable UUID storyId) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        VotingResult result = finishVotingUseCase.finishVoting(storyId, requesterId);
        return ResponseEntity.ok(VotingResultRestMapper.toResponse(result));
    }

    @PostMapping("/votes")
    @Operation(summary = "Submit a vote", description = "Submits or replaces a vote on a story that is currently in VOTING status.")
    @ApiResponse(responseCode = "201", description = "Vote submitted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or voting not open")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<VoteResponse> submitVote(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID storyId,
                                                    @Valid @RequestBody SubmitVoteRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Vote vote = submitVoteUseCase.submitVote(request, storyId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(VoteRestMapper.toResponse(vote));
    }

    @GetMapping("/votes")
    @Operation(summary = "Get votes", description = "Returns all votes for a story. Only available after voting is finished (VOTED status).")
    @ApiResponse(responseCode = "200", description = "List of votes")
    @ApiResponse(responseCode = "400", description = "Voting not finished yet")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<List<VoteResponse>> getVotes(@PathVariable UUID storyId) {
        List<Vote> votes = getVotesUseCase.getVotes(storyId);
        List<VoteResponse> response = VoteRestMapper.toResponseList(votes);
        return ResponseEntity.ok(response);
    }
}
