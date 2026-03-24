package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
@Import(StoryPersistenceAdapter.class)
class StoryPersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withInitScript("init-schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.schemas", () -> "estimation");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "estimation");
    }

    @Autowired
    private StoryPersistenceAdapter adapter;

    @Autowired
    private StoryJpaRepository jpaRepository;

    private static final UUID ROOM_ID = UUID.randomUUID();

    @BeforeEach
    void cleanUp() {
        jpaRepository.deleteAll();
        jpaRepository.flush();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Story buildStory(UUID roomId, String title, int sortOrder) {
        Instant now = Instant.now();
        return new Story(
                UUID.randomUUID(),
                roomId,
                title,
                "Description for " + title,
                StoryStatus.PENDING,
                sortOrder,
                null,
                false,
                new ArrayList<>(),
                now,
                now
        );
    }

    private Story buildStory(String title, int sortOrder) {
        return buildStory(ROOM_ID, title, sortOrder);
    }

    // ── Tests ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save and findById")
    class SaveAndFindById {

        @Test
        @DisplayName("should persist a story and retrieve it by id")
        void shouldSaveAndFindById() {
            Story story = buildStory("Login feature", 1);

            Story saved = adapter.save(story);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo(story.getId());
            assertThat(saved.getTitle()).isEqualTo("Login feature");
            assertThat(saved.getStatus()).isEqualTo(StoryStatus.PENDING);
            assertThat(saved.getSortOrder()).isEqualTo(1);
            assertThat(saved.isConsensusReached()).isFalse();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();

            Optional<Story> found = adapter.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getTitle()).isEqualTo("Login feature");
            assertThat(found.get().getRoomId()).isEqualTo(ROOM_ID);
            assertThat(found.get().getDescription()).isEqualTo("Description for Login feature");
        }

        @Test
        @DisplayName("should return empty optional for non-existent id")
        void shouldReturnEmptyForNonExistentId() {
            Optional<Story> found = adapter.findById(UUID.randomUUID());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should persist story with null description and null finalScore")
        void shouldPersistStoryWithNullableFields() {
            Instant now = Instant.now();
            Story story = new Story(
                    UUID.randomUUID(),
                    ROOM_ID,
                    "Nullable fields story",
                    null,
                    StoryStatus.PENDING,
                    0,
                    null,
                    false,
                    new ArrayList<>(),
                    now,
                    now
            );

            Story saved = adapter.save(story);

            Optional<Story> found = adapter.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getDescription()).isNull();
            assertThat(found.get().getFinalScore()).isNull();
        }

        @Test
        @DisplayName("should persist story with finalScore and consensusReached")
        void shouldPersistVotedStory() {
            Instant now = Instant.now();
            Story story = new Story(
                    UUID.randomUUID(),
                    ROOM_ID,
                    "Voted story",
                    "Done voting",
                    StoryStatus.VOTED,
                    5,
                    new BigDecimal("8.50"),
                    true,
                    new ArrayList<>(),
                    now,
                    now
            );

            Story saved = adapter.save(story);

            Optional<Story> found = adapter.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(StoryStatus.VOTED);
            assertThat(found.get().getFinalScore()).isEqualByComparingTo(new BigDecimal("8.50"));
            assertThat(found.get().isConsensusReached()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByRoomId (paginated)")
    class FindByRoomId {

        @Test
        @DisplayName("should return paginated stories for a given room")
        void shouldReturnPaginatedStoriesForRoom() {
            for (int i = 1; i <= 5; i++) {
                adapter.save(buildStory("Story " + i, i));
            }

            Page<Story> page = adapter.findByRoomId(ROOM_ID, 0, 3);

            assertThat(page.content()).hasSize(3);
            assertThat(page.totalElements()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return second page correctly")
        void shouldReturnSecondPage() {
            for (int i = 1; i <= 5; i++) {
                adapter.save(buildStory("Story " + i, i));
            }

            Page<Story> page = adapter.findByRoomId(ROOM_ID, 3, 3);

            assertThat(page.content()).hasSize(2);
            assertThat(page.totalElements()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return empty page when no stories exist for room")
        void shouldReturnEmptyPageForUnknownRoom() {
            adapter.save(buildStory("Some story", 1));

            Page<Story> page = adapter.findByRoomId(UUID.randomUUID(), 0, 10);

            assertThat(page.content()).isEmpty();
            assertThat(page.totalElements()).isZero();
        }

        @Test
        @DisplayName("should not return stories from a different room")
        void shouldIsolateByRoom() {
            UUID otherRoom = UUID.randomUUID();
            adapter.save(buildStory(ROOM_ID, "Room A story", 1));
            adapter.save(buildStory(otherRoom, "Room B story", 1));

            Page<Story> page = adapter.findByRoomId(ROOM_ID, 0, 10);

            assertThat(page.content()).hasSize(1);
            assertThat(page.content().get(0).getTitle()).isEqualTo("Room A story");
            assertThat(page.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findByRoomIdOrderBySortOrder")
    class FindByRoomIdOrderBySortOrder {

        @Test
        @DisplayName("should return stories sorted by sortOrder ascending")
        void shouldReturnStoriesSortedBySortOrder() {
            adapter.save(buildStory("Third", 3));
            adapter.save(buildStory("First", 1));
            adapter.save(buildStory("Second", 2));

            List<Story> sorted = adapter.findByRoomIdOrderBySortOrder(ROOM_ID);

            assertThat(sorted).hasSize(3);
            assertThat(sorted.get(0).getTitle()).isEqualTo("First");
            assertThat(sorted.get(1).getTitle()).isEqualTo("Second");
            assertThat(sorted.get(2).getTitle()).isEqualTo("Third");
        }

        @Test
        @DisplayName("should return empty list when room has no stories")
        void shouldReturnEmptyListForEmptyRoom() {
            List<Story> sorted = adapter.findByRoomIdOrderBySortOrder(UUID.randomUUID());

            assertThat(sorted).isEmpty();
        }

        @Test
        @DisplayName("should only return stories for the specified room")
        void shouldFilterByRoom() {
            UUID otherRoom = UUID.randomUUID();
            adapter.save(buildStory(ROOM_ID, "Room A", 1));
            adapter.save(buildStory(otherRoom, "Room B", 2));

            List<Story> sorted = adapter.findByRoomIdOrderBySortOrder(ROOM_ID);

            assertThat(sorted).hasSize(1);
            assertThat(sorted.get(0).getTitle()).isEqualTo("Room A");
        }
    }

    @Nested
    @DisplayName("countByRoomId")
    class CountByRoomId {

        @Test
        @DisplayName("should return zero when room has no stories")
        void shouldReturnZeroForEmptyRoom() {
            long count = adapter.countByRoomId(UUID.randomUUID());

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("should return correct count of stories in a room")
        void shouldCountStoriesInRoom() {
            adapter.save(buildStory("Story 1", 1));
            adapter.save(buildStory("Story 2", 2));
            adapter.save(buildStory("Story 3", 3));

            long count = adapter.countByRoomId(ROOM_ID);

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("should not count stories from other rooms")
        void shouldNotCountOtherRooms() {
            UUID otherRoom = UUID.randomUUID();
            adapter.save(buildStory(ROOM_ID, "Room A story", 1));
            adapter.save(buildStory(otherRoom, "Room B story", 1));

            long count = adapter.countByRoomId(ROOM_ID);

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("should delete an existing story")
        void shouldDeleteExistingStory() {
            Story saved = adapter.save(buildStory("To be deleted", 1));

            adapter.deleteById(saved.getId());
            jpaRepository.flush();

            Optional<Story> found = adapter.findById(saved.getId());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should not throw when deleting non-existent id")
        void shouldNotThrowForNonExistentId() {
            adapter.deleteById(UUID.randomUUID());
            // no exception expected
        }

        @Test
        @DisplayName("should not affect other stories when deleting one")
        void shouldNotAffectOtherStories() {
            Story story1 = adapter.save(buildStory("Keep me", 1));
            Story story2 = adapter.save(buildStory("Delete me", 2));

            adapter.deleteById(story2.getId());
            jpaRepository.flush();

            assertThat(adapter.findById(story1.getId())).isPresent();
            assertThat(adapter.findById(story2.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll (paginated)")
    class FindAll {

        @Test
        @DisplayName("should return paginated list of all stories")
        void shouldReturnPaginatedAllStories() {
            UUID room1 = UUID.randomUUID();
            UUID room2 = UUID.randomUUID();
            adapter.save(buildStory(room1, "Room1 Story", 1));
            adapter.save(buildStory(room2, "Room2 Story", 1));
            adapter.save(buildStory(room1, "Room1 Story 2", 2));

            Page<Story> page = adapter.findAll(0, 2);

            assertThat(page.content()).hasSize(2);
            assertThat(page.totalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return empty page when no stories exist")
        void shouldReturnEmptyPageWhenEmpty() {
            Page<Story> page = adapter.findAll(0, 10);

            assertThat(page.content()).isEmpty();
            assertThat(page.totalElements()).isZero();
        }

        @Test
        @DisplayName("should return remaining items on last page")
        void shouldReturnRemainingOnLastPage() {
            for (int i = 1; i <= 7; i++) {
                adapter.save(buildStory(UUID.randomUUID(), "Story " + i, i));
            }

            Page<Story> page = adapter.findAll(5, 5);

            assertThat(page.content()).hasSize(2);
            assertThat(page.totalElements()).isEqualTo(7);
        }
    }
}
