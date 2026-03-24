package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.RoomParticipant;

import java.util.UUID;

/**
 * Primary port for joining a room directly (via short code).
 */
public interface JoinRoomUseCase {

    RoomParticipant join(UUID roomId, UUID userId, String username);
}
