package com.planningpoker.room.application.port.in;

import java.util.UUID;

/**
 * Primary port for removing a participant from a room.
 * Only the room moderator can perform this operation.
 */
public interface RemoveParticipantUseCase {

    void remove(UUID roomId, UUID targetUserId, UUID requesterId);
}
