package com.planningpoker.estimation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain-level repository port for {@link Vote} persistence.
 * <p>
 * This is a pure interface with ZERO framework dependencies. Infrastructure
 * adapters (JPA, JDBC, in-memory) implement it in the adapter layer.
 */
public interface VoteRepository {

    List<Vote> findByStoryId(UUID storyId);

    Optional<Vote> findByStoryIdAndUserId(UUID storyId, UUID userId);

    Vote save(Vote vote);

    void deleteByStoryId(UUID storyId);
}
