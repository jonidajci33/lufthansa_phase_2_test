package com.planningpoker.estimation.web;

import com.planningpoker.estimation.application.port.in.CreateStoryUseCase;
import com.planningpoker.estimation.application.port.in.DeleteStoryUseCase;
import com.planningpoker.estimation.application.port.in.GetStoryUseCase;
import com.planningpoker.estimation.application.port.in.ListStoriesUseCase;
import com.planningpoker.estimation.application.port.in.ReorderStoriesUseCase;
import com.planningpoker.estimation.application.port.in.UpdateStoryUseCase;
import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.web.dto.CreateStoryRequest;
import com.planningpoker.estimation.web.dto.ReorderStoriesRequest;
import com.planningpoker.estimation.web.dto.StoryResponse;
import com.planningpoker.estimation.web.dto.UpdateStoryRequest;
import com.planningpoker.estimation.web.mapper.StoryRestMapper;
import com.planningpoker.shared.error.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for story management operations.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Stories")
public class StoryController {

    private final CreateStoryUseCase createStoryUseCase;
    private final GetStoryUseCase getStoryUseCase;
    private final ListStoriesUseCase listStoriesUseCase;
    private final UpdateStoryUseCase updateStoryUseCase;
    private final DeleteStoryUseCase deleteStoryUseCase;
    private final ReorderStoriesUseCase reorderStoriesUseCase;

    public StoryController(CreateStoryUseCase createStoryUseCase,
                           GetStoryUseCase getStoryUseCase,
                           ListStoriesUseCase listStoriesUseCase,
                           UpdateStoryUseCase updateStoryUseCase,
                           DeleteStoryUseCase deleteStoryUseCase,
                           ReorderStoriesUseCase reorderStoriesUseCase) {
        this.createStoryUseCase = createStoryUseCase;
        this.getStoryUseCase = getStoryUseCase;
        this.listStoriesUseCase = listStoriesUseCase;
        this.updateStoryUseCase = updateStoryUseCase;
        this.deleteStoryUseCase = deleteStoryUseCase;
        this.reorderStoriesUseCase = reorderStoriesUseCase;
    }

    @PostMapping("/stories")
    @Operation(summary = "Create a new story", description = "Creates a new story in a room. Only the room moderator can create stories.")
    @ApiResponse(responseCode = "201", description = "Story created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    public ResponseEntity<StoryResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody CreateStoryRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        Story story = createStoryUseCase.create(request, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(StoryRestMapper.toResponse(story));
    }

    @GetMapping("/stories")
    @Operation(summary = "List all stories (admin)", description = "Returns a paginated list of all stories across all rooms. Admin access only.")
    @ApiResponse(responseCode = "200", description = "Paginated list of stories")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<PageResponse<StoryResponse>> listAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        Page<Story> page = listStoriesUseCase.listAll(offset, limit);

        List<StoryResponse> data = StoryRestMapper.toResponseList(page.content());

        return ResponseEntity.ok(PageResponse.of(data, page.totalElements(), limit, offset));
    }

    @GetMapping("/rooms/{roomId}/stories")
    @Operation(summary = "List stories in a room", description = "Returns a paginated list of stories in a room.")
    @ApiResponse(responseCode = "200", description = "Paginated list of stories")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<PageResponse<StoryResponse>> listByRoom(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        Page<Story> page = listStoriesUseCase.listByRoom(roomId, offset, limit);

        List<StoryResponse> data = StoryRestMapper.toResponseList(page.content());

        return ResponseEntity.ok(PageResponse.of(data, page.totalElements(), limit, offset));
    }

    @GetMapping("/stories/{id}")
    @Operation(summary = "Get story by ID", description = "Returns a story by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Story found")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<StoryResponse> getById(@PathVariable UUID id) {
        Story story = getStoryUseCase.getById(id);
        return ResponseEntity.ok(StoryRestMapper.toResponse(story));
    }

    @PutMapping("/stories/{id}")
    @Operation(summary = "Update story", description = "Updates a story's editable fields. Only the room moderator can update.")
    @ApiResponse(responseCode = "200", description = "Story updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<StoryResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody UpdateStoryRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        Story story = updateStoryUseCase.update(id, request, requesterId);
        return ResponseEntity.ok(StoryRestMapper.toResponse(story));
    }

    @DeleteMapping("/stories/{id}")
    @Operation(summary = "Delete story", description = "Deletes a story. Only the room moderator can delete.")
    @ApiResponse(responseCode = "204", description = "Story deleted successfully")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Story not found")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID id) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        deleteStoryUseCase.delete(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/rooms/{roomId}/stories/reorder")
    @Operation(summary = "Reorder stories", description = "Reorders stories within a room. Only the room moderator can reorder.")
    @ApiResponse(responseCode = "200", description = "Stories reordered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    public ResponseEntity<Void> reorder(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable UUID roomId,
                                         @Valid @RequestBody ReorderStoriesRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        reorderStoriesUseCase.reorder(roomId, request.storyIds(), requesterId);
        return ResponseEntity.ok().build();
    }
}
