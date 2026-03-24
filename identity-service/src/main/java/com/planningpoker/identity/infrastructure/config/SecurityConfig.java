package com.planningpoker.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring Security configuration for the Identity Service.
 * Configures JWT-based authentication with Keycloak realm role mapping.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator health/readiness probes (management port)
                        .requestMatchers("/actuator/**").permitAll()
                        // Service-to-service internal endpoints
                        .requestMatchers("/internal/**").permitAll()
                        // /me requires authentication (must be listed before the wildcard)
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        // Public auth endpoints (no JWT required)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Swagger / OpenAPI
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Self-service: current user profile
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        // Admin-only: list all users
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        // Admin-only: view / update / delete any user by ID
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{id}").hasRole("ADMIN")
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Extracts Keycloak realm roles from the JWT and maps them to Spring Security
     * authorities with the {@code ROLE_} prefix.
     * <p>
     * Checks top-level {@code realm_roles} first (Keycloak 26.x custom mapper),
     * then falls back to nested {@code realm_access.roles}.
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Try top-level realm_roles first (Keycloak 26.x custom mapper), then nested realm_access.roles
            List<?> roles = jwt.getClaimAsStringList("realm_roles");
            if (roles == null || roles.isEmpty()) {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null) {
                    Object rolesObj = realmAccess.get("roles");
                    if (rolesObj instanceof List<?> nested) {
                        roles = nested;
                    }
                }
            }
            if (roles == null || roles.isEmpty()) {
                return Collections.emptyList();
            }
            return roles.stream()
                    .map(role -> "ROLE_" + String.valueOf(role).toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());
        }
    }
}
