package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.Room;
import com.planningpoker.room.web.dto.UpdateRoomRequest;

import java.util.UUID;

/**
 * Primary port for updating a room's editable fields.
 */
public interface UpdateRoomUseCase {

    Room update(UUID id, UpdateRoomRequest request, UUID requesterId);
}
