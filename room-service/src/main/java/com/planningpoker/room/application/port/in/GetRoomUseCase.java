package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.Room;

import java.util.UUID;

/**
 * Primary port for retrieving room details.
 */
public interface GetRoomUseCase {

    Room getById(UUID id);

    Room getByShortCode(String shortCode);
}
