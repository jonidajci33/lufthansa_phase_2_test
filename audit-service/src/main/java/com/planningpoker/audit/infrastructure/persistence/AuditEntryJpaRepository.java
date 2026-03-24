package com.planningpoker.audit.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link AuditEntryJpaEntity}.
 * Provides custom JPQL queries with optional filters.
 */
public interface AuditEntryJpaRepository extends JpaRepository<AuditEntryJpaEntity, Long> {

    boolean existsByEventId(String eventId);

    List<AuditEntryJpaEntity> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, UUID entityId);

    @Query("""
            SELECT a FROM AuditEntryJpaEntity a
            WHERE (:entityType IS NULL OR a.entityType = :entityType)
              AND (:operation IS NULL OR a.operation = :operation)
              AND (:userId IS NULL OR a.userId = :userId)
              AND (CAST(:from AS timestamp) IS NULL OR a.timestamp >= :from)
              AND (CAST(:to AS timestamp) IS NULL OR a.timestamp <= :to)
            ORDER BY a.timestamp DESC
            """)
    Page<AuditEntryJpaEntity> findAllWithFilters(
            @Param("entityType") String entityType,
            @Param("operation") com.planningpoker.audit.domain.AuditOperation operation,
            @Param("userId") UUID userId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(a) FROM AuditEntryJpaEntity a
            WHERE (:entityType IS NULL OR a.entityType = :entityType)
              AND (:operation IS NULL OR a.operation = :operation)
              AND (:userId IS NULL OR a.userId = :userId)
              AND (CAST(:from AS timestamp) IS NULL OR a.timestamp >= :from)
              AND (CAST(:to AS timestamp) IS NULL OR a.timestamp <= :to)
            """)
    long countWithFilters(
            @Param("entityType") String entityType,
            @Param("operation") com.planningpoker.audit.domain.AuditOperation operation,
            @Param("userId") UUID userId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
