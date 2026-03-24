package com.planningpoker.identity.application.port.out;

import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for user persistence.
 * Mirrors the domain {@link com.planningpoker.identity.domain.UserRepository}
 * contract. Infrastructure adapters implement this interface.
 */
public interface UserPersistencePort {

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
