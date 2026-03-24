package com.planningpoker.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility to extract user information from the Keycloak JWT in the SecurityContext.
 * Works with spring-boot-starter-oauth2-resource-server.
 */
public final class JwtClaimExtractor {

    private JwtClaimExtractor() {}

    /**
     * Get the authenticated user from the current SecurityContext.
     *
     * @return AuthenticatedUser or null if not authenticated
     */
    public static AuthenticatedUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return fromJwt(jwtAuth.getToken());
        }
        return null;
    }

    /**
     * Extract AuthenticatedUser from a JWT token.
     */
    public static AuthenticatedUser fromJwt(Jwt jwt) {
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        Set<String> roles = extractRoles(jwt);

        return new AuthenticatedUser(userId, username, email, roles);
    }

    /**
     * Get the current user's ID (Keycloak subject).
     */
    public static String currentUserId() {
        AuthenticatedUser user = currentUser();
        return user != null ? user.userId() : null;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractRoles(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        // Extract realm roles from realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object realmRoles = realmAccess.get("roles");
            if (realmRoles instanceof List<?> roleList) {
                roleList.forEach(r -> roles.add(String.valueOf(r).toUpperCase()));
            }
        }

        return Collections.unmodifiableSet(roles);
    }
}
