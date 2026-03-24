package com.planningpoker.identity.application.service;

import com.planningpoker.identity.application.port.in.DeactivateUserUseCase;
import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.application.port.in.ListUsersUseCase;
import com.planningpoker.identity.application.port.in.RegisterUserUseCase;
import com.planningpoker.identity.application.port.in.UpdateUserUseCase;
import com.planningpoker.identity.application.port.out.KeycloakSyncPort;
import com.planningpoker.identity.application.port.out.UserEventPublisherPort;
import com.planningpoker.identity.application.port.out.UserPersistencePort;
import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.web.dto.RegisterRequest;
import com.planningpoker.identity.web.dto.UpdateUserRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application service that orchestrates all user-related use cases.
 * <p>
 * This is the single entry point for the domain — it coordinates persistence,
 * Keycloak synchronisation and event publishing without leaking infrastructure
 * details into the domain model.
 */
@Service
@Transactional
public class UserService implements RegisterUserUseCase, GetUserUseCase,
        UpdateUserUseCase, DeactivateUserUseCase, ListUsersUseCase {

    private final UserPersistencePort persistencePort;
    private final KeycloakSyncPort keycloakSyncPort;
    private final UserEventPublisherPort eventPublisherPort;

    public UserService(UserPersistencePort persistencePort,
                       KeycloakSyncPort keycloakSyncPort,
                       UserEventPublisherPort eventPublisherPort) {
        this.persistencePort = persistencePort;
        this.keycloakSyncPort = keycloakSyncPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    // ── RegisterUserUseCase ──────────────────────────────────────────

    @Override
    public User register(RegisterRequest request) {
        if (persistencePort.existsByUsername(request.username())) {
            throw new BusinessException("USERNAME_TAKEN",
                    "Username '" + request.username() + "' is already taken");
        }
        if (persistencePort.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_TAKEN",
                    "Email '" + request.email() + "' is already registered");
        }

        // Create the user in Keycloak first — if this fails we don't persist locally
        String keycloakId = keycloakSyncPort.createUser(
                request.username(), request.email(), request.password(),
                request.firstName(), request.lastName());

        Instant now = Instant.now();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setKeycloakId(keycloakId);
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDisplayName(request.displayName() != null ? request.displayName() : request.username());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.addRole(UserRole.PARTICIPANT);

        User saved = persistencePort.save(user);
        eventPublisherPort.publishUserRegistered(saved);
        return saved;
    }

    // ── GetUserUseCase ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByKeycloakId(String keycloakId) {
        return persistencePort.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", keycloakId));
    }

    @Override
    @Transactional
    public User getCurrentUser(String keycloakId) {
        return persistencePort.findByKeycloakId(keycloakId)
                .orElseGet(() -> autoCreateFromKeycloak(keycloakId));
    }

    /**
     * Auto-creates a local DB user for SSO users (Google/Facebook) who authenticated
     * via Keycloak but don't have a local record yet.
     */
    private User autoCreateFromKeycloak(String keycloakId) {
        Instant now = Instant.now();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setKeycloakId(keycloakId);
        user.setUsername("user-" + keycloakId.substring(0, 8));
        user.setEmail("");
        user.setFirstName("");
        user.setLastName("");
        user.setDisplayName("");
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.addRole(UserRole.PARTICIPANT);

        // Try to enrich from Keycloak Admin API
        try {
            var kcUser = keycloakSyncPort.getUser(keycloakId);
            if (kcUser != null) {
                if (kcUser.username() != null) user.setUsername(kcUser.username());
                if (kcUser.email() != null) user.setEmail(kcUser.email());
                if (kcUser.firstName() != null) user.setFirstName(kcUser.firstName());
                if (kcUser.lastName() != null) user.setLastName(kcUser.lastName());
                user.setDisplayName(
                    (kcUser.firstName() != null ? kcUser.firstName() : "") +
                    (kcUser.lastName() != null ? " " + kcUser.lastName() : "")
                );
            }
        } catch (Exception e) {
            // Keycloak lookup failed — use defaults
        }

        User saved = persistencePort.save(user);
        eventPublisherPort.publishUserRegistered(saved);
        return saved;
    }

    // ── UpdateUserUseCase ────────────────────────────────────────────

    @Override
    public User update(UUID id, UpdateUserRequest request) {
        User user = persistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.updateProfile(request.displayName(), request.avatarUrl());

        if (request.displayName() != null) {
            keycloakSyncPort.updateUser(user.getKeycloakId(), request.displayName());
        }

        User saved = persistencePort.save(user);
        eventPublisherPort.publishUserUpdated(saved);
        return saved;
    }

    @Override
    public User updateByKeycloakId(String keycloakId, UpdateUserRequest request) {
        User user = persistencePort.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", keycloakId));
        return update(user.getId(), request);
    }

    // ── DeactivateUserUseCase ────────────────────────────────────────

    @Override
    public void deactivate(UUID id) {
        User user = persistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.deactivate();
        persistencePort.save(user);
        keycloakSyncPort.deactivateUser(user.getKeycloakId());
        eventPublisherPort.publishUserDeactivated(user);
    }

    @Override
    public void deactivateByKeycloakId(String keycloakId) {
        User user = persistencePort.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", keycloakId));
        deactivate(user.getId());
    }

    // ── ListUsersUseCase ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<User> list(int offset, int limit) {
        return persistencePort.findAll(offset, limit);
    }
}
