package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.application.port.out.StoryPersistencePort;
import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements {@link StoryPersistencePort}
 * by delegating to Spring Data JPA.
 */
@Component
public class StoryPersistenceAdapter implements StoryPersistencePort {

    private final StoryJpaRepository jpaRepository;

    public StoryPersistenceAdapter(StoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Story> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(StoryPersistenceMapper::toDomain);
    }

    @Override
    public Page<Story> findByRoomId(UUID roomId, int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);
        org.springframework.data.domain.Page<StoryJpaEntity> jpaPage =
                jpaRepository.findByRoomId(roomId, PageRequest.of(pageNumber, limit));

        return new Page<>(
                StoryPersistenceMapper.toDomainList(jpaPage.getContent()),
                jpaPage.getTotalElements()
        );
    }

    @Override
    public Page<Story> findAll(int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);
        org.springframework.data.domain.Page<StoryJpaEntity> jpaPage =
                jpaRepository.findAll(PageRequest.of(pageNumber, limit));

        return new Page<>(
                StoryPersistenceMapper.toDomainList(jpaPage.getContent()),
                jpaPage.getTotalElements()
        );
    }

    @Override
    public List<Story> findByRoomIdOrderBySortOrder(UUID roomId) {
        return StoryPersistenceMapper.toDomainList(
                jpaRepository.findByRoomIdOrderBySortOrderAsc(roomId));
    }

    @Override
    public long countByRoomId(UUID roomId) {
        return jpaRepository.countByRoomId(roomId);
    }

    @Override
    public Story save(Story story) {
        StoryJpaEntity entity = StoryPersistenceMapper.toEntity(story);
        StoryJpaEntity saved = jpaRepository.save(entity);
        return StoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
