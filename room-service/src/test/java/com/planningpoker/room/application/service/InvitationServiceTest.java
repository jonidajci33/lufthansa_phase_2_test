package com.planningpoker.room.application.service;

import com.planningpoker.room.application.port.out.InvitationPersistencePort;
import com.planningpoker.room.application.port.out.RoomEventPublisherPort;
import com.planningpoker.room.application.port.out.RoomPersistencePort;
import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.InvitationStatus;
import com.planningpoker.room.domain.InvitationType;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import com.planningpoker.room.web.dto.InviteRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationPersistencePort invitationPersistencePort;

    @Mock
    private RoomPersistencePort roomPersistencePort;

    @Mock
    private RoomEventPublisherPort eventPublisherPort;

    @InjectMocks
    private InvitationService invitationService;

    // ── Helpers ───────────────────────────────────────────────────────

    private static final UUID MODERATOR_ID = UUID.randomUUID();
    private static final UUID ROOM_ID = UUID.randomUUID();

    private static Room activeRoom() {
        Instant now = Instant.now();
        RoomParticipant moderator = new RoomParticipant(
                UUID.randomUUID(), ROOM_ID, MODERATOR_ID, "moderator", ParticipantRole.MODERATOR,
                now, null, true
        );
        return new Room(ROOM_ID, "Room", null, MODERATOR_ID,
                null, "CODE", RoomStatus.ACTIVE, 50,
                new ArrayList<>(List.of(moderator)), now, now);
    }

    private static Invitation pendingInvitation(UUID roomId) {
        Instant now = Instant.now();
        return new Invitation(
                UUID.randomUUID(), roomId, MODERATOR_ID,
                "user@example.com", "abc12345", InvitationType.EMAIL,
                InvitationStatus.PENDING, now.plus(7, ChronoUnit.DAYS), null, now
        );
    }

    private static Invitation expiredInvitation(UUID roomId) {
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        return new Invitation(
                UUID.randomUUID(), roomId, MODERATOR_ID,
                "user@example.com", "expired1", InvitationType.EMAIL,
                InvitationStatus.PENDING, past, null, past.minus(7, ChronoUnit.DAYS)
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // Invite via EMAIL
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldInviteViaEmail() {
        Room room = activeRoom();
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(invitationPersistencePort.findPendingByEmailAndRoomId("user@example.com", ROOM_ID))
                .thenReturn(Optional.empty());
        when(invitationPersistencePort.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InviteRequest request = new InviteRequest("user@example.com", "EMAIL");

        Invitation result = invitationService.invite(request, ROOM_ID, MODERATOR_ID);

        assertThat(result).isNotNull();
        assertThat(result.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getType()).isEqualTo(InvitationType.EMAIL);
        assertThat(result.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiresAt()).isNotNull();

        verify(invitationPersistencePort).save(any(Invitation.class));
        verify(eventPublisherPort).publishUserInvited(eq(result), eq("Room"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Invite via LINK
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldInviteViaLink() {
        Room room = activeRoom();
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(invitationPersistencePort.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InviteRequest request = new InviteRequest(null, "LINK");

        Invitation result = invitationService.invite(request, ROOM_ID, MODERATOR_ID);

        assertThat(result.getType()).isEqualTo(InvitationType.LINK);
        assertThat(result.getEmail()).isNull();
        verify(eventPublisherPort).publishUserInvited(eq(result), eq("Room"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Only Moderator Can Invite
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldThrowWhenNonModeratorInvites() {
        Room room = activeRoom();
        UUID nonModerator = UUID.randomUUID();
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.of(room));

        InviteRequest request = new InviteRequest("user@test.com", "EMAIL");

        assertThatThrownBy(() -> invitationService.invite(request, ROOM_ID, nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(invitationPersistencePort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Accept Invitation
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldAcceptInvitation() {
        Room room = activeRoom();
        Invitation invitation = pendingInvitation(ROOM_ID);
        UUID newUserId = UUID.randomUUID();

        when(invitationPersistencePort.findByToken(invitation.getToken()))
                .thenReturn(Optional.of(invitation));
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoomParticipant result = invitationService.accept(invitation.getToken(), newUserId, "newuser");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(newUserId);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getRole()).isEqualTo(ParticipantRole.PARTICIPANT);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

        verify(invitationPersistencePort).save(invitation);
        verify(roomPersistencePort).save(any(Room.class));
        verify(eventPublisherPort).publishUserJoinedRoom(any(Room.class), any(RoomParticipant.class));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Accept Expired Invitation
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldThrowWhenAcceptingExpiredInvitation() {
        Invitation invitation = expiredInvitation(ROOM_ID);
        UUID newUserId = UUID.randomUUID();

        when(invitationPersistencePort.findByToken(invitation.getToken()))
                .thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.accept(invitation.getToken(), newUserId, "newuser"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("INVITATION_EXPIRED");
                });
    }

    // ═══════════════════════════════════════════════════════════════════
    // Accept Non-Existent Invitation
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldThrowWhenInvitationNotFound() {
        when(invitationPersistencePort.findByToken("nonexistent"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.accept("nonexistent", UUID.randomUUID(), "newuser"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Accept Already Accepted Invitation
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldThrowWhenAcceptingAlreadyAcceptedInvitation() {
        Invitation invitation = pendingInvitation(ROOM_ID);
        invitation.accept(); // mark as already accepted

        when(invitationPersistencePort.findByToken(invitation.getToken()))
                .thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.accept(invitation.getToken(), UUID.randomUUID(), "newuser"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("INVITATION_NOT_PENDING");
                });
    }

    // ═══════════════════════════════════════════════════════════════════
    // Duplicate Invitation Guard
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnExistingInvitationWhenDuplicateEmailInvite() {
        Room room = activeRoom();
        Invitation existing = pendingInvitation(ROOM_ID);
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(invitationPersistencePort.findPendingByEmailAndRoomId("user@example.com", ROOM_ID))
                .thenReturn(Optional.of(existing));

        InviteRequest request = new InviteRequest("user@example.com", "EMAIL");

        Invitation result = invitationService.invite(request, ROOM_ID, MODERATOR_ID);

        assertThat(result).isEqualTo(existing);
        verify(invitationPersistencePort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Email Required for EMAIL Type
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldThrowWhenEmailTypeWithoutEmail() {
        Room room = activeRoom();
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.of(room));

        InviteRequest request = new InviteRequest(null, "EMAIL");

        assertThatThrownBy(() -> invitationService.invite(request, ROOM_ID, MODERATOR_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("EMAIL_REQUIRED");
                });

        verify(invitationPersistencePort, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Room Not Found for Invite
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldThrowWhenRoomNotFoundForInvite() {
        when(roomPersistencePort.findById(ROOM_ID)).thenReturn(Optional.empty());

        InviteRequest request = new InviteRequest("test@test.com", "EMAIL");

        assertThatThrownBy(() -> invitationService.invite(request, ROOM_ID, MODERATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
