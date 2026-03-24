package com.planningpoker.room.web;

import com.planningpoker.room.application.port.in.CreateRoomUseCase;
import com.planningpoker.room.application.port.in.DeleteRoomUseCase;
import com.planningpoker.room.application.port.in.GetRoomUseCase;
import com.planningpoker.room.application.port.in.InviteUserUseCase;
import com.planningpoker.room.application.port.in.JoinRoomUseCase;
import com.planningpoker.room.application.port.in.ListParticipantsUseCase;
import com.planningpoker.room.application.port.in.ListUserRoomsUseCase;
import com.planningpoker.room.application.port.in.RemoveParticipantUseCase;
import com.planningpoker.room.application.port.in.UpdateRoomUseCase;
import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.web.dto.CreateRoomRequest;
import com.planningpoker.room.web.dto.InvitationResponse;
import com.planningpoker.room.web.dto.InviteRequest;
import com.planningpoker.room.web.dto.ParticipantResponse;
import com.planningpoker.room.web.dto.RoomResponse;
import com.planningpoker.room.web.dto.ShareLinkResponse;
import com.planningpoker.room.web.dto.UpdateRoomRequest;
import com.planningpoker.room.web.mapper.InvitationRestMapper;
import com.planningpoker.room.web.mapper.ParticipantRestMapper;
import com.planningpoker.room.web.mapper.RoomRestMapper;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for room management operations.
 */
