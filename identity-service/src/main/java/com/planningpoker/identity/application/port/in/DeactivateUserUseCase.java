package com.planningpoker.identity.application.port.in;

import java.util.UUID;

/**
 * Primary port for deactivating (soft-deleting) a user account.
 */
public interface DeactivateUserUseCase {

    void deactivate(UUID id);

    void deactivateByKeycloakId(String keycloakId);
}
