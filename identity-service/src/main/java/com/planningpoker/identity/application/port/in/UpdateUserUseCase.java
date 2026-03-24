package com.planningpoker.identity.application.port.in;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.web.dto.UpdateUserRequest;

import java.util.UUID;

/**
 * Primary port for updating a user's profile.
 */
public interface UpdateUserUseCase {

    User update(UUID id, UpdateUserRequest request);

    User updateByKeycloakId(String keycloakId, UpdateUserRequest request);
}
