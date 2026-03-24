package com.planningpoker.room.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain-level repository port for {@link Room} aggregate persistence.
 * <p>
 * This is a pure interface with ZERO framework dependencies. Infrastructure
 * adapters (JPA, JDBC, in-memory) implement it in the adapter layer.
 */
public interface RoomRepository {

    Optional<Room> findById(UUID id);

    Optional<Room> findByShortCode(String shortCode);

    Room save(Room room);

    void delete(Room room);

    Page<Room> findByParticipantUserId(UUID userId, int offset, int limit);

    long count();
}
