package com.planningpoker.room.application.port.in;

import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.web.dto.InviteRequest;

import java.util.UUID;

/**
 * Primary port for inviting a user to a room.
 */
public interface InviteUserUseCase {

    Invitation invite(InviteRequest request, UUID roomId, UUID inviterId);
}
