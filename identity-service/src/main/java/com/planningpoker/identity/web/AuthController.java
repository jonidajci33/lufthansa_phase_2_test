package com.planningpoker.identity.web;

import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.application.port.in.RegisterUserUseCase;
import com.planningpoker.identity.application.port.out.UserPersistencePort;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.infrastructure.keycloak.KeycloakTokenService;
import com.planningpoker.identity.web.dto.AuthResponse;
import com.planningpoker.identity.web.dto.LoginRequest;
import com.planningpoker.identity.web.dto.RefreshRequest;
import com.planningpoker.identity.web.dto.RegisterRequest;
import com.planningpoker.identity.web.dto.UserResponse;
import com.planningpoker.identity.web.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UserPersistencePort userPersistencePort;
    private final KeycloakTokenService keycloakTokenService;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          GetUserUseCase getUserUseCase,
                          UserPersistencePort userPersistencePort,
                          KeycloakTokenService keycloakTokenService) {
        this.registerUserUseCase = registerUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.userPersistencePort = userPersistencePort;
        this.keycloakTokenService = keycloakTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a new user account and returns tokens for immediate login.")
    @ApiResponse(responseCode = "201", description = "User registered and authenticated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserUseCase.register(request);

        // Auto-login: obtain tokens so the client is immediately authenticated
        KeycloakTokenService.TokenResponse tokens =
                keycloakTokenService.login(request.username(), request.password());

        AuthResponse authResponse = new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                UserRestMapper.toResponse(user)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user",
            description = "Authenticates with username/password and returns access + refresh tokens.")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        KeycloakTokenService.TokenResponse tokens =
                keycloakTokenService.login(request.username(), request.password());

        // Look up the user in the local DB by username
        UserResponse userResponse = userPersistencePort.findByUsername(request.username())
                .map(UserRestMapper::toResponse)
                .orElseGet(() -> {
                    log.warn("User authenticated in Keycloak but not found in local DB: username={}", request.username());
                    return null;
                });

        AuthResponse authResponse = new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                userResponse
        );
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
            description = "Exchanges a refresh token for new access and refresh tokens.")
    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        KeycloakTokenService.TokenResponse tokens =
                keycloakTokenService.refresh(request.refreshToken());

        AuthResponse authResponse = new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                null  // No user lookup needed for token refresh
        );
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout",
            description = "Invalidates the refresh token at the identity provider.")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        keycloakTokenService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
            description = "Returns the profile of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User profile returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = getUserUseCase.getCurrentUser(keycloakId);
        return ResponseEntity.ok(UserRestMapper.toResponse(user));
    }

    // ── Internal helpers ──────────────────────────────────────────────

    /**
     * Extracts the "sub" claim from a JWT access token without full verification.
     * This is acceptable here because we just received the token from Keycloak.
     */
    private String extractSubFromAccessToken(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            String payload = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            // Simple extraction — avoid pulling in a full JSON parser for one field
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
            return node.get("sub").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract subject from access token", e);
        }
    }
}
