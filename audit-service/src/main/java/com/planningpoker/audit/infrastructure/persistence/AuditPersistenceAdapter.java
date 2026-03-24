package com.planningpoker.audit.infrastructure.persistence;

import com.planningpoker.audit.application.port.out.AuditPersistencePort;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.AuditRepository;
import com.planningpoker.audit.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements both the domain {@link AuditRepository}
 * and the application {@link AuditPersistencePort} by delegating to Spring Data JPA.
 */
@Component
public class AuditPersistenceAdapter implements AuditPersistencePort, AuditRepository {

    private final AuditEntryJpaRepository jpaRepository;

    public AuditPersistenceAdapter(AuditEntryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditEntry save(AuditEntry entry) {
        AuditEntryJpaEntity entity = AuditEntryPersistenceMapper.toEntity(entry);
        AuditEntryJpaEntity saved = jpaRepository.save(entity);
        return AuditEntryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<AuditEntry> findById(Long id) {
        return jpaRepository.findById(id)
                .map(AuditEntryPersistenceMapper::toDomain);
    }

    @Override
    public Page<AuditEntry> findAll(AuditFilter filter, int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);

        org.springframework.data.domain.Page<AuditEntryJpaEntity> jpaPage =
                jpaRepository.findAllWithFilters(
                        filter.entityType(),
                        filter.operation(),
                        filter.userId(),
                        filter.from(),
                        filter.to(),
                        PageRequest.of(pageNumber, limit)
                );

        return new Page<>(
                AuditEntryPersistenceMapper.toDomainList(jpaPage.getContent()),
                jpaPage.getTotalElements()
        );
    }

    @Override
    public List<AuditEntry> findByEntityTypeAndEntityId(String entityType, UUID entityId) {
        return AuditEntryPersistenceMapper.toDomainList(
                jpaRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId));
    }

    @Override
    public long count(AuditFilter filter) {
        return jpaRepository.countWithFilters(
                filter.entityType(),
                filter.operation(),
                filter.userId(),
                filter.from(),
                filter.to()
        );
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }
}
