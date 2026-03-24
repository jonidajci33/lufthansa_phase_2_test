package com.planningpoker.room.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RoomJpaEntity}.
 * Provides standard CRUD plus custom finder methods.
 */
public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, UUID> {

    Optional<RoomJpaEntity> findByShortCode(String shortCode);

    long countByParticipantsUserId(UUID userId);

    @Query("SELECT r FROM RoomJpaEntity r JOIN r.participants p WHERE p.userId = :userId")
    Page<RoomJpaEntity> findByParticipantsUserId(UUID userId, Pageable pageable);
}
