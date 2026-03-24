package com.planningpoker.room.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain-level repository port for {@link Invitation} persistence.
 * <p>
 * This is a pure interface with ZERO framework dependencies. Infrastructure
 * adapters implement it in the adapter layer.
 */
public interface InvitationRepository {

    Optional<Invitation> findById(UUID id);

    Optional<Invitation> findByToken(String token);

    List<Invitation> findByRoomId(UUID roomId);

    Optional<Invitation> findPendingByEmailAndRoomId(String email, UUID roomId);

    Invitation save(Invitation invitation);
}
