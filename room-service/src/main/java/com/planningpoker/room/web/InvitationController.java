package com.planningpoker.room.web;

import com.planningpoker.room.application.port.in.AcceptInvitationUseCase;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.web.dto.ParticipantResponse;
import com.planningpoker.room.web.mapper.ParticipantRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for invitation acceptance operations.
 */
@RestController
@RequestMapping("/api/v1/invitations")
@Tag(name = "Invitations")
public class InvitationController {

    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public InvitationController(AcceptInvitationUseCase acceptInvitationUseCase) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
    }

    @PostMapping("/{token}/accept")
    @Operation(summary = "Accept invitation", description = "Accepts an invitation to join a room using the invitation token.")
    @ApiResponse(responseCode = "200", description = "Invitation accepted, user joined the room")
    @ApiResponse(responseCode = "400", description = "Invitation expired or already accepted")
    @ApiResponse(responseCode = "404", description = "Invitation not found")
    public ResponseEntity<ParticipantResponse> accept(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String token) {

        // Basic token format validation
        if (token == null || token.length() < 4 || token.length() > 100) {
            return ResponseEntity.notFound().build();
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        RoomParticipant participant = acceptInvitationUseCase.accept(token, userId, username);
        return ResponseEntity.ok(ParticipantRestMapper.toResponse(participant));
    }
}
