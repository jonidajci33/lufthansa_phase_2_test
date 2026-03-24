package com.planningpoker.room.application.port.out;

import com.planningpoker.room.domain.Invitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for invitation persistence.
 * Mirrors the domain {@link com.planningpoker.room.domain.InvitationRepository}
 * contract. Infrastructure adapters implement this interface.
 */
public interface InvitationPersistencePort {

    Optional<Invitation> findById(UUID id);

    Optional<Invitation> findByToken(String token);

    List<Invitation> findByRoomId(UUID roomId);

    Optional<Invitation> findPendingByEmailAndRoomId(String email, UUID roomId);

    Invitation save(Invitation invitation);
}
