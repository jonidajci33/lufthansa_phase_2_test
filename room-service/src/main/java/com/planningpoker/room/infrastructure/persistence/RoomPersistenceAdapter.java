package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.application.port.out.RoomPersistencePort;
import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements both the domain {@link RoomRepository}
 * and the application {@link RoomPersistencePort} by delegating to Spring Data JPA.
 */
@Component
public class RoomPersistenceAdapter implements RoomPersistencePort, RoomRepository {

    private final RoomJpaRepository jpaRepository;

    public RoomPersistenceAdapter(RoomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Room> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(RoomPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Room> findByShortCode(String shortCode) {
        return jpaRepository.findByShortCode(shortCode)
                .map(RoomPersistenceMapper::toDomain);
    }

    @Override
    public Room save(Room room) {
        RoomJpaEntity entity = RoomPersistenceMapper.toEntity(room);
        RoomJpaEntity saved = jpaRepository.save(entity);
        return RoomPersistenceMapper.toDomain(saved);
    }

    @Override
    public void delete(Room room) {
        jpaRepository.deleteById(room.getId());
    }

    @Override
    public Page<Room> findByParticipantUserId(UUID userId, int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);
        org.springframework.data.domain.Page<RoomJpaEntity> jpaPage =
                jpaRepository.findByParticipantsUserId(userId, PageRequest.of(pageNumber, limit));

        return new Page<>(
                jpaPage.getContent().stream()
                        .map(RoomPersistenceMapper::toDomain)
                        .toList(),
                jpaPage.getTotalElements()
        );
    }

    @Override
    public Page<Room> findAll(int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);
        org.springframework.data.domain.Page<RoomJpaEntity> jpaPage =
                jpaRepository.findAll(PageRequest.of(pageNumber, limit));

        return new Page<>(
                jpaPage.getContent().stream()
                        .map(RoomPersistenceMapper::toDomain)
                        .toList(),
                jpaPage.getTotalElements()
        );
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
