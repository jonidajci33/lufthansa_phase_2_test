package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.Room;

import java.util.UUID;

/**
 * Primary port for listing rooms a user participates in.
 */
public interface ListUserRoomsUseCase {

    Page<Room> listForUser(UUID userId, int offset, int limit);

    Page<Room> listAll(int offset, int limit);
}
