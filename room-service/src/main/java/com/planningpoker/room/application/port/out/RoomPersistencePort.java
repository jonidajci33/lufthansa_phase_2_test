package com.planningpoker.room.application.port.out;

import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.Room;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for room persistence.
 * Mirrors the domain {@link com.planningpoker.room.domain.RoomRepository}
 * contract. Infrastructure adapters implement this interface.
 */
public interface RoomPersistencePort {

    Optional<Room> findById(UUID id);

    Optional<Room> findByShortCode(String shortCode);

    Room save(Room room);

    void delete(Room room);

    Page<Room> findByParticipantUserId(UUID userId, int offset, int limit);

    Page<Room> findAll(int offset, int limit);

    long count();
}
