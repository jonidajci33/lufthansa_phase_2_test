package com.planningpoker.notification.infrastructure.persistence;

import com.planningpoker.notification.domain.EmailLog;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailLogPersistenceMapperTest {

    private static final UUID LOG_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.now();
    private static final Instant SENT_AT = NOW.plusSeconds(5);

    // ── Helpers ──────────────────────────────────────────────────────

    private EmailLog sampleDomain() {
        return new EmailLog(
                LOG_ID,
                "user@example.com",
                "Welcome!",
                "welcome-email",
                "SENT",
                SENT_AT,
                null,
                NOW
        );
    }

    private EmailLogJpaEntity sampleEntity() {
        EmailLogJpaEntity entity = new EmailLogJpaEntity();
        entity.setId(LOG_ID);
        entity.setRecipient("user@example.com");
        entity.setSubject("Welcome!");
        entity.setTemplate("welcome-email");
        entity.setStatus("SENT");
        entity.setSentAt(SENT_AT);
        entity.setErrorMessage(null);
        entity.setCreatedAt(NOW);
        return entity;
    }

    // ═══════════════════════════════════════════════════════════════════
    // toDomain
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldMapEntityToDomain() {
        EmailLogJpaEntity entity = sampleEntity();

        EmailLog result = EmailLogPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(LOG_ID);
        assertThat(result.getRecipient()).isEqualTo("user@example.com");
        assertThat(result.getSubject()).isEqualTo("Welcome!");
        assertThat(result.getTemplate()).isEqualTo("welcome-email");
        assertThat(result.getStatus()).isEqualTo("SENT");
        assertThat(result.getSentAt()).isEqualTo(SENT_AT);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        EmailLog result = EmailLogPersistenceMapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldMapEntityWithErrorMessageToDomain() {
        EmailLogJpaEntity entity = sampleEntity();
        entity.setStatus("FAILED");
        entity.setErrorMessage("SMTP connection refused");
        entity.setSentAt(null);

        EmailLog result = EmailLogPersistenceMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getErrorMessage()).isEqualTo("SMTP connection refused");
        assertThat(result.getSentAt()).isNull();
    }

    // ═══════════════════════════════════════════════════════════════════
    // toEntity
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldMapDomainToEntity() {
        EmailLog emailLog = sampleDomain();

        EmailLogJpaEntity result = EmailLogPersistenceMapper.toEntity(emailLog);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(LOG_ID);
        assertThat(result.getRecipient()).isEqualTo("user@example.com");
        assertThat(result.getSubject()).isEqualTo("Welcome!");
        assertThat(result.getTemplate()).isEqualTo("welcome-email");
        assertThat(result.getStatus()).isEqualTo("SENT");
        assertThat(result.getSentAt()).isEqualTo(SENT_AT);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        EmailLogJpaEntity result = EmailLogPersistenceMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldMapDomainWithErrorToEntity() {
        EmailLog emailLog = new EmailLog(
                LOG_ID,
                "bad@example.com",
                "Invitation",
                "invitation-email",
                "FAILED",
                null,
                "Connection timeout",
                NOW
        );

        EmailLogJpaEntity result = EmailLogPersistenceMapper.toEntity(emailLog);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getErrorMessage()).isEqualTo("Connection timeout");
        assertThat(result.getSentAt()).isNull();
    }

    // ═══════════════════════════════════════════════════════════════════
    // toDomainList
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldMapEntityListToDomainList() {
        EmailLogJpaEntity entity1 = sampleEntity();
        EmailLogJpaEntity entity2 = new EmailLogJpaEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setRecipient("other@example.com");
        entity2.setSubject("Invitation");
        entity2.setTemplate("invitation-email");
        entity2.setStatus("PENDING");
        entity2.setCreatedAt(NOW);

        List<EmailLog> result = EmailLogPersistenceMapper.toDomainList(List.of(entity1, entity2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecipient()).isEqualTo("user@example.com");
        assertThat(result.get(1).getRecipient()).isEqualTo("other@example.com");
    }

    @Test
    void shouldReturnEmptyListWhenEntityListIsNull() {
        List<EmailLog> result = EmailLogPersistenceMapper.toDomainList(null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenEntityListIsEmpty() {
        List<EmailLog> result = EmailLogPersistenceMapper.toDomainList(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════
    // toEntityList
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldMapDomainListToEntityList() {
        EmailLog log1 = sampleDomain();
        EmailLog log2 = new EmailLog(
                UUID.randomUUID(),
                "other@example.com",
                "Invitation",
                "invitation-email",
                "PENDING",
                null,
                null,
                NOW
        );

        List<EmailLogJpaEntity> result = EmailLogPersistenceMapper.toEntityList(List.of(log1, log2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecipient()).isEqualTo("user@example.com");
        assertThat(result.get(1).getRecipient()).isEqualTo("other@example.com");
    }

    @Test
    void shouldReturnEmptyListWhenDomainListIsNull() {
        List<EmailLogJpaEntity> result = EmailLogPersistenceMapper.toEntityList(null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenDomainListIsEmpty() {
        List<EmailLogJpaEntity> result = EmailLogPersistenceMapper.toEntityList(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Round-trip consistency
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldRoundTripDomainToEntityAndBack() {
        EmailLog original = sampleDomain();

        EmailLogJpaEntity entity = EmailLogPersistenceMapper.toEntity(original);
        EmailLog roundTripped = EmailLogPersistenceMapper.toDomain(entity);

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getRecipient()).isEqualTo(original.getRecipient());
        assertThat(roundTripped.getSubject()).isEqualTo(original.getSubject());
        assertThat(roundTripped.getTemplate()).isEqualTo(original.getTemplate());
        assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTripped.getSentAt()).isEqualTo(original.getSentAt());
        assertThat(roundTripped.getErrorMessage()).isEqualTo(original.getErrorMessage());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}
