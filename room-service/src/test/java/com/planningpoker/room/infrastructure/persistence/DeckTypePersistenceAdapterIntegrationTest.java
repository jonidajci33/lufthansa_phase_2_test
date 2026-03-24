package com.planningpoker.room.infrastructure.persistence;

import com.planningpoker.room.domain.DeckCategory;
import com.planningpoker.room.domain.DeckType;
import com.planningpoker.room.domain.DeckValue;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DeckTypePersistenceAdapter.class)
class DeckTypePersistenceAdapterIntegrationTest {

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
    }

    @Autowired
    private DeckTypePersistenceAdapter adapter;

    @Autowired
    private DeckTypeJpaRepository jpaRepository;

    // Deterministic UUIDs from V2 seed migration
    private static final UUID SCRUM_DECK_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID FIBONACCI_DECK_ID = UUID.fromString("a0000000-0000-0000-0000-000000000002");
    private static final UUID SEQUENTIAL_DECK_ID = UUID.fromString("a0000000-0000-0000-0000-000000000003");
    private static final UUID TSHIRT_DECK_ID = UUID.fromString("a0000000-0000-0000-0000-000000000004");

    // ── Helpers ──────────────────────────────────────────────────────

    private static DeckType buildCustomDeckType(UUID id, String name, DeckCategory category) {
        List<DeckValue> values = List.of(
                new DeckValue(UUID.randomUUID(), "Small", new BigDecimal("1"), 1),
                new DeckValue(UUID.randomUUID(), "Medium", new BigDecimal("3"), 2),
                new DeckValue(UUID.randomUUID(), "Large", new BigDecimal("5"), 3)
        );
        return new DeckType(
                id,
                name,
                category,
                false,
                UUID.randomUUID(),
                values,
                Instant.now()
        );
    }

    // ── Tests relying on V2 seeded data ─────────────────────────────

    @Test
    void shouldFindSeededScrumDeckById() {
        Optional<DeckType> found = adapter.findById(SCRUM_DECK_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Scrum");
        assertThat(found.get().getCategory()).isEqualTo(DeckCategory.SCRUM);
        assertThat(found.get().isSystem()).isTrue();
        assertThat(found.get().getCreatedBy()).isNull();
        assertThat(found.get().getValues()).hasSize(13);
    }

    @Test
    void shouldFindSeededFibonacciDeckById() {
        Optional<DeckType> found = adapter.findById(FIBONACCI_DECK_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Fibonacci");
        assertThat(found.get().getCategory()).isEqualTo(DeckCategory.FIBONACCI);
        assertThat(found.get().isSystem()).isTrue();
        assertThat(found.get().getValues()).hasSize(11);
    }

    @Test
    void shouldFindAllSeededDeckTypes() {
        List<DeckType> all = adapter.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(4);
        assertThat(all).extracting(DeckType::getName)
                .contains("Scrum", "Fibonacci", "Sequential", "T-Shirt");
    }

    @Test
    void shouldFindByCategory() {
        List<DeckType> scrumDecks = adapter.findByCategory(DeckCategory.SCRUM);

        assertThat(scrumDecks).isNotEmpty();
        assertThat(scrumDecks).allSatisfy(deck ->
                assertThat(deck.getCategory()).isEqualTo(DeckCategory.SCRUM));
    }

    @Test
    void shouldFindByCategoryTShirt() {
        List<DeckType> tShirtDecks = adapter.findByCategory(DeckCategory.T_SHIRT);

        assertThat(tShirtDecks).hasSize(1);
        assertThat(tShirtDecks.get(0).getName()).isEqualTo("T-Shirt");
        assertThat(tShirtDecks.get(0).getValues()).hasSize(6);
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<DeckType> found = adapter.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForUnusedCategory() {
        List<DeckType> customDecks = adapter.findByCategory(DeckCategory.CUSTOM);
        assertThat(customDecks).isEmpty();
    }

    // ── Tests for saving custom deck types ──────────────────────────

    @Test
    void shouldSaveCustomDeckType() {
        UUID id = UUID.randomUUID();
        DeckType custom = buildCustomDeckType(id, "My Custom Deck", DeckCategory.CUSTOM);

        DeckType saved = adapter.save(custom);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getName()).isEqualTo("My Custom Deck");
        assertThat(saved.getCategory()).isEqualTo(DeckCategory.CUSTOM);
        assertThat(saved.isSystem()).isFalse();
        assertThat(saved.getValues()).hasSize(3);
    }

    @Test
    void shouldSaveAndRetrieveCustomDeckTypeWithValues() {
        UUID id = UUID.randomUUID();
        DeckType custom = buildCustomDeckType(id, "Story Points", DeckCategory.CUSTOM);

        adapter.save(custom);

        Optional<DeckType> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Story Points");
        assertThat(found.get().getValues()).hasSize(3);
        assertThat(found.get().getValues()).extracting(DeckValue::getLabel)
                .containsExactlyInAnyOrder("Small", "Medium", "Large");
    }

    @Test
    void shouldFindCustomDeckByCategory() {
        UUID id = UUID.randomUUID();
        DeckType custom = buildCustomDeckType(id, "Team Deck", DeckCategory.CUSTOM);
        adapter.save(custom);

        List<DeckType> customDecks = adapter.findByCategory(DeckCategory.CUSTOM);

        assertThat(customDecks).hasSize(1);
        assertThat(customDecks.get(0).getName()).isEqualTo("Team Deck");
    }

    @Test
    void shouldIncludeCustomDeckInFindAll() {
        UUID id = UUID.randomUUID();
        DeckType custom = buildCustomDeckType(id, "Extra Deck", DeckCategory.CUSTOM);
        adapter.save(custom);

        List<DeckType> all = adapter.findAll();

        // 4 seeded + 1 custom
        assertThat(all).hasSizeGreaterThanOrEqualTo(5);
        assertThat(all).extracting(DeckType::getName).contains("Extra Deck");
    }

    @Test
    void shouldPreserveDeckValueSortOrder() {
        Optional<DeckType> scrum = adapter.findById(SCRUM_DECK_ID);
        assertThat(scrum).isPresent();

        List<DeckValue> values = scrum.get().getValues();
        for (int i = 0; i < values.size() - 1; i++) {
            assertThat(values.get(i).getSortOrder())
                    .isLessThanOrEqualTo(values.get(i + 1).getSortOrder());
        }
    }
}
