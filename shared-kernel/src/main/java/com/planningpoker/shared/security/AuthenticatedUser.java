package com.planningpoker.shared.security;

import java.util.Set;

/**
 * Represents the currently authenticated user extracted from the JWT token.
 */
public record AuthenticatedUser(
        String userId,
        String username,
        String email,
        Set<String> roles
) {

    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
