package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.domain.InvitationStatus;
import com.planningpoker.room.domain.InvitationType;
import com.planningpoker.room.web.dto.InvitationResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvitationRestMapperTest {

    @Test
    void toResponse_shouldReturnNull_whenDomainIsNull() {
        assertThat(InvitationRestMapper.toResponse(null)).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        Invitation domain = createDomain();

        InvitationResponse result = InvitationRestMapper.toResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(domain.getId());
        assertThat(result.roomId()).isEqualTo(domain.getRoomId());
        assertThat(result.email()).isEqualTo(domain.getEmail());
        assertThat(result.token()).isEqualTo(domain.getToken());
        assertThat(result.type()).isEqualTo(domain.getType());
        assertThat(result.status()).isEqualTo(domain.getStatus());
        assertThat(result.expiresAt()).isEqualTo(domain.getExpiresAt());
    }

    @Test
    void toResponseList_shouldReturnEmptyList_whenNull() {
        assertThat(InvitationRestMapper.toResponseList(null)).isEmpty();
    }

    @Test
    void toResponseList_shouldMapAllElements() {
        List<Invitation> domains = List.of(createDomain(), createDomain());

        List<InvitationResponse> result = InvitationRestMapper.toResponseList(domains);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private static Invitation createDomain() {
        Instant now = Instant.now();
        return new Invitation(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "user@example.com", "invite-token-123",
                InvitationType.EMAIL, InvitationStatus.PENDING,
                now.plusSeconds(86400), null, now
        );
    }
}
