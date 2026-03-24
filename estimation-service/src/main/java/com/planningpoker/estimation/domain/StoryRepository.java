package com.planningpoker.estimation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain-level repository port for {@link Story} aggregate persistence.
 * <p>
 * This is a pure interface with ZERO framework dependencies. Infrastructure
 * adapters (JPA, JDBC, in-memory) implement it in the adapter layer.
 */
public interface StoryRepository {

    Optional<Story> findById(UUID id);

    Page<Story> findByRoomId(UUID roomId, int offset, int limit);

    List<Story> findByRoomIdOrderBySortOrder(UUID roomId);

    Story save(Story story);

    void deleteById(UUID id);

    long countByRoomId(UUID roomId);
}
