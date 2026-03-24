package com.planningpoker.room.application.port.out;

import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;

/**
 * Secondary (driven) port for publishing room-related domain events.
 * The infrastructure adapter translates these calls into Kafka messages
 * on the {@code room.events} topic.
 */
public interface RoomEventPublisherPort {

    void publishRoomCreated(Room room);

    void publishRoomUpdated(Room room);

    void publishRoomDeleted(Room room);

    void publishUserInvited(Invitation invitation, String roomName);

    void publishUserJoinedRoom(Room room, RoomParticipant participant);

    void publishUserLeftRoom(Room room, RoomParticipant participant);
}
