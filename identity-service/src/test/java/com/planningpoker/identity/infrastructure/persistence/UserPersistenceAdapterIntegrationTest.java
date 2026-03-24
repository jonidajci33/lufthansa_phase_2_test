package com.planningpoker.identity.infrastructure.persistence;

import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
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
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserPersistenceAdapter.class)
class UserPersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("identity_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.schemas", () -> "identity");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "identity");
    }

    @Autowired
    private UserPersistenceAdapter adapter;

    @Autowired
    private UserJpaRepository jpaRepository;

    @BeforeEach
    void cleanUp() {
        jpaRepository.deleteAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static User buildUser(UUID id, String keycloakId, String username, String email) {
        return new User(
                id,
                keycloakId,
                username,
                email,
                username,
                "TestLastName",
                "Display " + username,
                null,
                true,
                EnumSet.of(UserRole.PARTICIPANT),
                Instant.now(),
                Instant.now()
        );
    }

    // ── Tests ────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndFindUserById() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "kc-100", "alice", "alice@example.com");

        User saved = adapter.save(user);

        assertThat(saved.getId()).isEqualTo(id);

        Optional<User> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("alice");
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getRoles()).contains(UserRole.PARTICIPANT);
    }

    @Test
    void shouldFindByKeycloakId() {
        UUID id = UUID.randomUUID();
        adapter.save(buildUser(id, "kc-200", "bob", "bob@example.com"));

        Optional<User> found = adapter.findByKeycloakId("kc-200");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob");
    }

    @Test
    void shouldFindByUsername() {
        UUID id = UUID.randomUUID();
        adapter.save(buildUser(id, "kc-300", "charlie", "charlie@example.com"));

        Optional<User> found = adapter.findByUsername("charlie");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("charlie@example.com");
    }

    @Test
    void shouldFindByEmail() {
        UUID id = UUID.randomUUID();
        adapter.save(buildUser(id, "kc-400", "diana", "diana@example.com"));

        Optional<User> found = adapter.findByEmail("diana@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("diana");
    }

    @Test
    void shouldReturnEmptyForNonExistentUser() {
        Optional<User> found = adapter.findById(UUID.randomUUID());
        assertThat(found).isEmpty();

        Optional<User> byKc = adapter.findByKeycloakId("nonexistent");
        assertThat(byKc).isEmpty();

        Optional<User> byUsername = adapter.findByUsername("ghost");
        assertThat(byUsername).isEmpty();

        Optional<User> byEmail = adapter.findByEmail("ghost@example.com");
        assertThat(byEmail).isEmpty();
    }

    @Test
    void shouldCheckExistsByUsername() {
        UUID id = UUID.randomUUID();
        adapter.save(buildUser(id, "kc-500", "eve", "eve@example.com"));

        assertThat(adapter.existsByUsername("eve")).isTrue();
        assertThat(adapter.existsByUsername("unknown")).isFalse();
    }

    @Test
    void shouldCheckExistsByEmail() {
        UUID id = UUID.randomUUID();
        adapter.save(buildUser(id, "kc-600", "frank", "frank@example.com"));

        assertThat(adapter.existsByEmail("frank@example.com")).isTrue();
        assertThat(adapter.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void shouldListUsersWithPagination() {
        for (int i = 0; i < 5; i++) {
            adapter.save(buildUser(UUID.randomUUID(), "kc-pg-" + i, "page-user-" + i, "page" + i + "@example.com"));
        }

        Page<User> firstPage = adapter.findAll(0, 3);
        assertThat(firstPage.content()).hasSize(3);
        assertThat(firstPage.totalElements()).isEqualTo(5);

        Page<User> secondPage = adapter.findAll(3, 3);
        assertThat(secondPage.content()).hasSize(2);
        assertThat(secondPage.totalElements()).isEqualTo(5);
    }

    @Test
    void shouldUpdateUser() {
        UUID id = UUID.randomUUID();
        User original = buildUser(id, "kc-700", "grace", "grace@example.com");
        adapter.save(original);

        User toUpdate = adapter.findById(id).orElseThrow();
        toUpdate.updateProfile("Grace Updated", "https://avatar.example.com/grace.png");
        adapter.save(toUpdate);

        User updated = adapter.findById(id).orElseThrow();
        assertThat(updated.getDisplayName()).isEqualTo("Grace Updated");
        assertThat(updated.getAvatarUrl()).isEqualTo("https://avatar.example.com/grace.png");
    }
}
