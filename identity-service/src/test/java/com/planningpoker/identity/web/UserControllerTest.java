package com.planningpoker.identity.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planningpoker.identity.application.port.in.DeactivateUserUseCase;
import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.application.port.in.ListUsersUseCase;
import com.planningpoker.identity.application.port.in.UpdateUserUseCase;
import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.infrastructure.config.SecurityConfig;
import com.planningpoker.identity.web.dto.UpdateUserRequest;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private ListUsersUseCase listUsersUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private DeactivateUserUseCase deactivateUserUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String KEYCLOAK_SUB = "00000000-0000-0000-0000-0000000000a1";

    private static User sampleUser() {
        return new User(
                USER_ID,
                KEYCLOAK_SUB,
                "johndoe",
                "john@example.com",
                "John",
                "Doe",
                "John Doe",
                "https://avatar.example.com/john.png",
                true,
                EnumSet.of(UserRole.PARTICIPANT),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    // ── GET /api/v1/users/me ─────────────────────────────────────────

    @Test
    void shouldGetCurrentUserProfile() throws Exception {
        User user = sampleUser();
        given(getUserUseCase.getCurrentUser(KEYCLOAK_SUB)).willReturn(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(JwtTestHelper.withUser(KEYCLOAK_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(KEYCLOAK_SUB)))
                .andExpect(jsonPath("$.username", is("johndoe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.displayName", is("John Doe")))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.roles", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/users (list) ─────────────────────────────────────

    @Test
    void shouldListUsersWithPagination() throws Exception {
        User user = sampleUser();
        Page<User> page = new Page<>(List.of(user), 1L);
        given(listUsersUseCase.list(0, 10)).willReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("offset", "0")
                        .param("limit", "10")
                        .with(JwtTestHelper.withAdmin("admin-sub")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(KEYCLOAK_SUB)))
                .andExpect(jsonPath("$.data[0].username", is("johndoe")))
                .andExpect(jsonPath("$.meta.total", is(1)))
                .andExpect(jsonPath("$.meta.limit", is(10)))
                .andExpect(jsonPath("$.meta.offset", is(0)))
                .andExpect(jsonPath("$.meta.hasNext", is(false)));
    }

    // ── GET /api/v1/users/{id} ───────────────────────────────────────

    @Test
    void shouldGetUserById() throws Exception {
        User user = sampleUser();
        given(getUserUseCase.getByKeycloakId(KEYCLOAK_SUB)).willReturn(user);

        mockMvc.perform(get("/api/v1/users/{keycloakId}", KEYCLOAK_SUB)
                        .with(JwtTestHelper.withAdmin(KEYCLOAK_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(KEYCLOAK_SUB)))
                .andExpect(jsonPath("$.username", is("johndoe")));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        String missingKeycloakId = "00000000-0000-0000-0000-000000000099";
        given(getUserUseCase.getByKeycloakId(missingKeycloakId))
                .willThrow(new ResourceNotFoundException("User", missingKeycloakId));

        mockMvc.perform(get("/api/v1/users/{keycloakId}", missingKeycloakId)
                        .with(JwtTestHelper.withAdmin(KEYCLOAK_SUB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("USER_NOT_FOUND")));
    }

    // ── PUT /api/v1/users/{id} ───────────────────────────────────────

    @Test
    void shouldUpdateUser() throws Exception {
        User updatedUser = new User(
                USER_ID, KEYCLOAK_SUB, "johndoe", "john@example.com",
                "John", "Updated",
                "John Updated", "https://avatar.example.com/new.png",
                true, EnumSet.of(UserRole.PARTICIPANT),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
        given(updateUserUseCase.updateByKeycloakId(eq(KEYCLOAK_SUB), any(UpdateUserRequest.class)))
                .willReturn(updatedUser);

        UpdateUserRequest request = new UpdateUserRequest("John Updated", "https://avatar.example.com/new.png");

        mockMvc.perform(put("/api/v1/users/{keycloakId}", KEYCLOAK_SUB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(JwtTestHelper.withAdmin(KEYCLOAK_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("John Updated")))
                .andExpect(jsonPath("$.avatarUrl", is("https://avatar.example.com/new.png")));
    }

    // ── DELETE /api/v1/users/{id} ────────────────────────────────────

    @Test
    void shouldDeactivateUser() throws Exception {
        willDoNothing().given(deactivateUserUseCase).deactivateByKeycloakId(KEYCLOAK_SUB);

        mockMvc.perform(delete("/api/v1/users/{keycloakId}", KEYCLOAK_SUB)
                        .with(JwtTestHelper.withAdmin("admin-sub")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn403WhenNonAdminDeletesUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{keycloakId}", KEYCLOAK_SUB)
                        .with(JwtTestHelper.withUser(KEYCLOAK_SUB)))
                .andExpect(status().isForbidden());
    }
}
