package com.planningpoker.estimation.application.port.in;

import java.util.UUID;

/**
 * Primary port for deleting a story.
 */
public interface DeleteStoryUseCase {

    void delete(UUID id, UUID requesterId);
}
