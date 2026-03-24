package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.Page;
import com.planningpoker.room.domain.ParticipantRole;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.domain.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RoomPersistenceAdapter.class)
class RoomPersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("room_test_db")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-schema.sql");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.schemas", () -> "room");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "room");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private RoomPersistenceAdapter adapter;

    @Autowired
    private RoomJpaRepository jpaRepository;

    @BeforeEach
    void cleanUp() {
        jpaRepository.deleteAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static Room buildRoom(UUID id, String name, String shortCode) {
        return new Room(
                id,
                name,
                "Description for " + name,
                UUID.randomUUID(),
                null,
                shortCode,
                RoomStatus.ACTIVE,
                50,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
    }

    private static Room buildRoomWithParticipant(UUID roomId, String name, String shortCode,
                                                  UUID participantUserId) {
        RoomParticipant participant = new RoomParticipant(
                UUID.randomUUID(),
                roomId,
                participantUserId,
                "user-" + participantUserId.toString().substring(0, 8),
                ParticipantRole.PARTICIPANT,
                Instant.now(),
                null,
                true
        );
        List<RoomParticipant> participants = new ArrayList<>();
        participants.add(participant);

        return new Room(
                roomId,
                name,
                "Description for " + name,
                UUID.randomUUID(),
                null,
                shortCode,
                RoomStatus.ACTIVE,
                50,
                participants,
                Instant.now(),
                Instant.now()
        );
    }

    // ── Tests ────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndFindRoomById() {
        UUID id = UUID.randomUUID();
        Room room = buildRoom(id, "Sprint Planning", "SP001");

        Room saved = adapter.save(room);

        assertThat(saved.getId()).isEqualTo(id);

        Optional<Room> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Sprint Planning");
        assertThat(found.get().getShortCode()).isEqualTo("SP001");
        assertThat(found.get().getStatus()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(found.get().getMaxParticipants()).isEqualTo(50);
    }

    @Test
    void shouldFindByShortCode() {
        UUID id = UUID.randomUUID();
        adapter.save(buildRoom(id, "Retro Room", "RET001"));

        Optional<Room> found = adapter.findByShortCode("RET001");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Retro Room");
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Test
    void shouldReturnEmptyForNonExistentRoom() {
        Optional<Room> byId = adapter.findById(UUID.randomUUID());
        assertThat(byId).isEmpty();

        Optional<Room> byCode = adapter.findByShortCode("NONEXIST");
        assertThat(byCode).isEmpty();
    }

    @Test
    void shouldDeleteRoom() {
        UUID id = UUID.randomUUID();
        Room room = buildRoom(id, "To Delete", "DEL001");
        adapter.save(room);

        assertThat(adapter.findById(id)).isPresent();

        adapter.delete(room);

        assertThat(adapter.findById(id)).isEmpty();
    }

    @Test
    void shouldSaveRoomWithParticipants() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Room room = buildRoomWithParticipant(roomId, "Team Room", "TEAM01", userId);

        Room saved = adapter.save(room);

        assertThat(saved.getParticipants()).hasSize(1);

        Optional<Room> found = adapter.findById(roomId);
        assertThat(found).isPresent();
        assertThat(found.get().getParticipants()).hasSize(1);
        assertThat(found.get().getParticipants().get(0).getUserId()).isEqualTo(userId);
        assertThat(found.get().getParticipants().get(0).getRole()).isEqualTo(ParticipantRole.PARTICIPANT);
        assertThat(found.get().getParticipants().get(0).isConnected()).isTrue();
    }

    @Test
    void shouldFindByParticipantUserId() {
        UUID userId = UUID.randomUUID();

        // Create 3 rooms where userId is a participant
        for (int i = 0; i < 3; i++) {
            adapter.save(buildRoomWithParticipant(
                    UUID.randomUUID(), "Room " + i, "USR0" + i, userId));
        }

        // Create 2 rooms where userId is NOT a participant
        for (int i = 0; i < 2; i++) {
            adapter.save(buildRoomWithParticipant(
                    UUID.randomUUID(), "Other " + i, "OTH0" + i, UUID.randomUUID()));
        }

        Page<Room> firstPage = adapter.findByParticipantUserId(userId, 0, 2);
        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.totalElements()).isEqualTo(3);

        Page<Room> secondPage = adapter.findByParticipantUserId(userId, 2, 2);
        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.totalElements()).isEqualTo(3);
    }

    @Test
    void shouldFindAllWithPagination() {
        for (int i = 0; i < 5; i++) {
            adapter.save(buildRoom(UUID.randomUUID(), "Room " + i, "ALL0" + i));
        }

        Page<Room> firstPage = adapter.findAll(0, 3);
        assertThat(firstPage.content()).hasSize(3);
        assertThat(firstPage.totalElements()).isEqualTo(5);

        Page<Room> secondPage = adapter.findAll(3, 3);
        assertThat(secondPage.content()).hasSize(2);
        assertThat(secondPage.totalElements()).isEqualTo(5);
    }

    @Test
    void shouldCountRooms() {
        assertThat(adapter.count()).isZero();

        for (int i = 0; i < 4; i++) {
            adapter.save(buildRoom(UUID.randomUUID(), "Room " + i, "CNT0" + i));
        }

        assertThat(adapter.count()).isEqualTo(4);
    }

    @Test
    void shouldUpdateRoom() {
        UUID id = UUID.randomUUID();
        Room room = buildRoom(id, "Original Name", "UPD001");
        adapter.save(room);

        Room toUpdate = adapter.findById(id).orElseThrow();
        toUpdate.update("Updated Name", "Updated description", 100);
        adapter.save(toUpdate);

        Room updated = adapter.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getMaxParticipants()).isEqualTo(100);
    }

    @Test
    void shouldSaveRoomWithArchivedStatus() {
        UUID id = UUID.randomUUID();
        Room room = buildRoom(id, "Archived Room", "ARC001");
        room.archive();
        adapter.save(room);

        Room found = adapter.findById(id).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(RoomStatus.ARCHIVED);
    }

    @Test
    void shouldReturnEmptyPageForNoParticipantRooms() {
        Page<Room> page = adapter.findByParticipantUserId(UUID.randomUUID(), 0, 10);
        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }
}
