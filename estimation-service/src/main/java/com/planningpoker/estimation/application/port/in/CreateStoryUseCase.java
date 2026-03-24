package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.web.dto.CreateStoryRequest;

import java.util.UUID;

/**
 * Primary port for creating a new story in a room.
 */
public interface CreateStoryUseCase {

    Story create(CreateStoryRequest request, UUID requesterId);
}
