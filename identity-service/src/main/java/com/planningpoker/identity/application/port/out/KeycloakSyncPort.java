package com.planningpoker.identity.application.port.out;

/**
 * Secondary (driven) port for synchronising user state with Keycloak.
 * The infrastructure adapter wraps the Keycloak Admin Client behind this interface
 * so the application layer never depends on Keycloak libraries directly.
 */
public interface KeycloakSyncPort {

    /**
     * Creates a user in Keycloak and returns the Keycloak-assigned user id.
     *
     * @param username  user's login name
     * @param email     user's email
     * @param password  initial password (will be set as temporary or permanent per policy)
     * @param firstName user's first name
     * @param lastName  user's last name
     * @return the keycloak user id
     */
    String createUser(String username, String email, String password,
                      String firstName, String lastName);

    /**
     * Disables the user in Keycloak so they can no longer authenticate.
     *
     * @param keycloakId the Keycloak user id
     */
    void deactivateUser(String keycloakId);

    /**
     * Pushes profile changes (display name) to Keycloak.
     *
     * @param keycloakId  the Keycloak user id
     * @param displayName updated display name
     */
    void updateUser(String keycloakId, String displayName);

    /**
     * Fetches user info from Keycloak by ID.
     * Used to populate local DB records for SSO users.
     *
     * @param keycloakId the Keycloak user id
     * @return user info, or null if not found
     */
    KeycloakUserInfo getUser(String keycloakId);

    record KeycloakUserInfo(String username, String email, String firstName, String lastName) {}
}
