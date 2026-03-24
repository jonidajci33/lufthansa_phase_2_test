package com.planningpoker.estimation.application.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Primary port for reordering stories within a room.
 */
public interface ReorderStoriesUseCase {

    void reorder(UUID roomId, List<UUID> storyIds, UUID requesterId);
}
