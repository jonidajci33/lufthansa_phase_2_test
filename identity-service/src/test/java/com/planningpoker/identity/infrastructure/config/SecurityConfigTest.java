package com.planningpoker.identity.infrastructure.config;

import com.planningpoker.identity.infrastructure.config.SecurityConfig.KeycloakRealmRoleConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private KeycloakRealmRoleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeycloakRealmRoleConverter();
    }

    // ═══════════════════════════════════════════════════════════════════
    // KeycloakRealmRoleConverter
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class RealmRoleConverter {

        @Test
        void shouldExtractRolesFromTopLevelRealmRolesClaim() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_roles", List.of("ADMIN", "PARTICIPANT"))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PARTICIPANT");
        }

        @Test
        void shouldExtractRolesFromNestedRealmAccessClaim() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_access", Map.of("roles", List.of("MODERATOR", "OBSERVER")))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_MODERATOR", "ROLE_OBSERVER");
        }

        @Test
        void shouldPreferTopLevelOverNested() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_roles", List.of("ADMIN"))
                    .claim("realm_access", Map.of("roles", List.of("SHOULD_NOT_APPEAR")))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        void shouldReturnEmptyWhenNeitherClaimPresent() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("sub", "user123")
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenTopLevelRolesListIsEmpty() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_roles", List.of())
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenRealmAccessHasNoRolesKey() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_access", Map.of("some_other_key", "value"))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenRealmAccessRolesIsNotAList() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_access", Map.of("roles", "not-a-list"))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities).isEmpty();
        }

        @Test
        void shouldUppercaseRoleNames() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_roles", List.of("admin", "Participant"))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PARTICIPANT");
        }

        @Test
        void shouldHandleSingleRole() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_roles", List.of("PARTICIPANT"))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_PARTICIPANT");
        }

        @Test
        void shouldFallbackToNestedWhenTopLevelIsEmptyList() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("realm_roles", List.of())
                    .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                    .build();

            Collection<GrantedAuthority> authorities = converter.convert(jwt);

            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_ADMIN");
        }
    }
}
