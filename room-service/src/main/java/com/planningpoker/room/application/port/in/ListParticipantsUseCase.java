package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.RoomParticipant;

import java.util.List;
import java.util.UUID;

/**
 * Primary port for listing participants in a room.
 */
public interface ListParticipantsUseCase {

    List<RoomParticipant> listParticipants(UUID roomId);
}
