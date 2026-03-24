package com.planningpoker.room.application.port.out;

import java.util.UUID;

/**
 * Secondary (driven) port for validating user existence.
 * The infrastructure adapter calls the Identity Service's
 * {@code /internal/users/{id}} endpoint.
 */
public interface UserValidationPort {

    boolean userExists(UUID userId);
}
