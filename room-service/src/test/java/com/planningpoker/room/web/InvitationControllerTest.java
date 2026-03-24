package com.planningpoker.room.web;

import com.planningpoker.room.application.port.in.AcceptInvitationUseCase;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.infrastructure.config.SecurityConfig;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcceptInvitationUseCase acceptInvitationUseCase;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final String USER_SUB = USER_ID.toString();

    @Test
    void shouldReturn200WhenAcceptingValidInvitation() throws Exception {
        RoomParticipant participant = new RoomParticipant(
                UUID.randomUUID(), UUID.randomUUID(), USER_ID, "johndoe",
                ParticipantRole.PARTICIPANT, Instant.now(), null, false
        );

        when(acceptInvitationUseCase.accept(eq("abc12345"), eq(USER_ID), any()))
                .thenReturn(participant);

        mockMvc.perform(post("/api/v1/invitations/abc12345/accept")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.role").value("PARTICIPANT"));
    }

    @Test
    void shouldReturn404WhenTokenNotFound() throws Exception {
        when(acceptInvitationUseCase.accept(eq("invalid1"), eq(USER_ID), any()))
                .thenThrow(new ResourceNotFoundException("Invitation", "invalid1"));

        mockMvc.perform(post("/api/v1/invitations/invalid1/accept")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenTokenTooShort() throws Exception {
        mockMvc.perform(post("/api/v1/invitations/ab/accept")
                        .with(JwtTestHelper.withUser(USER_SUB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/invitations/abc12345/accept"))
                .andExpect(status().isUnauthorized());
    }
}
