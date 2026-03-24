package com.planningpoker.room.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTest {

    // ── Factory helpers ───────────────────────────────────────────────

    private static Room newDefaultRoom() {
        return new Room();
    }

    private static Room newFullRoom() {
        Instant now = Instant.now();
        UUID roomId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        DeckType deckType = new DeckType(
                UUID.randomUUID(), "Fibonacci", DeckCategory.FIBONACCI,
                true, null, List.of(), now
        );

        RoomParticipant moderator = new RoomParticipant(
                UUID.randomUUID(), roomId, moderatorId, "moderator", ParticipantRole.MODERATOR,
                now, null, true
        );

        return new Room(
                roomId, "Sprint Planning", "Weekly sprint planning",
                moderatorId, deckType, "ABC12345", RoomStatus.ACTIVE,
                50, new ArrayList<>(List.of(moderator)), now, now
        );
    }

    // ── Constructor / default values ──────────────────────────────────

    @Test
    void shouldCreateRoomWithDefaults() {
        Room room = newDefaultRoom();

        assertThat(room.getStatus()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(room.getMaxParticipants()).isEqualTo(50);
        assertThat(room.getParticipants()).isEmpty();
        assertThat(room.getId()).isNull();
        assertThat(room.getName()).isNull();
        assertThat(room.getDescription()).isNull();
    }

    @Test
    void shouldCreateRoomWithAllArgsConstructor() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        Room room = new Room(id, "Room1", "Desc", moderatorId,
                null, "ABCD1234", RoomStatus.ACTIVE, 10,
                null, now, now);

        assertThat(room.getId()).isEqualTo(id);
        assertThat(room.getName()).isEqualTo("Room1");
        assertThat(room.getDescription()).isEqualTo("Desc");
        assertThat(room.getModeratorId()).isEqualTo(moderatorId);
        assertThat(room.getShortCode()).isEqualTo("ABCD1234");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(room.getMaxParticipants()).isEqualTo(10);
        assertThat(room.getParticipants()).isEmpty();
        assertThat(room.getCreatedAt()).isEqualTo(now);
        assertThat(room.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldHandleNullParticipantsInConstructor() {
        Room room = new Room(UUID.randomUUID(), "Room", null, UUID.randomUUID(),
                null, "CODE", RoomStatus.ACTIVE, 50, null,
                Instant.now(), Instant.now());

        assertThat(room.getParticipants()).isNotNull().isEmpty();
    }

    // ── update() ──────────────────────────────────────────────────────

    @Test
    void shouldUpdateName() {
        Room room = newFullRoom();

        room.update("New Name", null, null);

        assertThat(room.getName()).isEqualTo("New Name");
        assertThat(room.getDescription()).isEqualTo("Weekly sprint planning");
    }

    @Test
    void shouldUpdateDescription() {
        Room room = newFullRoom();

        room.update(null, "New description", null);

        assertThat(room.getDescription()).isEqualTo("New description");
        assertThat(room.getName()).isEqualTo("Sprint Planning");
    }

    @Test
    void shouldUpdateMaxParticipants() {
        Room room = newFullRoom();

        room.update(null, null, 100);

        assertThat(room.getMaxParticipants()).isEqualTo(100);
    }

    @Test
    void shouldUpdateAllFields() {
        Room room = newFullRoom();
        Instant beforeUpdate = Instant.now();

        room.update("New Name", "New Desc", 25);

        assertThat(room.getName()).isEqualTo("New Name");
        assertThat(room.getDescription()).isEqualTo("New Desc");
        assertThat(room.getMaxParticipants()).isEqualTo(25);
        assertThat(room.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void shouldUpdateTimestampEvenWhenAllNulls() {
        Room room = newFullRoom();
        Instant beforeUpdate = Instant.now();

        room.update(null, null, null);

        assertThat(room.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    // ── archive() ─────────────────────────────────────────────────────

    @Test
    void shouldArchiveRoom() {
        Room room = newFullRoom();
        assertThat(room.getStatus()).isEqualTo(RoomStatus.ACTIVE);

        Instant beforeArchive = Instant.now();
        room.archive();

        assertThat(room.getStatus()).isEqualTo(RoomStatus.ARCHIVED);
        assertThat(room.getUpdatedAt()).isAfterOrEqualTo(beforeArchive);
    }

    @Test
    void shouldBeIdempotentWhenArchivingAlreadyArchived() {
        Room room = newFullRoom();
        room.archive();
        assertThat(room.getStatus()).isEqualTo(RoomStatus.ARCHIVED);

        // Archiving again should not throw
        room.archive();
        assertThat(room.getStatus()).isEqualTo(RoomStatus.ARCHIVED);
    }

    // ── isModeratedBy() ───────────────────────────────────────────────

    @Test
    void shouldReturnTrueForModerator() {
        Room room = newFullRoom();
        assertThat(room.isModeratedBy(room.getModeratorId())).isTrue();
    }

    @Test
    void shouldReturnFalseForNonModerator() {
        Room room = newFullRoom();
        assertThat(room.isModeratedBy(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldReturnFalseForNull() {
        Room room = newFullRoom();
        assertThat(room.isModeratedBy(null)).isFalse();
    }

    // ── addParticipant() ──────────────────────────────────────────────

    @Test
    void shouldAddParticipant() {
        Room room = newFullRoom();
        int initialSize = room.getParticipants().size();

        RoomParticipant participant = new RoomParticipant(
                UUID.randomUUID(), room.getId(), UUID.randomUUID(), "participant",
                ParticipantRole.PARTICIPANT, Instant.now(), null, true
        );

        room.addParticipant(participant);

        assertThat(room.getParticipants()).hasSize(initialSize + 1);
    }

    @Test
    void shouldThrowWhenAddingNullParticipant() {
        Room room = newFullRoom();

        assertThatThrownBy(() -> room.addParticipant(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("participant must not be null");
    }

    // ── removeParticipant() ───────────────────────────────────────────

    @Test
    void shouldRemoveParticipantByUserId() {
        Room room = newFullRoom();
        UUID userId = room.getParticipants().get(0).getUserId();

        room.removeParticipant(userId);

        assertThat(room.getParticipants()).isEmpty();
    }

    @Test
    void shouldBeIdempotentWhenRemovingAbsentUser() {
        Room room = newFullRoom();
        int size = room.getParticipants().size();

        room.removeParticipant(UUID.randomUUID());

        assertThat(room.getParticipants()).hasSize(size);
    }

    // ── isFull() ──────────────────────────────────────────────────────

    @Test
    void shouldReturnFalseWhenNotFull() {
        Room room = newFullRoom();
        assertThat(room.isFull()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAtMaxCapacity() {
        Instant now = Instant.now();
        UUID roomId = UUID.randomUUID();

        Room room = new Room(roomId, "Room", null, UUID.randomUUID(),
                null, "CODE", RoomStatus.ACTIVE, 1,
                new ArrayList<>(), now, now);

        RoomParticipant participant = new RoomParticipant(
                UUID.randomUUID(), roomId, UUID.randomUUID(), "participant",
                ParticipantRole.PARTICIPANT, now, null, true
        );
        room.addParticipant(participant);

        assertThat(room.isFull()).isTrue();
    }

    @Test
    void shouldNotCountLeftParticipantsInFull() {
        Instant now = Instant.now();
        UUID roomId = UUID.randomUUID();

        Room room = new Room(roomId, "Room", null, UUID.randomUUID(),
                null, "CODE", RoomStatus.ACTIVE, 1,
                new ArrayList<>(), now, now);

        RoomParticipant leftParticipant = new RoomParticipant(
                UUID.randomUUID(), roomId, UUID.randomUUID(), "left-user",
                ParticipantRole.PARTICIPANT, now, now, false
        );
        room.addParticipant(leftParticipant);

        // Left participant should not count
        assertThat(room.isFull()).isFalse();
    }

    // ── getParticipants() returns unmodifiable list ────────────────────

    @Test
    void shouldReturnUnmodifiableParticipantsList() {
        Room room = newFullRoom();

        List<RoomParticipant> participants = room.getParticipants();

        assertThatThrownBy(() -> participants.add(new RoomParticipant()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ── equals / hashCode / toString ──────────────────────────────────

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Room room1 = new Room(id, "Room1", null, UUID.randomUUID(),
                null, "CODE1", RoomStatus.ACTIVE, 50, null, now, now);
        Room room2 = new Room(id, "Room2", null, UUID.randomUUID(),
                null, "CODE2", RoomStatus.ARCHIVED, 10, null, now, now);

        assertThat(room1).isEqualTo(room2);
        assertThat(room1.hashCode()).isEqualTo(room2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Instant now = Instant.now();
        Room room1 = new Room(UUID.randomUUID(), "Room", null, UUID.randomUUID(),
                null, "CODE", RoomStatus.ACTIVE, 50, null, now, now);
        Room room2 = new Room(UUID.randomUUID(), "Room", null, UUID.randomUUID(),
                null, "CODE", RoomStatus.ACTIVE, 50, null, now, now);

        assertThat(room1).isNotEqualTo(room2);
    }

    @Test
    void shouldIncludeKeyFieldsInToString() {
        Room room = newFullRoom();
        String str = room.toString();

        assertThat(str).contains("Room{");
        assertThat(str).contains(room.getName());
        assertThat(str).contains(room.getShortCode());
    }
}
