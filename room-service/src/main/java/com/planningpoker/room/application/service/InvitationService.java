package com.planningpoker.room.application.service;

import com.planningpoker.room.application.port.in.AcceptInvitationUseCase;
import com.planningpoker.room.application.port.in.InviteUserUseCase;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Application service that orchestrates invitation-related use cases.
 * <p>
 * Handles invitation creation, acceptance, and the automatic room-join
 * that follows a successful acceptance.
 */
@Service
@Transactional
public class InvitationService implements InviteUserUseCase, AcceptInvitationUseCase {

    private static final int INVITATION_EXPIRY_DAYS = 7;

    private final InvitationPersistencePort invitationPersistencePort;
    private final RoomPersistencePort roomPersistencePort;
    private final RoomEventPublisherPort eventPublisherPort;

    public InvitationService(InvitationPersistencePort invitationPersistencePort,
                             RoomPersistencePort roomPersistencePort,
                             RoomEventPublisherPort eventPublisherPort) {
        this.invitationPersistencePort = invitationPersistencePort;
        this.roomPersistencePort = roomPersistencePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    // -- InviteUserUseCase ------------------------------------------------

    @Override
    public Invitation invite(InviteRequest request, UUID roomId, UUID inviterId) {
        Room room = roomPersistencePort.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (!room.isModeratedBy(inviterId)) {
            throw new BusinessException("NOT_MODERATOR", "Only the room moderator can invite users");
        }

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("ROOM_NOT_ACTIVE", "Cannot invite users to an archived room");
        }

        // Cross-field validation: EMAIL type requires an email address
        if ("EMAIL".equals(request.type()) && (request.email() == null || request.email().isBlank())) {
            throw new BusinessException("EMAIL_REQUIRED", "Email address is required for EMAIL-type invitations");
        }

        // Duplicate-invitation guard: return existing PENDING invitation if one exists
        if ("EMAIL".equals(request.type()) && request.email() != null) {
            var existing = invitationPersistencePort.findPendingByEmailAndRoomId(request.email(), roomId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Instant now = Instant.now();
        String token = UUID.randomUUID().toString().substring(0, 8);

        Invitation invitation = new Invitation();
        invitation.setId(UUID.randomUUID());
        invitation.setRoomId(roomId);
        invitation.setInvitedBy(inviterId);
        invitation.setEmail(request.email());
        invitation.setToken(token);
        invitation.setType(InvitationType.valueOf(request.type()));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(now.plus(INVITATION_EXPIRY_DAYS, ChronoUnit.DAYS));
        invitation.setCreatedAt(now);

        Invitation saved = invitationPersistencePort.save(invitation);
        eventPublisherPort.publishUserInvited(saved, room.getName());
        return saved;
    }

    // -- AcceptInvitationUseCase ------------------------------------------

    @Override
    public RoomParticipant accept(String token, UUID userId, String username) {
        Invitation invitation = invitationPersistencePort.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", token));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("INVITATION_NOT_PENDING",
                    "Invitation is not in PENDING status; current status: " + invitation.getStatus());
        }

        if (invitation.isExpired()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationPersistencePort.save(invitation);
            throw new BusinessException("INVITATION_EXPIRED", "This invitation has expired");
        }

        Room room = roomPersistencePort.findById(invitation.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", invitation.getRoomId()));

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("ROOM_NOT_ACTIVE", "Cannot join an archived room");
        }

        // Check if user is already a participant
        boolean alreadyJoined = room.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId) && p.getLeftAt() == null);
        if (alreadyJoined) {
            throw new BusinessException("ALREADY_JOINED", "User is already a participant in this room");
        }

        if (room.isFull()) {
            throw new BusinessException("ROOM_FULL", "Room has reached its maximum number of participants");
        }

        // Mark invitation as accepted
        invitation.accept();
        invitationPersistencePort.save(invitation);

        // Auto-join room
        Instant now = Instant.now();
        RoomParticipant participant = new RoomParticipant();
        participant.setId(UUID.randomUUID());
        participant.setRoomId(room.getId());
        participant.setUserId(userId);
        participant.setUsername(username);
        participant.setRole(ParticipantRole.PARTICIPANT);
        participant.setJoinedAt(now);
        participant.setConnected(false);

        room.addParticipant(participant);
        roomPersistencePort.save(room);

        eventPublisherPort.publishUserJoinedRoom(room, participant);
        return participant;
    }
}
