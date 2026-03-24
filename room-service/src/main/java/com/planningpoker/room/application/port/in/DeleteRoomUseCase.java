package com.planningpoker.room.application.port.in;

import java.util.UUID;

/**
 * Primary port for deleting a room.
 */
public interface DeleteRoomUseCase {

    void delete(UUID id, UUID requesterId);

    void deleteAsAdmin(UUID id);
}
