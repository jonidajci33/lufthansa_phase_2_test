package com.planningpoker.estimation.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Secondary (driven) port for validating room-related data against the Room Service.
 */
public interface RoomValidationPort {

    boolean roomExists(UUID roomId);

    boolean isModeratorOf(UUID roomId, UUID userId);

    List<UUID> getParticipantUserIds(UUID roomId);
}
