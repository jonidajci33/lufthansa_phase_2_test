package com.planningpoker.estimation.application.port.in;

import com.planningpoker.estimation.domain.Story;

import java.util.List;
import java.util.UUID;

/**
 * Primary port for retrieving story details.
 */
public interface GetStoryUseCase {

    Story getById(UUID id);

    List<Story> getByRoomId(UUID roomId);
}
