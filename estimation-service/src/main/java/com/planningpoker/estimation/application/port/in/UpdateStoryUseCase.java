package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.web.dto.UpdateStoryRequest;

import java.util.UUID;

/**
 * Primary port for updating a story's editable fields.
 */
public interface UpdateStoryUseCase {

    Story update(UUID id, UpdateStoryRequest request, UUID requesterId);
}
