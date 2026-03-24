package com.planningpoker.identity.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void shouldContainParticipantRole() {
        assertThat(UserRole.valueOf("PARTICIPANT")).isEqualTo(UserRole.PARTICIPANT);
    }

    @Test
    void shouldContainAdminRole() {
        assertThat(UserRole.valueOf("ADMIN")).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldHaveExactlyTwoRoles() {
        assertThat(UserRole.values()).hasSize(2);
    }

    @Test
    void shouldReturnCorrectNameForParticipant() {
        assertThat(UserRole.PARTICIPANT.name()).isEqualTo("PARTICIPANT");
    }

    @Test
    void shouldReturnCorrectNameForAdmin() {
        assertThat(UserRole.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    void shouldMaintainOrdinalOrder() {
        // PARTICIPANT is declared first, ADMIN second
        assertThat(UserRole.PARTICIPANT.ordinal()).isLessThan(UserRole.ADMIN.ordinal());
    }
}
