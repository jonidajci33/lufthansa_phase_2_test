package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.application.port.out.DeckTypePersistencePort;
import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements both the domain {@link DeckTypeRepository}
 * and the application {@link DeckTypePersistencePort} by delegating to Spring Data JPA.
 */
@Component
public class DeckTypePersistenceAdapter implements DeckTypePersistencePort, DeckTypeRepository {

    private final DeckTypeJpaRepository jpaRepository;

    public DeckTypePersistenceAdapter(DeckTypeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<DeckType> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(DeckTypePersistenceMapper::toDomain);
    }

    @Override
    public List<DeckType> findAll() {
        return jpaRepository.findAll().stream()
                .map(DeckTypePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<DeckType> findByCategory(DeckCategory category) {
        return jpaRepository.findByCategory(category).stream()
                .map(DeckTypePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public DeckType save(DeckType deckType) {
        DeckTypeJpaEntity entity = DeckTypePersistenceMapper.toEntity(deckType);
        DeckTypeJpaEntity saved = jpaRepository.save(entity);
        return DeckTypePersistenceMapper.toDomain(saved);
    }
}
