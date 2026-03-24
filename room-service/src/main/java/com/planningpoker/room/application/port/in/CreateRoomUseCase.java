package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.Room;
import com.planningpoker.room.web.dto.CreateRoomRequest;

import java.util.UUID;

/**
 * Primary port for creating a new room.
 */
public interface CreateRoomUseCase {

    Room create(CreateRoomRequest request, UUID moderatorId, String username);
}
