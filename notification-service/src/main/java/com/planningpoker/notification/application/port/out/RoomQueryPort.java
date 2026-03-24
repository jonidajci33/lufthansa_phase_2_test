package com.planningpoker.notification.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Port for querying room information from the Room Service.
 */
public interface RoomQueryPort {

    List<UUID> getParticipantUserIds(UUID roomId);

    RoomInfo getRoom(UUID roomId);

    record RoomInfo(UUID id, String name, UUID moderatorId, String status) {}
}
