package com.planningpoker.identity.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain-level repository port for {@link User} aggregate persistence.
 * <p>
 * This is a pure interface with ZERO framework dependencies. Infrastructure
 * adapters (JPA, JDBC, in-memory) implement it in the adapter layer.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User save(User user);

    Page<User> findAll(int offset, int limit);

    long count();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
