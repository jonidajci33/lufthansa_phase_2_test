package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.InvitationStatus;
import com.planningpoker.room.domain.InvitationType;
import com.planningpoker.room.domain.Room;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InvitationPersistenceAdapter.class, RoomPersistenceAdapter.class})
class InvitationPersistenceAdapterIntegrationTest {

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
    private InvitationPersistenceAdapter invitationAdapter;

    @Autowired
    private RoomPersistenceAdapter roomAdapter;

    @Autowired
    private InvitationJpaRepository invitationJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    private UUID persistedRoomId;
    private UUID moderatorId;

    @BeforeEach
    void cleanUp() {
        invitationJpaRepository.deleteAll();
        roomJpaRepository.deleteAll();

        // Create a room that invitations can reference (FK constraint)
        persistedRoomId = UUID.randomUUID();
        moderatorId = UUID.randomUUID();
        Room room = new Room(
                persistedRoomId,
                "Test Room",
                "Room for invitation tests",
                moderatorId,
                null,
                "INV" + persistedRoomId.toString().substring(0, 4),
                RoomStatus.ACTIVE,
                50,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
        roomAdapter.save(room);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private Invitation buildEmailInvitation(UUID id, String email, String token) {
        return new Invitation(
                id,
                persistedRoomId,
                moderatorId,
                email,
                token,
                InvitationType.EMAIL,
                InvitationStatus.PENDING,
                Instant.now().plus(7, ChronoUnit.DAYS),
                null,
                Instant.now()
        );
    }

    private Invitation buildLinkInvitation(UUID id, String token) {
        return new Invitation(
                id,
                persistedRoomId,
                moderatorId,
                null,
                token,
                InvitationType.LINK,
                InvitationStatus.PENDING,
                Instant.now().plus(24, ChronoUnit.HOURS),
                null,
                Instant.now()
        );
    }

    // ── Tests ────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndFindInvitationById() {
        UUID id = UUID.randomUUID();
        Invitation invitation = buildEmailInvitation(id, "alice@example.com", "tok-001");

        Invitation saved = invitationAdapter.save(invitation);

        assertThat(saved.getId()).isEqualTo(id);

        Optional<Invitation> found = invitationAdapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getToken()).isEqualTo("tok-001");
        assertThat(found.get().getType()).isEqualTo(InvitationType.EMAIL);
        assertThat(found.get().getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(found.get().getRoomId()).isEqualTo(persistedRoomId);
        assertThat(found.get().getInvitedBy()).isEqualTo(moderatorId);
    }

    @Test
    void shouldFindByToken() {
        UUID id = UUID.randomUUID();
        invitationAdapter.save(buildEmailInvitation(id, "bob@example.com", "unique-token-abc"));

        Optional<Invitation> found = invitationAdapter.findByToken("unique-token-abc");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void shouldFindByRoomId() {
        invitationAdapter.save(buildEmailInvitation(UUID.randomUUID(), "c1@example.com", "tok-r1"));
        invitationAdapter.save(buildEmailInvitation(UUID.randomUUID(), "c2@example.com", "tok-r2"));
        invitationAdapter.save(buildLinkInvitation(UUID.randomUUID(), "tok-r3"));

        List<Invitation> found = invitationAdapter.findByRoomId(persistedRoomId);

        assertThat(found).hasSize(3);
    }

    @Test
    void shouldFindPendingByEmailAndRoomId() {
        String email = "pending@example.com";
        invitationAdapter.save(buildEmailInvitation(UUID.randomUUID(), email, "tok-pending"));

        Optional<Invitation> found = invitationAdapter.findPendingByEmailAndRoomId(email, persistedRoomId);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void shouldNotFindAcceptedInvitationAsPending() {
        String email = "accepted@example.com";
        UUID id = UUID.randomUUID();
        Invitation invitation = new Invitation(
                id,
                persistedRoomId,
                moderatorId,
                email,
                "tok-accepted",
                InvitationType.EMAIL,
                InvitationStatus.ACCEPTED,
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now(),
                Instant.now()
        );
        invitationAdapter.save(invitation);

        Optional<Invitation> found = invitationAdapter.findPendingByEmailAndRoomId(email, persistedRoomId);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonExistentInvitation() {
        Optional<Invitation> byId = invitationAdapter.findById(UUID.randomUUID());
        assertThat(byId).isEmpty();

        Optional<Invitation> byToken = invitationAdapter.findByToken("nonexistent-token");
        assertThat(byToken).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForRoomWithNoInvitations() {
        // Create a second room with no invitations
        UUID otherRoomId = UUID.randomUUID();
        Room otherRoom = new Room(
                otherRoomId,
                "Other Room",
                "No invitations here",
                UUID.randomUUID(),
                null,
                "OTR" + otherRoomId.toString().substring(0, 4),
                RoomStatus.ACTIVE,
                50,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
        roomAdapter.save(otherRoom);

        List<Invitation> found = invitationAdapter.findByRoomId(otherRoomId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveLinkInvitationWithoutEmail() {
        UUID id = UUID.randomUUID();
        Invitation linkInvitation = buildLinkInvitation(id, "link-tok-001");

        Invitation saved = invitationAdapter.save(linkInvitation);

        assertThat(saved.getEmail()).isNull();
        assertThat(saved.getType()).isEqualTo(InvitationType.LINK);

        Optional<Invitation> found = invitationAdapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isNull();
        assertThat(found.get().getType()).isEqualTo(InvitationType.LINK);
    }

    @Test
    void shouldReturnEmptyForPendingSearchWithWrongEmail() {
        invitationAdapter.save(buildEmailInvitation(UUID.randomUUID(), "real@example.com", "tok-real"));

        Optional<Invitation> found = invitationAdapter.findPendingByEmailAndRoomId(
                "wrong@example.com", persistedRoomId);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyForPendingSearchWithWrongRoomId() {
        String email = "roomcheck@example.com";
        invitationAdapter.save(buildEmailInvitation(UUID.randomUUID(), email, "tok-roomcheck"));

        Optional<Invitation> found = invitationAdapter.findPendingByEmailAndRoomId(
                email, UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldPersistExpiresAt() {
        UUID id = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(48, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MICROS);
        Invitation invitation = new Invitation(
                id,
                persistedRoomId,
                moderatorId,
                "expiry@example.com",
                "tok-expiry",
                InvitationType.EMAIL,
                InvitationStatus.PENDING,
                expiresAt,
                null,
                Instant.now()
        );
        invitationAdapter.save(invitation);

        Optional<Invitation> found = invitationAdapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getExpiresAt()).isNotNull();
    }
}
