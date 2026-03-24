package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link DeckTypeJpaEntity}.
 * Provides standard CRUD plus custom finder methods.
 */
public interface DeckTypeJpaRepository extends JpaRepository<DeckTypeJpaEntity, UUID> {

    List<DeckTypeJpaEntity> findByCategory(DeckCategory category);
}
