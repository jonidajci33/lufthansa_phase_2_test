package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.RoomParticipant;

import java.util.UUID;

/**
 * Primary port for accepting an invitation via token.
 */
public interface AcceptInvitationUseCase {

    RoomParticipant accept(String token, UUID userId, String username);
}
