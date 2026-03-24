package com.planningpoker.identity.application.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserPersistencePort persistencePort;

    @Mock
    private KeycloakSyncPort keycloakSyncPort;

    @Mock
    private UserEventPublisherPort eventPublisherPort;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    // ── Helpers ───────────────────────────────────────────────────────

    private static User existingUser() {
        Instant now = Instant.now();
        return new User(
                UUID.randomUUID(),
                "kc-abc-123",
                "johndoe",
                "john@example.com",
                "John",
                "Doe",
                "John Doe",
                "https://example.com/avatar.png",
                true,
                EnumSet.of(UserRole.PARTICIPANT),
                now,
                now
        );
    }

    private static RegisterRequest registerRequest() {
        return new RegisterRequest("newuser", "new@example.com", "P@ssw0rd!", "New", "User", "New User");
    }

    private static RegisterRequest registerRequestWithoutDisplayName() {
        return new RegisterRequest("newuser", "new@example.com", "P@ssw0rd!", "New", "User", null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Register
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = registerRequest();
        when(persistencePort.existsByUsername(request.username())).thenReturn(false);
        when(persistencePort.existsByEmail(request.email())).thenReturn(false);
        when(keycloakSyncPort.createUser(request.username(), request.email(), request.password(),
                request.firstName(), request.lastName()))
                .thenReturn("kc-new-id");
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("kc-new-id");
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getDisplayName()).isEqualTo("New User");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getRoles()).containsExactly(UserRole.PARTICIPANT);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(persistencePort).existsByUsername(request.username());
        verify(persistencePort).existsByEmail(request.email());
        verify(keycloakSyncPort).createUser(request.username(), request.email(), request.password(),
                request.firstName(), request.lastName());
        verify(persistencePort).save(any(User.class));
        verify(eventPublisherPort).publishUserRegistered(result);
    }

    @Test
    void shouldUseUsernameAsDisplayNameWhenDisplayNameIsNull() {
        RegisterRequest request = registerRequestWithoutDisplayName();
        when(persistencePort.existsByUsername(request.username())).thenReturn(false);
        when(persistencePort.existsByEmail(request.email())).thenReturn(false);
        when(keycloakSyncPort.createUser(request.username(), request.email(), request.password(),
                request.firstName(), request.lastName()))
                .thenReturn("kc-new-id");
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getDisplayName()).isEqualTo(request.username());
    }

    @Test
    void shouldFailWhenUsernameAlreadyExists() {
        RegisterRequest request = registerRequest();
        when(persistencePort.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("USERNAME_TAKEN");
                });

        verify(persistencePort, never()).save(any());
        verifyNoInteractions(keycloakSyncPort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() {
        RegisterRequest request = registerRequest();
        when(persistencePort.existsByUsername(request.username())).thenReturn(false);
        when(persistencePort.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("EMAIL_TAKEN");
                });

        verify(persistencePort, never()).save(any());
        verifyNoInteractions(keycloakSyncPort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void shouldNotPersistOrPublishWhenKeycloakFails() {
        RegisterRequest request = registerRequest();
        when(persistencePort.existsByUsername(request.username())).thenReturn(false);
        when(persistencePort.existsByEmail(request.email())).thenReturn(false);
        when(keycloakSyncPort.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Keycloak unreachable"));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Keycloak unreachable");

        verify(persistencePort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetById
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnUserWhenExists() {
        User user = existingUser();
        when(persistencePort.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.getById(user.getId());

        assertThat(result).isEqualTo(user);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        verify(persistencePort).findById(user.getId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(persistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("USER_NOT_FOUND");
                });
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetByKeycloakId
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnUserByKeycloakId() {
        User user = existingUser();
        when(persistencePort.findByKeycloakId("kc-abc-123")).thenReturn(Optional.of(user));

        User result = userService.getByKeycloakId("kc-abc-123");

        assertThat(result).isEqualTo(user);
        verify(persistencePort).findByKeycloakId("kc-abc-123");
    }

    @Test
    void shouldThrowWhenKeycloakIdNotFoundForGetByKeycloakId() {
        when(persistencePort.findByKeycloakId("unknown-kc-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByKeycloakId("unknown-kc-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetCurrentUser
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnCurrentUserByKeycloakId() {
        User user = existingUser();
        when(persistencePort.findByKeycloakId("kc-abc-123")).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser("kc-abc-123");

        assertThat(result).isEqualTo(user);
        verify(persistencePort).findByKeycloakId("kc-abc-123");
    }

    @Test
    void shouldAutoCreateUserWhenKeycloakIdNotFoundForGetCurrentUser() {
        when(persistencePort.findByKeycloakId("unknown-kc-id")).thenReturn(Optional.empty());
        when(persistencePort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.getCurrentUser("unknown-kc-id");

        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("unknown-kc-id");
        verify(persistencePort).save(any(User.class));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Update
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldUpdateUserProfile() {
        User user = existingUser();
        UUID userId = user.getId();
        when(persistencePort.findById(userId)).thenReturn(Optional.of(user));
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "https://new-avatar.com/img.png");

        User result = userService.update(userId, request);

        assertThat(result.getDisplayName()).isEqualTo("Updated Name");
        assertThat(result.getAvatarUrl()).isEqualTo("https://new-avatar.com/img.png");
        verify(persistencePort).findById(userId);
        verify(persistencePort).save(any(User.class));
    }

    @Test
    void shouldPublishEventOnUpdate() {
        User user = existingUser();
        UUID userId = user.getId();
        when(persistencePort.findById(userId)).thenReturn(Optional.of(user));
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("Updated Name", null);

        userService.update(userId, request);

        verify(eventPublisherPort).publishUserUpdated(any(User.class));
    }

    @Test
    void shouldSyncKeycloakOnUpdateWhenDisplayNameProvided() {
        User user = existingUser();
        UUID userId = user.getId();
        when(persistencePort.findById(userId)).thenReturn(Optional.of(user));
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "https://avatar.com/new.png");

        userService.update(userId, request);

        verify(keycloakSyncPort).updateUser(user.getKeycloakId(), "Updated Name");
    }

    @Test
    void shouldNotSyncKeycloakOnUpdateWhenDisplayNameIsNull() {
        User user = existingUser();
        UUID userId = user.getId();
        when(persistencePort.findById(userId)).thenReturn(Optional.of(user));
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest(null, "https://avatar.com/new.png");

        userService.update(userId, request);

        verify(keycloakSyncPort, never()).updateUser(anyString(), anyString());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentUser() {
        UUID id = UUID.randomUUID();
        when(persistencePort.findById(id)).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest("Name", null);

        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(persistencePort, never()).save(any());
        verifyNoInteractions(keycloakSyncPort);
        verifyNoInteractions(eventPublisherPort);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Deactivate
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldDeactivateUser() {
        User user = existingUser();
        UUID userId = user.getId();
        String keycloakId = user.getKeycloakId();
        when(persistencePort.findById(userId)).thenReturn(Optional.of(user));
        when(persistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deactivate(userId);

        assertThat(user.isActive()).isFalse();

        verify(persistencePort).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isActive()).isFalse();

        verify(keycloakSyncPort).deactivateUser(keycloakId);
        verify(eventPublisherPort).publishUserDeactivated(user);
    }

    @Test
    void shouldThrowWhenDeactivatingNonExistentUser() {
        UUID id = UUID.randomUUID();
        when(persistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivate(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(persistencePort, never()).save(any());
        verifyNoInteractions(keycloakSyncPort);
        verifyNoInteractions(eventPublisherPort);
    }

    // ═══════════════════════════════════════════════════════════════════
    // List (paginated)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnPaginatedUsers() {
        User user1 = existingUser();
        User user2 = existingUser();
        Page<User> page = new Page<>(List.of(user1, user2), 10L);
        when(persistencePort.findAll(0, 20)).thenReturn(page);

        Page<User> result = userService.list(0, 20);

        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(10L);
        verify(persistencePort).findAll(0, 20);
    }

    @Test
    void shouldPassOffsetAndLimitToPersistence() {
        Page<User> emptyPage = new Page<>(List.of(), 0L);
        when(persistencePort.findAll(40, 10)).thenReturn(emptyPage);

        Page<User> result = userService.list(40, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        verify(persistencePort).findAll(40, 10);
    }

    @Test
    void shouldReturnEmptyPageWhenNoUsers() {
        Page<User> emptyPage = new Page<>(List.of(), 0L);
        when(persistencePort.findAll(0, 20)).thenReturn(emptyPage);

        Page<User> result = userService.list(0, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
