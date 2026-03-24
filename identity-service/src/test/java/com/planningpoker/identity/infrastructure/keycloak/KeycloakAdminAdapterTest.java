package com.planningpoker.identity.infrastructure.keycloak;

import com.planningpoker.identity.application.port.out.KeycloakSyncPort.KeycloakUserInfo;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminAdapterTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private KeycloakProperties properties;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @InjectMocks
    private KeycloakAdminAdapter adapter;

    @Captor
    private ArgumentCaptor<UserRepresentation> userRepCaptor;

    @BeforeEach
    void setUp() {
        when(properties.realm()).thenReturn("planning-poker");
        when(keycloak.realm("planning-poker")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    // ═══════════════════════════════════════════════════════════════════
    // createUser
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class CreateUser {

        @Test
        void shouldCreateUserAndReturnKeycloakId() {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(201);
            when(response.getLocation()).thenReturn(
                    URI.create("http://keycloak:8080/admin/realms/planning-poker/users/kc-new-id-123"));
            when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

            String keycloakId = adapter.createUser("johndoe", "john@example.com",
                    "P@ssw0rd!", "John", "Doe");

            assertThat(keycloakId).isEqualTo("kc-new-id-123");

            verify(usersResource).create(userRepCaptor.capture());
            UserRepresentation captured = userRepCaptor.getValue();
            assertThat(captured.getUsername()).isEqualTo("johndoe");
            assertThat(captured.getEmail()).isEqualTo("john@example.com");
            assertThat(captured.getFirstName()).isEqualTo("John");
            assertThat(captured.getLastName()).isEqualTo("Doe");
            assertThat(captured.isEnabled()).isTrue();
            assertThat(captured.isEmailVerified()).isTrue();
            assertThat(captured.getCredentials()).hasSize(1);
            assertThat(captured.getCredentials().getFirst().getValue()).isEqualTo("P@ssw0rd!");
            assertThat(captured.getCredentials().getFirst().isTemporary()).isFalse();
        }

        @Test
        void shouldThrowWhenKeycloakReturnsNon201Status() {
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(409);
            when(response.readEntity(String.class)).thenReturn("User exists");
            when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

            assertThatThrownBy(() -> adapter.createUser("johndoe", "john@example.com",
                    "P@ssw0rd!", "John", "Doe"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create user in Keycloak")
                    .hasMessageContaining("status=409")
                    .hasMessageContaining("User exists");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // deactivateUser
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class DeactivateUser {

        @Test
        void shouldDisableUserInKeycloak() {
            UserResource userResource = mock(UserResource.class);
            UserRepresentation userRep = new UserRepresentation();
            userRep.setEnabled(true);
            userRep.setUsername("johndoe");

            when(usersResource.get("kc-abc-123")).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(userRep);

            adapter.deactivateUser("kc-abc-123");

            verify(userResource).update(userRepCaptor.capture());
            assertThat(userRepCaptor.getValue().isEnabled()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // updateUser
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateDisplayNameInKeycloak() {
            UserResource userResource = mock(UserResource.class);
            UserRepresentation userRep = new UserRepresentation();
            userRep.setFirstName("OldFirst");
            userRep.setLastName("OldLast");

            when(usersResource.get("kc-abc-123")).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(userRep);

            adapter.updateUser("kc-abc-123", "NewDisplayName");

            verify(userResource).update(userRepCaptor.capture());
            UserRepresentation updated = userRepCaptor.getValue();
            assertThat(updated.getFirstName()).isEqualTo("NewDisplayName");
            assertThat(updated.getLastName()).isEmpty();
        }

        @Test
        void shouldNotModifyNamesWhenDisplayNameIsNull() {
            UserResource userResource = mock(UserResource.class);
            UserRepresentation userRep = new UserRepresentation();
            userRep.setFirstName("OriginalFirst");
            userRep.setLastName("OriginalLast");

            when(usersResource.get("kc-abc-123")).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(userRep);

            adapter.updateUser("kc-abc-123", null);

            verify(userResource).update(userRepCaptor.capture());
            UserRepresentation updated = userRepCaptor.getValue();
            assertThat(updated.getFirstName()).isEqualTo("OriginalFirst");
            assertThat(updated.getLastName()).isEqualTo("OriginalLast");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // getUser
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class GetUser {

        @Test
        void shouldReturnKeycloakUserInfo() {
            UserResource userResource = mock(UserResource.class);
            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername("johndoe");
            userRep.setEmail("john@example.com");
            userRep.setFirstName("John");
            userRep.setLastName("Doe");

            when(usersResource.get("kc-abc-123")).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(userRep);

            KeycloakUserInfo result = adapter.getUser("kc-abc-123");

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("johndoe");
            assertThat(result.email()).isEqualTo("john@example.com");
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
        }

        @Test
        void shouldReturnNullWhenKeycloakThrowsException() {
            UserResource userResource = mock(UserResource.class);
            when(usersResource.get("unknown-id")).thenReturn(userResource);
            when(userResource.toRepresentation())
                    .thenThrow(new RuntimeException("User not found"));

            KeycloakUserInfo result = adapter.getUser("unknown-id");

            assertThat(result).isNull();
        }
    }
}
