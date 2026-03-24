package com.planningpoker.room.application.service;

import com.planningpoker.room.application.port.out.DeckTypePersistencePort;
import com.planningpoker.room.application.port.out.RoomEventPublisherPort;
import com.planningpoker.room.application.port.out.RoomPersistencePort;
import com.planningpoker.room.domain.DeckCategory;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomPersistencePort roomPersistencePort;

    @Mock
    private DeckTypePersistencePort deckTypePersistencePort;

    @Mock
    private RoomEventPublisherPort eventPublisherPort;

    @InjectMocks
    private RoomService roomService;

    @Captor
    private ArgumentCaptor<Room> roomCaptor;

    // ── Helpers ───────────────────────────────────────────────────────

    private static final UUID MODERATOR_ID = UUID.randomUUID();
    private static final UUID DECK_TYPE_ID = UUID.randomUUID();

    private static DeckType sampleDeckType() {
        return new DeckType(DECK_TYPE_ID, "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(), Instant.now());
    }

    private static Room existingRoom() {
        Instant now = Instant.now();
        UUID roomId = UUID.randomUUID();

        RoomParticipant moderator = new RoomParticipant(
                UUID.randomUUID(), roomId, MODERATOR_ID, "moderator", ParticipantRole.MODERATOR,
                now, null, true
        );

        return new Room(roomId, "Sprint Planning", "Weekly sprint",
                MODERATOR_ID, sampleDeckType(), "ABCD1234", RoomStatus.ACTIVE,
                50, new ArrayList<>(List.of(moderator)), now, now);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Create
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCreateRoomSuccessfully() {
        CreateRoomRequest request = new CreateRoomRequest(
                "Sprint Planning", "Weekly sprint", DECK_TYPE_ID, 30);

        when(deckTypePersistencePort.findById(DECK_TYPE_ID))
                .thenReturn(Optional.of(sampleDeckType()));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.create(request, MODERATOR_ID, "moderator");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Sprint Planning");
        assertThat(result.getDescription()).isEqualTo("Weekly sprint");
        assertThat(result.getModeratorId()).isEqualTo(MODERATOR_ID);
        assertThat(result.getShortCode()).isNotBlank();
        assertThat(result.getStatus()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(result.getMaxParticipants()).isEqualTo(30);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(roomPersistencePort).save(any(Room.class));
        verify(eventPublisherPort).publishRoomCreated(result);
    }

    @Test
    void shouldGenerateShortCodeOnCreate() {
        CreateRoomRequest request = new CreateRoomRequest(
                "Room", null, DECK_TYPE_ID, null);

        when(deckTypePersistencePort.findById(DECK_TYPE_ID))
                .thenReturn(Optional.of(sampleDeckType()));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.create(request, MODERATOR_ID, "moderator");

        assertThat(result.getShortCode()).isNotNull().isNotBlank();
    }

    @Test
    void shouldAddModeratorAsParticipantOnCreate() {
        CreateRoomRequest request = new CreateRoomRequest(
                "Room", null, DECK_TYPE_ID, null);

        when(deckTypePersistencePort.findById(DECK_TYPE_ID))
                .thenReturn(Optional.of(sampleDeckType()));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.create(request, MODERATOR_ID, "moderator");

        assertThat(result.getParticipants()).hasSize(1);
        RoomParticipant moderator = result.getParticipants().get(0);
        assertThat(moderator.getUserId()).isEqualTo(MODERATOR_ID);
        assertThat(moderator.getRole()).isEqualTo(ParticipantRole.MODERATOR);
    }

    @Test
    void shouldPublishEventOnCreate() {
        CreateRoomRequest request = new CreateRoomRequest(
                "Room", null, DECK_TYPE_ID, null);

        when(deckTypePersistencePort.findById(DECK_TYPE_ID))
                .thenReturn(Optional.of(sampleDeckType()));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.create(request, MODERATOR_ID, "moderator");

        verify(eventPublisherPort).publishRoomCreated(result);
    }

    @Test
    void shouldUseDefaultMaxParticipantsWhenNull() {
        CreateRoomRequest request = new CreateRoomRequest(
                "Room", null, DECK_TYPE_ID, null);

        when(deckTypePersistencePort.findById(DECK_TYPE_ID))
                .thenReturn(Optional.of(sampleDeckType()));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.create(request, MODERATOR_ID, "moderator");

        assertThat(result.getMaxParticipants()).isEqualTo(50);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Update
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldUpdateRoomByModerator() {
        Room room = existingRoom();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateRoomRequest request = new UpdateRoomRequest("Updated Name", "Updated desc", 100);

        Room result = roomService.update(room.getId(), request, MODERATOR_ID);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated desc");
        assertThat(result.getMaxParticipants()).isEqualTo(100);
        verify(eventPublisherPort).publishRoomUpdated(result);
    }

    @Test
    void shouldThrowWhenNonModeratorUpdates() {
        Room room = existingRoom();
        UUID otherId = UUID.randomUUID();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        UpdateRoomRequest request = new UpdateRoomRequest("New", null, null);

        assertThatThrownBy(() -> roomService.update(room.getId(), request, otherId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(roomPersistencePort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void shouldThrowWhenUpdatingArchivedRoom() {
        Room room = existingRoom();
        room.archive();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        UpdateRoomRequest request = new UpdateRoomRequest("New", null, null);

        assertThatThrownBy(() -> roomService.update(room.getId(), request, MODERATOR_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("ROOM_NOT_ACTIVE");
                });
    }

    // ═══════════════════════════════════════════════════════════════════
    // Delete
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldDeleteRoomByModerator() {
        Room room = existingRoom();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        roomService.delete(room.getId(), MODERATOR_ID);

        verify(roomPersistencePort).delete(room);
        verify(eventPublisherPort).publishRoomDeleted(room);
    }

    @Test
    void shouldThrowWhenNonModeratorDeletes() {
        Room room = existingRoom();
        UUID otherId = UUID.randomUUID();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.delete(room.getId(), otherId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(roomPersistencePort, never()).delete(any());
        verifyNoInteractions(eventPublisherPort);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetById
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnRoomWhenExists() {
        Room room = existingRoom();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        Room result = roomService.getById(room.getId());

        assertThat(result).isEqualTo(room);
        verify(roomPersistencePort).findById(room.getId());
    }

    @Test
    void shouldThrowWhenRoomNotFound() {
        UUID id = UUID.randomUUID();
        when(roomPersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetByShortCode
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnRoomByShortCode() {
        Room room = existingRoom();
        when(roomPersistencePort.findByShortCode("ABCD1234")).thenReturn(Optional.of(room));

        Room result = roomService.getByShortCode("ABCD1234");

        assertThat(result).isEqualTo(room);
    }

    @Test
    void shouldThrowWhenShortCodeNotFound() {
        when(roomPersistencePort.findByShortCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getByShortCode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ListForUser
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnPaginatedUserRooms() {
        Room room = existingRoom();
        Page<Room> page = new Page<>(List.of(room), 1L);
        when(roomPersistencePort.findByParticipantUserId(MODERATOR_ID, 0, 20))
                .thenReturn(page);

        Page<Room> result = roomService.listForUser(MODERATOR_ID, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Join
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldJoinRoomSuccessfully() {
        Room room = existingRoom();
        UUID newUserId = UUID.randomUUID();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoomParticipant result = roomService.join(room.getId(), newUserId, "newuser");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(newUserId);
        assertThat(result.getRole()).isEqualTo(ParticipantRole.PARTICIPANT);
        verify(eventPublisherPort).publishUserJoinedRoom(any(Room.class), any(RoomParticipant.class));
    }

    @Test
    void shouldThrowWhenJoiningFullRoom() {
        Instant now = Instant.now();
        UUID roomId = UUID.randomUUID();

        RoomParticipant existing = new RoomParticipant(
                UUID.randomUUID(), roomId, UUID.randomUUID(), "existing",
                ParticipantRole.MODERATOR, now, null, true
        );

        Room room = new Room(roomId, "Room", null, existing.getUserId(),
                null, "CODE", RoomStatus.ACTIVE, 1,
                new ArrayList<>(List.of(existing)), now, now);

        when(roomPersistencePort.findById(roomId)).thenReturn(Optional.of(room));

        UUID newUserId = UUID.randomUUID();
        assertThatThrownBy(() -> roomService.join(roomId, newUserId, "newuser"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("ROOM_FULL");
                });

        verify(roomPersistencePort, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════
    // ListParticipants
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldListParticipants() {
        Room room = existingRoom();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        List<RoomParticipant> result = roomService.listParticipants(room.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(ParticipantRole.MODERATOR);
    }

    @Test
    void shouldThrowWhenListingParticipantsOfNonExistentRoom() {
        UUID id = UUID.randomUUID();
        when(roomPersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.listParticipants(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // RemoveParticipant
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldRemoveParticipantByModerator() {
        Room room = existingRoom();
        UUID participantUserId = UUID.randomUUID();
        RoomParticipant participant = new RoomParticipant(
                UUID.randomUUID(), room.getId(), participantUserId, "alice",
                ParticipantRole.PARTICIPANT, Instant.now(), null, true
        );
        room.addParticipant(participant);

        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomPersistencePort.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        roomService.remove(room.getId(), participantUserId, MODERATOR_ID);

        verify(roomPersistencePort).save(roomCaptor.capture());
        Room saved = roomCaptor.getValue();
        assertThat(saved.getParticipants()).noneMatch(p -> p.getUserId().equals(participantUserId));
        verify(eventPublisherPort).publishUserLeftRoom(any(Room.class), any(RoomParticipant.class));
    }

    @Test
    void shouldThrowWhenNonModeratorRemovesParticipant() {
        Room room = existingRoom();
        UUID otherId = UUID.randomUUID();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.remove(room.getId(), UUID.randomUUID(), otherId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(roomPersistencePort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void shouldThrowWhenRemovingModeratorFromRoom() {
        Room room = existingRoom();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.remove(room.getId(), MODERATOR_ID, MODERATOR_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("CANNOT_REMOVE_MODERATOR");
                });

        verify(roomPersistencePort, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingNonExistentParticipant() {
        Room room = existingRoom();
        UUID unknownUserId = UUID.randomUUID();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.remove(room.getId(), unknownUserId, MODERATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roomPersistencePort, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingFromArchivedRoom() {
        Room room = existingRoom();
        room.archive();
        when(roomPersistencePort.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.remove(room.getId(), UUID.randomUUID(), MODERATOR_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("ROOM_NOT_ACTIVE");
                });
    }

    @Test
    void shouldThrowWhenRoomNotFoundForRemove() {
        UUID id = UUID.randomUUID();
        when(roomPersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.remove(id, UUID.randomUUID(), MODERATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