@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Rooms")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final CreateRoomUseCase createRoomUseCase;
    private final UpdateRoomUseCase updateRoomUseCase;
    private final DeleteRoomUseCase deleteRoomUseCase;
    private final GetRoomUseCase getRoomUseCase;
    private final ListUserRoomsUseCase listUserRoomsUseCase;
    private final JoinRoomUseCase joinRoomUseCase;
    private final ListParticipantsUseCase listParticipantsUseCase;
    private final InviteUserUseCase inviteUserUseCase;
    private final RemoveParticipantUseCase removeParticipantUseCase;
    private final String frontendUrl;

    public RoomController(CreateRoomUseCase createRoomUseCase,
                          UpdateRoomUseCase updateRoomUseCase,
                          DeleteRoomUseCase deleteRoomUseCase,
                          GetRoomUseCase getRoomUseCase,
                          ListUserRoomsUseCase listUserRoomsUseCase,
                          JoinRoomUseCase joinRoomUseCase,
                          ListParticipantsUseCase listParticipantsUseCase,
                          InviteUserUseCase inviteUserUseCase,
                          RemoveParticipantUseCase removeParticipantUseCase,
                          @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.createRoomUseCase = createRoomUseCase;
        this.updateRoomUseCase = updateRoomUseCase;
        this.deleteRoomUseCase = deleteRoomUseCase;
        this.getRoomUseCase = getRoomUseCase;
        this.listUserRoomsUseCase = listUserRoomsUseCase;
        this.joinRoomUseCase = joinRoomUseCase;
        this.listParticipantsUseCase = listParticipantsUseCase;
        this.inviteUserUseCase = inviteUserUseCase;
        this.removeParticipantUseCase = removeParticipantUseCase;
        this.frontendUrl = frontendUrl;
    }

    @PostMapping
    @Operation(summary = "Create a new room", description = "Creates a new planning poker room with the authenticated user as moderator.")
    @ApiResponse(responseCode = "201", description = "Room created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<RoomResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody CreateRoomRequest request) {
        UUID moderatorId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        Room room = createRoomUseCase.create(request, moderatorId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomRestMapper.toResponse(room));
    }

    @GetMapping
    @Operation(summary = "List rooms", description = "Returns a paginated list of rooms. Admins see all rooms; regular users see only rooms they participate in.")
    @ApiResponse(responseCode = "200", description = "Paginated list of rooms")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<PageResponse<RoomResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        String sub = jwt != null ? jwt.getSubject() : null;
        if (sub == null) {
            log.error("JWT subject is null — cannot extract user ID. Token claims: {}", jwt != null ? jwt.getClaims() : "null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<Room> page;
        if (hasRole("ROLE_ADMIN")) {
            page = listUserRoomsUseCase.listAll(offset, limit);
        } else {
            UUID userId = UUID.fromString(sub);
            page = listUserRoomsUseCase.listForUser(userId, offset, limit);
        }

        List<RoomResponse> data = page.content().stream()
                .map(RoomRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(PageResponse.of(data, page.totalElements(), limit, offset));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID", description = "Returns a room by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Room found")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<RoomResponse> getById(@PathVariable UUID id) {
        Room room = getRoomUseCase.getById(id);
        return ResponseEntity.ok(RoomRestMapper.toResponse(room));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update room", description = "Updates a room's editable fields. Only the room moderator can update.")
    @ApiResponse(responseCode = "200", description = "Room updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<RoomResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                @PathVariable UUID id,
                                                @Valid @RequestBody UpdateRoomRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        Room room = updateRoomUseCase.update(id, request, requesterId);
        return ResponseEntity.ok(RoomRestMapper.toResponse(room));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete room", description = "Deletes a room. Only the room moderator or an admin can delete.")
    @ApiResponse(responseCode = "204", description = "Room deleted successfully")
    @ApiResponse(responseCode = "403", description = "Not the room moderator or admin")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID id) {
        if (hasRole("ROLE_ADMIN")) {
            deleteRoomUseCase.deleteAsAdmin(id);
        } else {
            UUID requesterId = UUID.fromString(jwt.getSubject());
            deleteRoomUseCase.delete(id, requesterId);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join room by ID", description = "Joins a room as a participant using the room UUID.")
    @ApiResponse(responseCode = "200", description = "Successfully joined the room")
    @ApiResponse(responseCode = "400", description = "Room is full")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<ParticipantResponse> join(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        RoomParticipant participant = joinRoomUseCase.join(id, userId, username);
        return ResponseEntity.ok(ParticipantRestMapper.toResponse(participant));
    }

    @PostMapping("/join/{shortCode}")
    @Operation(summary = "Join room by short code", description = "Looks up a room by its short code and joins as a participant.")
    @ApiResponse(responseCode = "200", description = "Successfully joined the room")
    @ApiResponse(responseCode = "400", description = "Room is full")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<RoomResponse> joinByShortCode(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable String shortCode) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        Room room = getRoomUseCase.getByShortCode(shortCode);
        joinRoomUseCase.join(room.getId(), userId, username);
        // Re-fetch room to include the newly joined participant
        Room updated = getRoomUseCase.getById(room.getId());
        return ResponseEntity.ok(RoomRestMapper.toResponse(updated));
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "List room participants", description = "Returns all participants in a room.")
    @ApiResponse(responseCode = "200", description = "List of participants")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<List<ParticipantResponse>> listParticipants(@PathVariable UUID id) {
        List<RoomParticipant> participants = listParticipantsUseCase.listParticipants(id);
        List<ParticipantResponse> response = participants.stream()
                .map(ParticipantRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private static boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    @DeleteMapping("/{roomId}/participants/{userId}")
    @Operation(summary = "Remove participant", description = "Removes a participant from the room. Only the room moderator can remove participants.")
    @ApiResponse(responseCode = "204", description = "Participant removed successfully")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Room or participant not found")
    public ResponseEntity<Void> removeParticipant(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID roomId,
                                                   @PathVariable UUID userId) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        removeParticipantUseCase.remove(roomId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Invite user to room", description = "Creates an invitation for a user to join this room. Only moderators can invite.")
    @ApiResponse(responseCode = "201", description = "Invitation created")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<InvitationResponse> invite(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable UUID id,
                                                      @Valid @RequestBody InviteRequest request) {
        UUID inviterId = UUID.fromString(jwt.getSubject());
        Invitation invitation = inviteUserUseCase.invite(request, id, inviterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(InvitationRestMapper.toResponse(invitation));
    }

    @PostMapping("/{id}/share-link")
    @Operation(summary = "Generate share link", description = "Returns a shareable link for the room using its short code. Only the moderator can generate share links.")
    @ApiResponse(responseCode = "200", description = "Share link generated")
    @ApiResponse(responseCode = "403", description = "Not the room moderator")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<ShareLinkResponse> generateShareLink(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        Room room = getRoomUseCase.getById(id);

        if (!room.isModeratedBy(requesterId)) {
            throw new BusinessException("NOT_MODERATOR", "Only the room moderator can generate share links");
        }

        String shareLink = frontendUrl + "/rooms/join/" + room.getShortCode();
        return ResponseEntity.ok(new ShareLinkResponse(room.getShortCode(), shareLink));
    }
}
