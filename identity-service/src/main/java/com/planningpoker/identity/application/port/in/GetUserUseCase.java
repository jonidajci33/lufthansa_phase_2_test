package com.planningpoker.identity.application.port.in;

import com.planningpoker.identity.domain.User;

import java.util.UUID;

/**
 * Primary port for retrieving user details.
 */
public interface GetUserUseCase {

    User getById(UUID id);

    User getByKeycloakId(String keycloakId);

    User getCurrentUser(String keycloakId);
}
