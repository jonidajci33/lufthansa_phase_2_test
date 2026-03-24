package com.planningpoker.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link EmailLogJpaEntity}.
 */
public interface EmailLogJpaRepository extends JpaRepository<EmailLogJpaEntity, UUID> {
}
