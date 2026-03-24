package com.planningpoker.estimation.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link StoryJpaEntity}.
 */
public interface StoryJpaRepository extends JpaRepository<StoryJpaEntity, UUID> {

    Page<StoryJpaEntity> findByRoomId(UUID roomId, Pageable pageable);

    List<StoryJpaEntity> findByRoomIdOrderBySortOrderAsc(UUID roomId);

    long countByRoomId(UUID roomId);
}
