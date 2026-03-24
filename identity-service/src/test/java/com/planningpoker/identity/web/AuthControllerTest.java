package com.planningpoker.identity.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.application.port.in.RegisterUserUseCase;
import com.planningpoker.identity.application.port.out.UserPersistencePort;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.infrastructure.config.SecurityConfig;
import com.planningpoker.identity.infrastructure.keycloak.KeycloakTokenService;
import com.planningpoker.identity.web.dto.RegisterRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private UserPersistencePort userPersistencePort;

    @MockitoBean
    private KeycloakTokenService keycloakTokenService;

    // ── Helpers ──────────────────────────────────────────────────────

    private static User sampleUser() {
        return new User(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "00000000-0000-0000-0000-0000000000a1",
                "johndoe",
                "john@example.com",
                "John",
                "Doe",
                "John Doe",
                null,
                true,
                EnumSet.of(UserRole.PARTICIPANT),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    // ── Tests ────────────────────────────────────────────────────────

    @Test
    void shouldRegisterUserAndReturn201() throws Exception {
        User user = sampleUser();
        given(registerUserUseCase.register(any(RegisterRequest.class))).willReturn(user);
        given(keycloakTokenService.login("johndoe", "password123"))
                .willReturn(new KeycloakTokenService.TokenResponse("access-token", "refresh-token", 300));

        RegisterRequest request = new RegisterRequest("johndoe", "john@example.com", "password123", "John", "Doe", "John Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")))
                .andExpect(jsonPath("$.user.id", is("00000000-0000-0000-0000-0000000000a1")))
                .andExpect(jsonPath("$.user.username", is("johndoe")))
                .andExpect(jsonPath("$.user.email", is("john@example.com")))
                .andExpect(jsonPath("$.user.displayName", is("John Doe")))
                .andExpect(jsonPath("$.user.isActive", is(true)))
                .andExpect(jsonPath("$.user.roles", notNullValue()))
                .andExpect(jsonPath("$.user.createdAt", notNullValue()));
    }

    @Test
    void shouldReturn400WhenUsernameBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("", "john@example.com", "password123", "John", "Doe", "John Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field", is("username")));
    }

    @Test
    void shouldReturn400WhenEmailInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("johndoe", "not-an-email", "password123", "John", "Doe", "John Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field", is("email")));
    }

    @Test
    void shouldReturn400WhenPasswordTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest("johndoe", "john@example.com", "12345", "John", "Doe", "John Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field", is("password")));
    }

    @Test
    void shouldReturn400WhenUsernameAlreadyExists() throws Exception {
        given(registerUserUseCase.register(any(RegisterRequest.class)))
                .willThrow(new BusinessException("USERNAME_TAKEN", "Username already exists"));

        RegisterRequest request = new RegisterRequest("johndoe", "john@example.com", "password123", "John", "Doe", "John Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("USERNAME_TAKEN")))
                .andExpect(jsonPath("$.message", is("Username already exists")));
    }
}
