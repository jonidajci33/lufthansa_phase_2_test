package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;

import java.util.UUID;

/**
 * Primary port for listing stories in a room with pagination.
 */
public interface ListStoriesUseCase {

    Page<Story> listByRoom(UUID roomId, int offset, int limit);

    Page<Story> listAll(int offset, int limit);
}
