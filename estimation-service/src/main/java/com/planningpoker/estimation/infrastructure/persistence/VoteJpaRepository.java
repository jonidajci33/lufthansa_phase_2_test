package com.planningpoker.estimation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link VoteJpaEntity}.
 */
public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, UUID> {

    List<VoteJpaEntity> findByStoryId(UUID storyId);

    Optional<VoteJpaEntity> findByStoryIdAndUserId(UUID storyId, UUID userId);

    void deleteByStoryId(UUID storyId);
}
