package com.planningpoker.estimation.infrastructure.persistence;

import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.Vote;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
@Import({VotePersistenceAdapter.class, StoryPersistenceAdapter.class})
class VotePersistenceAdapterIntegrationTest {

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
    private VotePersistenceAdapter voteAdapter;

    @Autowired
    private StoryPersistenceAdapter storyAdapter;

    @Autowired
    private VoteJpaRepository voteJpaRepository;

    @Autowired
    private StoryJpaRepository storyJpaRepository;

    private static final UUID ROOM_ID = UUID.randomUUID();

    @BeforeEach
    void cleanUp() {
        voteJpaRepository.deleteAll();
        voteJpaRepository.flush();
        storyJpaRepository.deleteAll();
        storyJpaRepository.flush();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Story createAndSaveStory(String title, int sortOrder) {
        Instant now = Instant.now();
        Story story = new Story(
                UUID.randomUUID(),
                ROOM_ID,
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
        return storyAdapter.save(story);
    }

    private Vote buildVote(UUID storyId, UUID userId, String value, BigDecimal numericValue) {
        Instant now = Instant.now();
        return new Vote(
                UUID.randomUUID(),
                storyId,
                userId,
                value,
                numericValue,
                true,
                now,
                now
        );
    }

    private Vote buildVote(UUID storyId, String value, BigDecimal numericValue) {
        return buildVote(storyId, UUID.randomUUID(), value, numericValue);
    }

    // ── Tests ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist a vote linked to an existing story")
        void shouldSaveVote() {
            Story story = createAndSaveStory("Estimation story", 1);
            Vote vote = buildVote(story.getId(), "8", new BigDecimal("8"));

            Vote saved = voteAdapter.save(vote);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo(vote.getId());
            assertThat(saved.getStoryId()).isEqualTo(story.getId());
            assertThat(saved.getValue()).isEqualTo("8");
            assertThat(saved.getNumericValue()).isEqualByComparingTo(new BigDecimal("8"));
            assertThat(saved.isAnonymous()).isTrue();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when story does not exist")
        void shouldThrowWhenStoryNotFound() {
            UUID nonExistentStoryId = UUID.randomUUID();
            Vote vote = buildVote(nonExistentStoryId, "5", new BigDecimal("5"));

            assertThatThrownBy(() -> voteAdapter.save(vote))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Story not found");
        }

        @Test
        @DisplayName("should persist vote with null numericValue (non-numeric vote)")
        void shouldSaveNonNumericVote() {
            Story story = createAndSaveStory("Non-numeric story", 1);
            Vote vote = buildVote(story.getId(), "?", null);

            Vote saved = voteAdapter.save(vote);

            assertThat(saved.getValue()).isEqualTo("?");
            assertThat(saved.getNumericValue()).isNull();
        }

        @Test
        @DisplayName("should persist vote with anonymous flag set to false")
        void shouldSaveNonAnonymousVote() {
            Story story = createAndSaveStory("Public vote story", 1);
            Instant now = Instant.now();
            Vote vote = new Vote(
                    UUID.randomUUID(),
                    story.getId(),
                    UUID.randomUUID(),
                    "13",
                    new BigDecimal("13"),
                    false,
                    now,
                    now
            );

            Vote saved = voteAdapter.save(vote);

            assertThat(saved.isAnonymous()).isFalse();
        }
    }

    @Nested
    @DisplayName("findByStoryId")
    class FindByStoryId {

        @Test
        @DisplayName("should return all votes for a given story")
        void shouldReturnAllVotesForStory() {
            Story story = createAndSaveStory("Multi-vote story", 1);
            voteAdapter.save(buildVote(story.getId(), "3", new BigDecimal("3")));
            voteAdapter.save(buildVote(story.getId(), "5", new BigDecimal("5")));
            voteAdapter.save(buildVote(story.getId(), "8", new BigDecimal("8")));

            List<Vote> votes = voteAdapter.findByStoryId(story.getId());

            assertThat(votes).hasSize(3);
            assertThat(votes).extracting(Vote::getStoryId)
                    .containsOnly(story.getId());
        }

        @Test
        @DisplayName("should return empty list when story has no votes")
        void shouldReturnEmptyForStoryWithNoVotes() {
            Story story = createAndSaveStory("Empty story", 1);

            List<Vote> votes = voteAdapter.findByStoryId(story.getId());

            assertThat(votes).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for non-existent story id")
        void shouldReturnEmptyForNonExistentStory() {
            List<Vote> votes = voteAdapter.findByStoryId(UUID.randomUUID());

            assertThat(votes).isEmpty();
        }

        @Test
        @DisplayName("should not return votes from a different story")
        void shouldIsolateByStory() {
            Story story1 = createAndSaveStory("Story A", 1);
            Story story2 = createAndSaveStory("Story B", 2);
            voteAdapter.save(buildVote(story1.getId(), "5", new BigDecimal("5")));
            voteAdapter.save(buildVote(story2.getId(), "8", new BigDecimal("8")));

            List<Vote> votesForStory1 = voteAdapter.findByStoryId(story1.getId());

            assertThat(votesForStory1).hasSize(1);
            assertThat(votesForStory1.get(0).getValue()).isEqualTo("5");
        }
    }

    @Nested
    @DisplayName("findByStoryIdAndUserId")
    class FindByStoryIdAndUserId {

        @Test
        @DisplayName("should return the vote for a specific user on a specific story")
        void shouldFindVoteByStoryAndUser() {
            Story story = createAndSaveStory("User vote story", 1);
            UUID userId = UUID.randomUUID();
            voteAdapter.save(buildVote(story.getId(), userId, "13", new BigDecimal("13")));

            Optional<Vote> found = voteAdapter.findByStoryIdAndUserId(story.getId(), userId);

            assertThat(found).isPresent();
            assertThat(found.get().getUserId()).isEqualTo(userId);
            assertThat(found.get().getValue()).isEqualTo("13");
        }

        @Test
        @DisplayName("should return empty when user has not voted on the story")
        void shouldReturnEmptyWhenUserHasNotVoted() {
            Story story = createAndSaveStory("No vote story", 1);
            voteAdapter.save(buildVote(story.getId(), "5", new BigDecimal("5")));

            Optional<Vote> found = voteAdapter.findByStoryIdAndUserId(story.getId(), UUID.randomUUID());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should return empty for non-existent story")
        void shouldReturnEmptyForNonExistentStory() {
            Optional<Vote> found = voteAdapter.findByStoryIdAndUserId(UUID.randomUUID(), UUID.randomUUID());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should distinguish between different users on same story")
        void shouldDistinguishBetweenUsers() {
            Story story = createAndSaveStory("Multi user story", 1);
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            voteAdapter.save(buildVote(story.getId(), user1, "3", new BigDecimal("3")));
            voteAdapter.save(buildVote(story.getId(), user2, "8", new BigDecimal("8")));

            Optional<Vote> vote1 = voteAdapter.findByStoryIdAndUserId(story.getId(), user1);
            Optional<Vote> vote2 = voteAdapter.findByStoryIdAndUserId(story.getId(), user2);

            assertThat(vote1).isPresent();
            assertThat(vote1.get().getValue()).isEqualTo("3");
            assertThat(vote2).isPresent();
            assertThat(vote2.get().getValue()).isEqualTo("8");
        }
    }

    @Nested
    @DisplayName("deleteByStoryId")
    class DeleteByStoryId {

        @Test
        @DisplayName("should delete all votes for a given story")
        void shouldDeleteAllVotesForStory() {
            Story story = createAndSaveStory("Delete votes story", 1);
            voteAdapter.save(buildVote(story.getId(), "3", new BigDecimal("3")));
            voteAdapter.save(buildVote(story.getId(), "5", new BigDecimal("5")));
            voteAdapter.save(buildVote(story.getId(), "8", new BigDecimal("8")));

            voteAdapter.deleteByStoryId(story.getId());
            voteJpaRepository.flush();

            List<Vote> remaining = voteAdapter.findByStoryId(story.getId());
            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("should not affect votes from other stories")
        void shouldNotAffectOtherStories() {
            Story story1 = createAndSaveStory("Story to purge", 1);
            Story story2 = createAndSaveStory("Story to keep", 2);
            voteAdapter.save(buildVote(story1.getId(), "3", new BigDecimal("3")));
            voteAdapter.save(buildVote(story2.getId(), "8", new BigDecimal("8")));

            voteAdapter.deleteByStoryId(story1.getId());
            voteJpaRepository.flush();

            assertThat(voteAdapter.findByStoryId(story1.getId())).isEmpty();
            assertThat(voteAdapter.findByStoryId(story2.getId())).hasSize(1);
        }

        @Test
        @DisplayName("should not throw when story has no votes")
        void shouldNotThrowWhenNoVotes() {
            Story story = createAndSaveStory("Empty story", 1);

            voteAdapter.deleteByStoryId(story.getId());
            // no exception expected
        }

        @Test
        @DisplayName("should not throw when story id does not exist")
        void shouldNotThrowForNonExistentStory() {
            voteAdapter.deleteByStoryId(UUID.randomUUID());
            // no exception expected
        }
    }
}
