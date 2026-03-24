package com.planningpoker.estimation.application.port.out;

import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for story persistence.
 * Infrastructure adapters implement this interface.
 */
public interface StoryPersistencePort {

    Optional<Story> findById(UUID id);

    Page<Story> findByRoomId(UUID roomId, int offset, int limit);

    Page<Story> findAll(int offset, int limit);

    List<Story> findByRoomIdOrderBySortOrder(UUID roomId);

    long countByRoomId(UUID roomId);

    Story save(Story story);

    void deleteById(UUID id);
}
