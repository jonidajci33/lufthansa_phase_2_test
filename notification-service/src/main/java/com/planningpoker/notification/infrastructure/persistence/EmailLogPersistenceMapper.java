package com.planningpoker.notification.infrastructure.persistence;

import com.planningpoker.notification.domain.EmailLog;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper between {@link EmailLog} domain objects and {@link EmailLogJpaEntity} persistence entities.
 * <p>
 * Pure utility class — no framework imports, null-safe.
 */
public final class EmailLogPersistenceMapper {

    private EmailLogPersistenceMapper() {
        // utility class
    }

    /**
     * Converts an {@link EmailLogJpaEntity} to a domain {@link EmailLog}.
     *
     * @param entity the JPA entity (may be null)
     * @return the domain email log, or {@code null} if the input is null
     */
    public static EmailLog toDomain(EmailLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new EmailLog(
                entity.getId(),
                entity.getRecipient(),
                entity.getSubject(),
                entity.getTemplate(),
                entity.getStatus(),
                entity.getSentAt(),
                entity.getErrorMessage(),
                entity.getCreatedAt()
        );
    }

    /**
     * Converts a domain {@link EmailLog} to an {@link EmailLogJpaEntity}.
     *
     * @param emailLog the domain email log (may be null)
     * @return the JPA entity, or {@code null} if the input is null
     */
    public static EmailLogJpaEntity toEntity(EmailLog emailLog) {
        if (emailLog == null) {
            return null;
        }
        EmailLogJpaEntity entity = new EmailLogJpaEntity();
        entity.setId(emailLog.getId());
        entity.setRecipient(emailLog.getRecipient());
        entity.setSubject(emailLog.getSubject());
        entity.setTemplate(emailLog.getTemplate());
        entity.setStatus(emailLog.getStatus());
        entity.setSentAt(emailLog.getSentAt());
        entity.setErrorMessage(emailLog.getErrorMessage());
        entity.setCreatedAt(emailLog.getCreatedAt());
        return entity;
    }

    /**
     * Converts a list of {@link EmailLogJpaEntity} to domain {@link EmailLog} objects.
     *
     * @param entities the JPA entities (may be null)
     * @return an unmodifiable list of domain email logs; empty list if input is null or empty
     */
    public static List<EmailLog> toDomainList(List<EmailLogJpaEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(EmailLogPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Converts a list of domain {@link EmailLog} objects to {@link EmailLogJpaEntity} instances.
     *
     * @param emailLogs the domain email logs (may be null)
     * @return an unmodifiable list of JPA entities; empty list if input is null or empty
     */
    public static List<EmailLogJpaEntity> toEntityList(List<EmailLog> emailLogs) {
        if (emailLogs == null || emailLogs.isEmpty()) {
            return Collections.emptyList();
        }
        return emailLogs.stream()
                .map(EmailLogPersistenceMapper::toEntity)
                .toList();
    }
}
