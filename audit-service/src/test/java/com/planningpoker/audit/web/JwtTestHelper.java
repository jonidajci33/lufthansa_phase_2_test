package com.planningpoker.audit.web;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Utility for building mock JWT request post-processors in controller slice tests.
 * Mirrors the Keycloak realm role structure used by {@code SecurityConfig.KeycloakRealmRoleConverter}.
 */
final class JwtTestHelper {

    private JwtTestHelper() {}

    /**
     * Returns a JWT post-processor representing an authenticated user with the PARTICIPANT role.
     */
    static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withUser(String sub) {
        return jwt()
                .jwt(builder -> builder
                        .subject(sub)
                        .claim("preferred_username", "user-" + sub)
                        .claim("email", "user-" + sub + "@example.com")
                        .claim("realm_access", Map.of("roles", List.of("PARTICIPANT")))
                )
                .authorities(new SimpleGrantedAuthority("ROLE_PARTICIPANT"));
    }

    /**
     * Returns a JWT post-processor representing an authenticated admin with the ADMIN role.
     */
    static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withAdmin(String sub) {
        return jwt()
                .jwt(builder -> builder
                        .subject(sub)
                        .claim("preferred_username", "admin-" + sub)
                        .claim("email", "admin-" + sub + "@example.com")
                        .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                )
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
