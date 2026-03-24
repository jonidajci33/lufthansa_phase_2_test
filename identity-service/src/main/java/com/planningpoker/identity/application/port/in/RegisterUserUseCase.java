package com.planningpoker.identity.application.port.in;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.web.dto.RegisterRequest;

/**
 * Primary port for registering a new user.
 */
public interface RegisterUserUseCase {

    User register(RegisterRequest request);
}
