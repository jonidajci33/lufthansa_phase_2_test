package com.planningpoker.audit.infrastructure.persistence;

import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.AuditOperation;
import com.planningpoker.audit.domain.Page;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditPersistenceAdapter.class)
class AuditPersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("audit_test_db")
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
        registry.add("spring.flyway.schemas", () -> "audit");
        registry.add("spring.flyway.create-schemas", () -> "true");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "audit");
    }

    @Autowired
    private AuditPersistenceAdapter adapter;

    @Autowired
    private AuditEntryJpaRepository jpaRepository;

    @BeforeEach
    void cleanUp() {
        jpaRepository.deleteAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static AuditEntry buildEntry(String entityType, UUID entityId, AuditOperation operation,
                                          UUID userId, String sourceService) {
        return AuditEntry.create(entityType, entityId, operation, userId, sourceService,
                Instant.now(), null, null, null, null);
    }

    private static AuditEntry buildEntryWithStates(String entityType, UUID entityId, AuditOperation operation,
                                                     UUID userId, String sourceService,
                                                     String previousState, String newState) {
        return AuditEntry.create(entityType, entityId, operation, userId, sourceService,
                Instant.now(), previousState, newState, null, null);
    }

    private static AuditEntry buildEntryWithEventId(String entityType, UUID entityId, AuditOperation operation,
                                                      UUID userId, String sourceService, String eventId) {
        return AuditEntry.create(entityType, entityId, operation, userId, sourceService,
                Instant.now(), null, null, null, eventId);
    }

    private static AuditEntry buildEntryWithTimestamp(String entityType, UUID entityId, AuditOperation operation,
                                                       UUID userId, String sourceService, Instant timestamp) {
        return AuditEntry.create(entityType, entityId, operation, userId, sourceService,
                timestamp, null, null, null, null);
    }

    // ── save + findById ─────────────────────────────────────────────

    @Test
    void shouldSaveAndFindById() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditEntry entry = buildEntry("Room", entityId, AuditOperation.CREATED, userId, "room-service");

        AuditEntry saved = adapter.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEntityType()).isEqualTo("Room");
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        assertThat(saved.getOperation()).isEqualTo(AuditOperation.CREATED);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getSourceService()).isEqualTo("room-service");
        assertThat(saved.getTimestamp()).isNotNull();

        Optional<AuditEntry> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEntityType()).isEqualTo("Room");
        assertThat(found.get().getEntityId()).isEqualTo(entityId);
    }

    @Test
    void shouldSaveEntryWithStates() {
        UUID entityId = UUID.randomUUID();
        String previousState = "{\"name\": \"Old Room\"}";
        String newState = "{\"name\": \"New Room\"}";
        AuditEntry entry = buildEntryWithStates("Room", entityId, AuditOperation.UPDATED,
                UUID.randomUUID(), "room-service", previousState, newState);

        AuditEntry saved = adapter.save(entry);

        Optional<AuditEntry> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPreviousState()).isEqualTo(previousState);
        assertThat(found.get().getNewState()).isEqualTo(newState);
    }

    @Test
    void shouldReturnEmptyForNonExistentEntry() {
        Optional<AuditEntry> found = adapter.findById(999999L);
        assertThat(found).isEmpty();
    }

    // ── findAll with filters ────────────────────────────────────────

    @Test
    void shouldFindAllWithNoFilters() {
        UUID userId = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));
        adapter.save(buildEntry("User", UUID.randomUUID(), AuditOperation.UPDATED, userId, "identity-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.DELETED, userId, "room-service"));

        Page<AuditEntry> page = adapter.findAll(AuditFilter.empty(), 0, 10);

        assertThat(page.content()).hasSize(3);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void shouldFindAllWithPagination() {
        UUID userId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));
        }

        Page<AuditEntry> firstPage = adapter.findAll(AuditFilter.empty(), 0, 3);
        assertThat(firstPage.content()).hasSize(3);
        assertThat(firstPage.totalElements()).isEqualTo(5);

        Page<AuditEntry> secondPage = adapter.findAll(AuditFilter.empty(), 3, 3);
        assertThat(secondPage.content()).hasSize(2);
        assertThat(secondPage.totalElements()).isEqualTo(5);
    }

    @Test
    void shouldFilterByEntityType() {
        UUID userId = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));
        adapter.save(buildEntry("User", UUID.randomUUID(), AuditOperation.CREATED, userId, "identity-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.UPDATED, userId, "room-service"));

        AuditFilter filter = new AuditFilter("Room", null, null, null, null);
        Page<AuditEntry> page = adapter.findAll(filter, 0, 10);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).allSatisfy(e -> assertThat(e.getEntityType()).isEqualTo("Room"));
    }

    @Test
    void shouldFilterByOperation() {
        UUID userId = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.UPDATED, userId, "room-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.DELETED, userId, "room-service"));

        AuditFilter filter = new AuditFilter(null, AuditOperation.CREATED, null, null, null);
        Page<AuditEntry> page = adapter.findAll(filter, 0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().getOperation()).isEqualTo(AuditOperation.CREATED);
    }

    @Test
    void shouldFilterByUserId() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, user1, "room-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, user2, "room-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.UPDATED, user1, "room-service"));

        AuditFilter filter = new AuditFilter(null, null, user1, null, null);
        Page<AuditEntry> page = adapter.findAll(filter, 0, 10);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).allSatisfy(e -> assertThat(e.getUserId()).isEqualTo(user1));
    }

    @Test
    void shouldFilterByTimeRange() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);

        adapter.save(buildEntryWithTimestamp("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service", threeDaysAgo));
        adapter.save(buildEntryWithTimestamp("Room", UUID.randomUUID(), AuditOperation.UPDATED, userId, "room-service", yesterday));
        adapter.save(buildEntryWithTimestamp("Room", UUID.randomUUID(), AuditOperation.DELETED, userId, "room-service", now));

        AuditFilter filter = new AuditFilter(null, null, null, twoDaysAgo, now);
        Page<AuditEntry> page = adapter.findAll(filter, 0, 10);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
    }

    @Test
    void shouldCombineMultipleFilters() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, user1, "room-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.UPDATED, user1, "room-service"));
        adapter.save(buildEntry("User", UUID.randomUUID(), AuditOperation.CREATED, user1, "identity-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, user2, "room-service"));

        AuditFilter filter = new AuditFilter("Room", AuditOperation.CREATED, user1, null, null);
        Page<AuditEntry> page = adapter.findAll(filter, 0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().getEntityType()).isEqualTo("Room");
        assertThat(page.content().getFirst().getOperation()).isEqualTo(AuditOperation.CREATED);
        assertThat(page.content().getFirst().getUserId()).isEqualTo(user1);
    }

    // ── findByEntityTypeAndEntityId ─────────────────────────────────

    @Test
    void shouldFindByEntityTypeAndEntityId() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        adapter.save(buildEntry("Room", entityId, AuditOperation.CREATED, userId, "room-service"));
        adapter.save(buildEntry("Room", entityId, AuditOperation.UPDATED, userId, "room-service"));
        // Different entity
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));

        List<AuditEntry> results = adapter.findByEntityTypeAndEntityId("Room", entityId);

        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(e -> {
            assertThat(e.getEntityType()).isEqualTo("Room");
            assertThat(e.getEntityId()).isEqualTo(entityId);
        });
    }

    @Test
    void shouldReturnEmptyListForNonExistentEntityTypeAndId() {
        List<AuditEntry> results = adapter.findByEntityTypeAndEntityId("Room", UUID.randomUUID());
        assertThat(results).isEmpty();
    }

    // ── count ───────────────────────────────────────────────────────

    @Test
    void shouldCountWithNoFilters() {
        UUID userId = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));
        adapter.save(buildEntry("User", UUID.randomUUID(), AuditOperation.UPDATED, userId, "identity-service"));

        long count = adapter.count(AuditFilter.empty());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCountWithFilters() {
        UUID userId = UUID.randomUUID();
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.CREATED, userId, "room-service"));
        adapter.save(buildEntry("Room", UUID.randomUUID(), AuditOperation.UPDATED, userId, "room-service"));
        adapter.save(buildEntry("User", UUID.randomUUID(), AuditOperation.CREATED, userId, "identity-service"));

        AuditFilter filter = new AuditFilter("Room", null, null, null, null);
        long count = adapter.count(filter);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroCountWhenEmpty() {
        long count = adapter.count(AuditFilter.empty());
        assertThat(count).isZero();
    }

    // ── existsByEventId ─────────────────────────────────────────────

    @Test
    void shouldReturnFalseForNonExistentEventId() {
        boolean exists = adapter.existsByEventId("non-existent-event");
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueForExistingEventId() {
        String eventId = "event-" + UUID.randomUUID();
        adapter.save(buildEntryWithEventId("Room", UUID.randomUUID(), AuditOperation.CREATED,
                UUID.randomUUID(), "room-service", eventId));

        boolean exists = adapter.existsByEventId(eventId);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldDistinguishBetweenDifferentEventIds() {
        String eventId1 = "event-1-" + UUID.randomUUID();
        String eventId2 = "event-2-" + UUID.randomUUID();

        adapter.save(buildEntryWithEventId("Room", UUID.randomUUID(), AuditOperation.CREATED,
                UUID.randomUUID(), "room-service", eventId1));

        assertThat(adapter.existsByEventId(eventId1)).isTrue();
        assertThat(adapter.existsByEventId(eventId2)).isFalse();
    }

    @Test
    void shouldSaveEntryWithNullableFields() {
        UUID entityId = UUID.randomUUID();
        AuditEntry entry = AuditEntry.create("Room", entityId, AuditOperation.CREATED,
                null, "room-service", Instant.now(), null, null, null, null);

        AuditEntry saved = adapter.save(entry);

        Optional<AuditEntry> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isNull();
        assertThat(found.get().getPreviousState()).isNull();
        assertThat(found.get().getNewState()).isNull();
        assertThat(found.get().getCorrelationId()).isNull();
        assertThat(found.get().getEventId()).isNull();
    }

    @Test
    void shouldSaveEntryWithCorrelationId() {
        UUID entityId = UUID.randomUUID();
        String correlationId = "corr-" + UUID.randomUUID();
        AuditEntry entry = AuditEntry.create("Room", entityId, AuditOperation.CREATED,
                UUID.randomUUID(), "room-service", Instant.now(), null, null, correlationId, null);

        AuditEntry saved = adapter.save(entry);

        Optional<AuditEntry> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCorrelationId()).isEqualTo(correlationId);
    }
}
