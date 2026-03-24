package com.planningpoker.identity.infrastructure.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.identity.infrastructure.keycloak.KeycloakTokenService.KeycloakTokenException;
import com.planningpoker.identity.infrastructure.keycloak.KeycloakTokenService.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakTokenServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private KeycloakTokenService tokenService;

    @BeforeEach
    void setUp() throws Exception {
        KeycloakProperties properties = new KeycloakProperties(
                "http://keycloak:8080", "planning-poker",
                "admin-cli", "admin", "admin"
        );
        ObjectMapper objectMapper = new ObjectMapper();

        tokenService = new KeycloakTokenService(properties, objectMapper);

        // Replace the internal HttpClient with our mock via reflection
        Field httpClientField = KeycloakTokenService.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(tokenService, httpClient);
    }

    // ═══════════════════════════════════════════════════════════════════
    // login
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class Login {

        @Test
        @SuppressWarnings("unchecked")
        void shouldReturnTokenResponseOnSuccess() throws Exception {
            String jsonBody = """
                    {
                      "access_token": "eyJhbGciOiJSUzI1NiJ9.access",
                      "refresh_token": "eyJhbGciOiJSUzI1NiJ9.refresh",
                      "expires_in": 300
                    }
                    """;
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonBody);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            TokenResponse result = tokenService.login("johndoe", "P@ssw0rd!");

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("eyJhbGciOiJSUzI1NiJ9.access");
            assertThat(result.refreshToken()).isEqualTo("eyJhbGciOiJSUzI1NiJ9.refresh");
            assertThat(result.expiresIn()).isEqualTo(300L);
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowWhenKeycloakReturns401() throws Exception {
            when(httpResponse.statusCode()).thenReturn(401);
            when(httpResponse.body()).thenReturn("Invalid credentials");
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThatThrownBy(() -> tokenService.login("johndoe", "wrong"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Token request failed with status 401");
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowWhenHttpClientThrowsIOException() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new IOException("Connection refused"));

            assertThatThrownBy(() -> tokenService.login("johndoe", "P@ssw0rd!"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Failed to call Keycloak token endpoint")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowAndInterruptOnInterruptedException() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new InterruptedException("Interrupted"));

            assertThatThrownBy(() -> tokenService.login("johndoe", "P@ssw0rd!"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Failed to call Keycloak token endpoint");

            // Clear the interrupt flag so it doesn't affect other tests
            Thread.interrupted();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // refresh
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class Refresh {

        @Test
        @SuppressWarnings("unchecked")
        void shouldReturnNewTokensOnRefreshSuccess() throws Exception {
            String jsonBody = """
                    {
                      "access_token": "new-access-token",
                      "refresh_token": "new-refresh-token",
                      "expires_in": 600
                    }
                    """;
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonBody);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            TokenResponse result = tokenService.refresh("old-refresh-token");

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(result.expiresIn()).isEqualTo(600L);
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowWhenRefreshTokenIsExpired() throws Exception {
            when(httpResponse.statusCode()).thenReturn(400);
            when(httpResponse.body()).thenReturn("Token expired");
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThatThrownBy(() -> tokenService.refresh("expired-token"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Token request failed with status 400");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // logout
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class Logout {

        @Test
        @SuppressWarnings("unchecked")
        void shouldLogoutSuccessfully() throws Exception {
            when(httpResponse.statusCode()).thenReturn(204);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            // Should not throw
            tokenService.logout("some-refresh-token");
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowWhenLogoutFails() throws Exception {
            when(httpResponse.statusCode()).thenReturn(400);
            when(httpResponse.body()).thenReturn("Bad request");
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThatThrownBy(() -> tokenService.logout("invalid-token"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Logout failed with status 400");
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowWhenLogoutIOExceptionOccurs() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new IOException("Network error"));

            assertThatThrownBy(() -> tokenService.logout("some-refresh-token"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Failed to call Keycloak logout endpoint")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowAndInterruptOnLogoutInterruptedException() throws Exception {
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new InterruptedException("Interrupted"));

            assertThatThrownBy(() -> tokenService.logout("some-refresh-token"))
                    .isInstanceOf(KeycloakTokenException.class)
                    .hasMessageContaining("Failed to call Keycloak logout endpoint");

            // Clear the interrupt flag
            Thread.interrupted();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TokenResponse record
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class TokenResponseTests {

        @Test
        void shouldCreateTokenResponseWithAccessors() {
            TokenResponse response = new TokenResponse("access", "refresh", 300);

            assertThat(response.accessToken()).isEqualTo("access");
            assertThat(response.refreshToken()).isEqualTo("refresh");
            assertThat(response.expiresIn()).isEqualTo(300L);
        }

        @Test
        void shouldHaveValueEquality() {
            TokenResponse a = new TokenResponse("access", "refresh", 300);
            TokenResponse b = new TokenResponse("access", "refresh", 300);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // KeycloakTokenException
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class KeycloakTokenExceptionTests {

        @Test
        void shouldCreateWithMessageOnly() {
            KeycloakTokenException ex = new KeycloakTokenException("Something failed");

            assertThat(ex.getMessage()).isEqualTo("Something failed");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        void shouldCreateWithMessageAndCause() {
            IOException cause = new IOException("Connection reset");
            KeycloakTokenException ex = new KeycloakTokenException("Token call failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Token call failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }
}
