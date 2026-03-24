package com.planningpoker.room.application.service;

import com.planningpoker.room.application.port.in.CreateRoomUseCase;
import com.planningpoker.room.application.port.in.DeleteRoomUseCase;
import com.planningpoker.room.application.port.in.GetRoomUseCase;
import com.planningpoker.room.application.port.in.JoinRoomUseCase;
import com.planningpoker.room.application.port.in.ListParticipantsUseCase;
import com.planningpoker.room.application.port.in.ListUserRoomsUseCase;
import com.planningpoker.room.application.port.in.RemoveParticipantUseCase;
import com.planningpoker.room.application.port.in.UpdateRoomUseCase;
import com.planningpoker.room.application.port.out.DeckTypePersistencePort;
import com.planningpoker.room.application.port.out.RoomEventPublisherPort;
import com.planningpoker.room.application.port.out.RoomPersistencePort;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import com.planningpoker.room.web.dto.CreateRoomRequest;
import com.planningpoker.room.web.dto.UpdateRoomRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates room-related use cases.
 * <p>
 * Coordinates persistence and event publishing without leaking
 * infrastructure details into the domain model.
 */
@Service
@Transactional
public class RoomService implements CreateRoomUseCase, GetRoomUseCase,
        UpdateRoomUseCase, DeleteRoomUseCase, ListUserRoomsUseCase,
        JoinRoomUseCase, ListParticipantsUseCase, RemoveParticipantUseCase {

    private final RoomPersistencePort roomPersistencePort;
    private final DeckTypePersistencePort deckTypePersistencePort;
    private final RoomEventPublisherPort eventPublisherPort;

    public RoomService(RoomPersistencePort roomPersistencePort,
                       DeckTypePersistencePort deckTypePersistencePort,
                       RoomEventPublisherPort eventPublisherPort) {
        this.roomPersistencePort = roomPersistencePort;
        this.deckTypePersistencePort = deckTypePersistencePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    // -- CreateRoomUseCase ------------------------------------------------

    @Override
    public Room create(CreateRoomRequest request, UUID moderatorId, String username) {
        DeckType deckType = null;
        if (request.deckTypeId() != null) {
            deckType = deckTypePersistencePort.findById(request.deckTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DeckType", request.deckTypeId()));
        }

        Instant now = Instant.now();
        String shortCode = UUID.randomUUID().toString().substring(0, 8);

        Room room = new Room();
        room.setId(UUID.randomUUID());
        room.setName(request.name());
        room.setDescription(request.description());
        room.setModeratorId(moderatorId);
        room.setDeckType(deckType);
        room.setShortCode(shortCode);
        room.setStatus(RoomStatus.ACTIVE);
        room.setMaxParticipants(request.maxParticipants() != null ? request.maxParticipants() : 50);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);

        // Creator is automatically added as moderator participant
        RoomParticipant moderator = new RoomParticipant();
        moderator.setId(UUID.randomUUID());
        moderator.setRoomId(room.getId());
        moderator.setUserId(moderatorId);
        moderator.setUsername(username);
        moderator.setRole(ParticipantRole.MODERATOR);
        moderator.setJoinedAt(now);
        moderator.setConnected(false);
        room.addParticipant(moderator);

        Room saved = roomPersistencePort.save(room);
        eventPublisherPort.publishRoomCreated(saved);
        return saved;
    }

    // -- GetRoomUseCase ---------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Room getById(UUID id) {
        return roomPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Room getByShortCode(String shortCode) {
        return roomPersistencePort.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room", shortCode));
    }

    // -- UpdateRoomUseCase ------------------------------------------------

    @Override
    public Room update(UUID id, UpdateRoomRequest request, UUID requesterId) {
        Room room = roomPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));

        if (!room.isModeratedBy(requesterId)) {
            throw new BusinessException("NOT_MODERATOR", "Only the room moderator can update the room");
        }

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("ROOM_NOT_ACTIVE", "Cannot update an archived room");
        }

        room.update(request.name(), request.description(), request.maxParticipants());

        Room saved = roomPersistencePort.save(room);
        eventPublisherPort.publishRoomUpdated(saved);
        return saved;
    }

    // -- DeleteRoomUseCase ------------------------------------------------

    @Override
    public void delete(UUID id, UUID requesterId) {
        Room room = roomPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));

        if (!room.isModeratedBy(requesterId)) {
            throw new BusinessException("NOT_MODERATOR", "Only the room moderator can delete the room");
        }

        roomPersistencePort.delete(room);
        eventPublisherPort.publishRoomDeleted(room);
    }

    @Override
    public void deleteAsAdmin(UUID id) {
        Room room = roomPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));

        roomPersistencePort.delete(room);
        eventPublisherPort.publishRoomDeleted(room);
    }

    // -- ListUserRoomsUseCase ---------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<Room> listForUser(UUID userId, int offset, int limit) {
        return roomPersistencePort.findByParticipantUserId(userId, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Room> listAll(int offset, int limit) {
        return roomPersistencePort.findAll(offset, limit);
    }

    // -- JoinRoomUseCase --------------------------------------------------

    @Override
    public RoomParticipant join(UUID roomId, UUID userId, String username) {
        Room room = roomPersistencePort.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("ROOM_NOT_ACTIVE", "Cannot join an archived room");
        }

        // If user is already a participant, return existing (idempotent)
        RoomParticipant existing = room.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId) && p.getLeftAt() == null)
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        if (room.isFull()) {
            throw new BusinessException("ROOM_FULL", "Room has reached its maximum number of participants");
        }

        Instant now = Instant.now();
        RoomParticipant participant = new RoomParticipant();
        participant.setId(UUID.randomUUID());
        participant.setRoomId(roomId);
        participant.setUserId(userId);
        participant.setUsername(username);
        participant.setRole(ParticipantRole.PARTICIPANT);
        participant.setJoinedAt(now);
        participant.setConnected(false);

        room.addParticipant(participant);
        Room saved = roomPersistencePort.save(room);

        eventPublisherPort.publishUserJoinedRoom(saved, participant);
        return participant;
    }

    // -- ListParticipantsUseCase ------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<RoomParticipant> listParticipants(UUID roomId) {
        Room room = roomPersistencePort.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        return room.getParticipants();
    }

    // -- RemoveParticipantUseCase -----------------------------------------

    @Override
    public void remove(UUID roomId, UUID targetUserId, UUID requesterId) {
        Room room = roomPersistencePort.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("ROOM_NOT_ACTIVE", "Cannot modify participants in an archived room");
        }

        if (!room.isModeratedBy(requesterId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "NOT_MODERATOR",
                    "Only the room moderator can remove participants");
        }

        if (room.isModeratedBy(targetUserId)) {
            throw new BusinessException("CANNOT_REMOVE_MODERATOR",
                    "The room moderator cannot be removed");
        }

        RoomParticipant target = room.getParticipants().stream()
                .filter(p -> p.getUserId().equals(targetUserId) && p.getLeftAt() == null)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant", targetUserId));

        room.removeParticipant(targetUserId);
        Room saved = roomPersistencePort.save(room);

        eventPublisherPort.publishUserLeftRoom(saved, target);
    }
}
