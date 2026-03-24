package com.planningpoker.identity.application.port.in;

import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;

/**
 * Primary port for listing users with pagination.
 */
public interface ListUsersUseCase {

    Page<User> list(int offset, int limit);
}
