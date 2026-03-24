package com.planningpoker.identity.web.mapper;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.web.dto.InternalUserResponse;
import com.planningpoker.identity.web.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRestMapperTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String KEYCLOAK_ID = "22222222-2222-2222-2222-222222222222";
    private static final String USERNAME = "johndoe";
    private static final String EMAIL = "john@example.com";
    private static final String DISPLAY_NAME = "John Doe";
    private static final String AVATAR_URL = "https://example.com/avatar.png";
    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-03-01T14:30:00Z");

    @Test
    void toResponse_mapsAllFields() {
        User user = buildDomainUser();

        UserResponse response = UserRestMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(UUID.fromString(KEYCLOAK_ID));
        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.displayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.avatarUrl()).isEqualTo(AVATAR_URL);
        assertThat(response.isActive()).isTrue();
        assertThat(response.roles()).containsExactlyInAnyOrder("PARTICIPANT", "ADMIN");
        assertThat(response.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void toResponse_convertsEnumRolesToStrings() {
        User user = new User(
                ID, KEYCLOAK_ID, USERNAME, EMAIL, "John", "Doe", DISPLAY_NAME, AVATAR_URL,
                true, EnumSet.of(UserRole.ADMIN), CREATED_AT, UPDATED_AT
        );

        UserResponse response = UserRestMapper.toResponse(user);

        assertThat(response.roles()).containsExactly("ADMIN");
    }

    @Test
    void toResponse_nullInput_returnsNull() {
        assertThat(UserRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toInternalResponse_mapsAllFields() {
        User user = buildDomainUser();

        InternalUserResponse response = UserRestMapper.toInternalResponse(user);

        assertThat(response.id()).isEqualTo(ID);
        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.displayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toInternalResponse_nullInput_returnsNull() {
        assertThat(UserRestMapper.toInternalResponse(null)).isNull();
    }

    @Test
    void toResponseList_mapsMultipleUsers() {
        User user1 = buildDomainUser();
        User user2 = new User(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "33333333-3333-3333-3333-333333333333", "janedoe", "jane@example.com",
                "Jane", "Doe", "Jane Doe", null, true,
                EnumSet.of(UserRole.PARTICIPANT), CREATED_AT, UPDATED_AT
        );

        List<UserResponse> responses = UserRestMapper.toResponseList(List.of(user1, user2));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).username()).isEqualTo(USERNAME);
        assertThat(responses.get(1).username()).isEqualTo("janedoe");
    }

    @Test
    void toResponseList_emptyList_returnsEmpty() {
        assertThat(UserRestMapper.toResponseList(List.of())).isEmpty();
    }

    @Test
    void toResponseList_nullList_returnsEmpty() {
        assertThat(UserRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toInternalResponseList_mapsMultipleUsers() {
        User user1 = buildDomainUser();
        User user2 = new User(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "33333333-3333-3333-3333-333333333333", "janedoe", "jane@example.com",
                "Jane", "Doe", "Jane Doe", null, false,
                EnumSet.of(UserRole.PARTICIPANT), CREATED_AT, UPDATED_AT
        );

        List<InternalUserResponse> responses = UserRestMapper.toInternalResponseList(
                List.of(user1, user2));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).isActive()).isTrue();
        assertThat(responses.get(1).isActive()).isFalse();
    }

    @Test
    void toInternalResponseList_emptyList_returnsEmpty() {
        assertThat(UserRestMapper.toInternalResponseList(List.of())).isEmpty();
    }

    @Test
    void toInternalResponseList_nullList_returnsEmpty() {
        assertThat(UserRestMapper.toInternalResponseList(null)).isEmpty();
    }

    @Test
    void toResponse_emptyRoles_returnsEmptySet() {
        User user = new User(
                ID, KEYCLOAK_ID, USERNAME, EMAIL, "John", "Doe", DISPLAY_NAME, AVATAR_URL,
                true, EnumSet.noneOf(UserRole.class), CREATED_AT, UPDATED_AT
        );

        UserResponse response = UserRestMapper.toResponse(user);

        assertThat(response.roles()).isEmpty();
    }

    @Test
    void toResponse_nullOptionalFields_handlesGracefully() {
        User user = new User(
                ID, KEYCLOAK_ID, USERNAME, EMAIL,
                null, null, null, null, false,
                EnumSet.noneOf(UserRole.class), CREATED_AT, UPDATED_AT
        );

        UserResponse response = UserRestMapper.toResponse(user);

        assertThat(response.displayName()).isNull();
        assertThat(response.avatarUrl()).isNull();
        assertThat(response.isActive()).isFalse();
    }

    // ── Test data builder ──────────────────────────────────────────

    private User buildDomainUser() {
        return new User(
                ID,
                KEYCLOAK_ID,
                USERNAME,
                EMAIL,
                "John",
                "Doe",
                DISPLAY_NAME,
                AVATAR_URL,
                true,
                EnumSet.of(UserRole.PARTICIPANT, UserRole.ADMIN),
                CREATED_AT,
                UPDATED_AT
        );
    }
}
