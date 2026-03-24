package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link InvitationJpaEntity}.
 * Provides standard CRUD plus custom finder methods.
 */
public interface InvitationJpaRepository extends JpaRepository<InvitationJpaEntity, UUID> {

    Optional<InvitationJpaEntity> findByToken(String token);

    List<InvitationJpaEntity> findByRoomId(UUID roomId);

    Optional<InvitationJpaEntity> findByEmailAndRoomIdAndStatus(String email, UUID roomId, InvitationStatus status);
}
