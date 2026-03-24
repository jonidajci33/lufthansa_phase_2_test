package com.planningpoker.estimation.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request body for reordering stories within a room.
 */
public record ReorderStoriesRequest(
        @NotEmpty List<UUID> storyIds
) {}
