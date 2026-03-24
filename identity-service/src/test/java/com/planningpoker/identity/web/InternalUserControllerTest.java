package com.planningpoker.identity.web;

import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.infrastructure.config.SecurityConfig;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static User sampleUser() {
        return new User(
                USER_ID,
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

    @Test
    void shouldGetUserByIdWithoutAuth() throws Exception {
        User user = sampleUser();
        given(getUserUseCase.getById(USER_ID)).willReturn(user);

        mockMvc.perform(get("/internal/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(USER_ID.toString())))
                .andExpect(jsonPath("$.username", is("johndoe")))
                .andExpect(jsonPath("$.displayName", is("John Doe")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void shouldReturn404WhenInternalUserNotFound() throws Exception {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        given(getUserUseCase.getById(missingId))
                .willThrow(new ResourceNotFoundException("User", missingId));

        mockMvc.perform(get("/internal/users/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("USER_NOT_FOUND")));
    }
}
