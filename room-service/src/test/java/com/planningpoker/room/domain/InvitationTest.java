package com.planningpoker.room.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvitationTest {

    // ── Factory helpers ───────────────────────────────────────────────

    private static Invitation pendingInvitation() {
        Instant now = Instant.now();
        return new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "user@example.com",
                UUID.randomUUID().toString(),
                InvitationType.EMAIL,
                InvitationStatus.PENDING,
                now.plus(7, ChronoUnit.DAYS),
                null,
                now
        );
    }

    private static Invitation expiredInvitation() {
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        return new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "user@example.com",
                UUID.randomUUID().toString(),
                InvitationType.EMAIL,
                InvitationStatus.PENDING,
                past,
                null,
                past.minus(7, ChronoUnit.DAYS)
        );
    }

    // ── Constructor / defaults ────────────────────────────────────────

    @Test
    void shouldCreateInvitationWithDefaultPendingStatus() {
        Invitation invitation = new Invitation();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    // ── accept() ──────────────────────────────────────────────────────

    @Test
    void shouldAcceptPendingInvitation() {
        Invitation invitation = pendingInvitation();

        Instant beforeAccept = Instant.now();
        invitation.accept();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getAcceptedAt()).isAfterOrEqualTo(beforeAccept);
    }

    @Test
    void shouldThrowWhenAcceptingAlreadyAcceptedInvitation() {
        Invitation invitation = pendingInvitation();
        invitation.accept();

        assertThatThrownBy(invitation::accept)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in PENDING status");
    }

    @Test
    void shouldThrowWhenAcceptingCancelledInvitation() {
        Invitation invitation = pendingInvitation();
        invitation.cancel();

        assertThatThrownBy(invitation::accept)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in PENDING status");
    }

    // ── isExpired() ───────────────────────────────────────────────────

    @Test
    void shouldReturnFalseWhenNotExpired() {
        Invitation invitation = pendingInvitation();
        assertThat(invitation.isExpired()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenExpired() {
        Invitation invitation = expiredInvitation();
        assertThat(invitation.isExpired()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenExpiresAtIsNull() {
        Invitation invitation = new Invitation();
        invitation.setExpiresAt(null);
        assertThat(invitation.isExpired()).isFalse();
    }

    // ── cancel() ──────────────────────────────────────────────────────

    @Test
    void shouldCancelPendingInvitation() {
        Invitation invitation = pendingInvitation();
        invitation.cancel();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
    }

    @Test
    void shouldBeIdempotentWhenCancellingAlreadyCancelled() {
        Invitation invitation = pendingInvitation();
        invitation.cancel();
        invitation.cancel();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
    }

    // ── equals / hashCode / toString ──────────────────────────────────

    @Test
    void shouldBeEqualWhenSameId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Invitation inv1 = new Invitation(id, UUID.randomUUID(), UUID.randomUUID(),
                "a@test.com", "token1", InvitationType.EMAIL,
                InvitationStatus.PENDING, now.plus(1, ChronoUnit.DAYS), null, now);
        Invitation inv2 = new Invitation(id, UUID.randomUUID(), UUID.randomUUID(),
                "b@test.com", "token2", InvitationType.LINK,
                InvitationStatus.ACCEPTED, now.plus(2, ChronoUnit.DAYS), now, now);

        assertThat(inv1).isEqualTo(inv2);
        assertThat(inv1.hashCode()).isEqualTo(inv2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Instant now = Instant.now();
        Invitation inv1 = new Invitation(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "a@test.com", "token", InvitationType.EMAIL,
                InvitationStatus.PENDING, now, null, now);
        Invitation inv2 = new Invitation(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "a@test.com", "token", InvitationType.EMAIL,
                InvitationStatus.PENDING, now, null, now);

        assertThat(inv1).isNotEqualTo(inv2);
    }

    @Test
    void shouldIncludeKeyFieldsInToString() {
        Invitation invitation = pendingInvitation();
        String str = invitation.toString();

        assertThat(str).contains("Invitation{");
        assertThat(str).contains("PENDING");
    }
}
