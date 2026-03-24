package com.planningpoker.identity.infrastructure.persistence;

import com.planningpoker.identity.application.port.out.UserPersistencePort;
import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements both the domain {@link UserRepository}
 * and the application {@link UserPersistencePort} by delegating to Spring Data JPA.
 */
@Component
public class UserPersistenceAdapter implements UserPersistencePort, UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByKeycloakId(String keycloakId) {
        return jpaRepository.findByKeycloakId(keycloakId)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserPersistenceMapper.toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public Page<User> findAll(int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);
        org.springframework.data.domain.Page<UserJpaEntity> jpaPage =
                jpaRepository.findAll(PageRequest.of(pageNumber, limit));

        return new Page<>(
                jpaPage.getContent().stream()
                        .map(UserPersistenceMapper::toDomain)
                        .toList(),
                jpaPage.getTotalElements()
        );
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
