package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.application.port.out.InvitationPersistencePort;
import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.InvitationRepository;
import com.planningpoker.room.domain.InvitationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements both the domain {@link InvitationRepository}
 * and the application {@link InvitationPersistencePort} by delegating to Spring Data JPA.
 */
@Component
public class InvitationPersistenceAdapter implements InvitationPersistencePort, InvitationRepository {

    private final InvitationJpaRepository jpaRepository;

    public InvitationPersistenceAdapter(InvitationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Invitation> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(InvitationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(InvitationPersistenceMapper::toDomain);
    }

    @Override
    public List<Invitation> findByRoomId(UUID roomId) {
        return jpaRepository.findByRoomId(roomId).stream()
                .map(InvitationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Invitation> findPendingByEmailAndRoomId(String email, UUID roomId) {
        return jpaRepository.findByEmailAndRoomIdAndStatus(email, roomId, InvitationStatus.PENDING)
                .map(InvitationPersistenceMapper::toDomain);
    }

    @Override
    public Invitation save(Invitation invitation) {
        InvitationJpaEntity entity = InvitationPersistenceMapper.toEntity(invitation);
        InvitationJpaEntity saved = jpaRepository.save(entity);
        return InvitationPersistenceMapper.toDomain(saved);
    }
}
