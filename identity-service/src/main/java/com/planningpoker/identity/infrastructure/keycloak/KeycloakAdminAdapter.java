package com.planningpoker.identity.infrastructure.keycloak;

import com.planningpoker.identity.application.port.out.KeycloakSyncPort;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Infrastructure adapter that implements {@link KeycloakSyncPort}
 * by delegating to the Keycloak Admin Client.
 */
@Component
public class KeycloakAdminAdapter implements KeycloakSyncPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminAdapter.class);

    private final Keycloak keycloak;
    private final KeycloakProperties properties;

    public KeycloakAdminAdapter(Keycloak keycloak, KeycloakProperties properties) {
        this.keycloak = keycloak;
        this.properties = properties;
    }

    @Override
    public String createUser(String username, String email, String password,
                             String firstName, String lastName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        UsersResource usersResource = keycloak.realm(properties.realm()).users();

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                String locationPath = response.getLocation().getPath();
                String keycloakUserId = locationPath.substring(locationPath.lastIndexOf('/') + 1);
                log.info("Created Keycloak user: username={}, keycloakId={}", username, keycloakUserId);
                return keycloakUserId;
            }
            throw new RuntimeException(
                    "Failed to create user in Keycloak: status=" + response.getStatus()
                            + ", body=" + response.readEntity(String.class)
            );
        }
    }

    @Override
    public void deactivateUser(String keycloakId) {
        UserRepresentation user = keycloak.realm(properties.realm())
                .users()
                .get(keycloakId)
                .toRepresentation();

        user.setEnabled(false);

        keycloak.realm(properties.realm())
                .users()
                .get(keycloakId)
                .update(user);

        log.info("Deactivated Keycloak user: keycloakId={}", keycloakId);
    }

    @Override
    public void updateUser(String keycloakId, String displayName) {
        UserRepresentation user = keycloak.realm(properties.realm())
                .users()
                .get(keycloakId)
                .toRepresentation();

        if (displayName != null) {
            user.setFirstName(displayName);
            user.setLastName("");
        }

        keycloak.realm(properties.realm())
                .users()
                .get(keycloakId)
                .update(user);

        log.info("Updated Keycloak user: keycloakId={}, displayName={}", keycloakId, displayName);
    }

    @Override
    public KeycloakUserInfo getUser(String keycloakId) {
        try {
            UserRepresentation user = keycloak.realm(properties.realm())
                    .users()
                    .get(keycloakId)
                    .toRepresentation();
            return new KeycloakUserInfo(
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch Keycloak user: keycloakId={}, error={}", keycloakId, e.getMessage());
            return null;
        }
    }
}
