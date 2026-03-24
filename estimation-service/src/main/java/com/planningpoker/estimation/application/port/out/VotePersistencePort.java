package com.planningpoker.estimation.application.port.out;

import com.planningpoker.estimation.domain.Vote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for vote persistence.
 * Infrastructure adapters implement this interface.
 */
public interface VotePersistencePort {

    List<Vote> findByStoryId(UUID storyId);

    Optional<Vote> findByStoryIdAndUserId(UUID storyId, UUID userId);

    Vote save(Vote vote);

    void deleteByStoryId(UUID storyId);
}
