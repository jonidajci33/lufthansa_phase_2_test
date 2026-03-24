package com.planningpoker.notification.infrastructure.persistence;

import com.planningpoker.notification.application.port.out.NotificationPersistencePort;
import com.planningpoker.notification.domain.EmailLog;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter that implements both the domain {@link NotificationRepository}
 * and the application {@link NotificationPersistencePort} by delegating to Spring Data JPA.
 */
@Component
public class NotificationPersistenceAdapter implements NotificationPersistencePort, NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final EmailLogJpaRepository emailLogJpaRepository;
    private final ProcessedEventJpaRepository processedEventJpaRepository;

    public NotificationPersistenceAdapter(NotificationJpaRepository notificationJpaRepository,
                                          EmailLogJpaRepository emailLogJpaRepository,
                                          ProcessedEventJpaRepository processedEventJpaRepository) {
        this.notificationJpaRepository = notificationJpaRepository;
        this.emailLogJpaRepository = emailLogJpaRepository;
        this.processedEventJpaRepository = processedEventJpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = NotificationPersistenceMapper.toEntity(notification);
        NotificationJpaEntity saved = notificationJpaRepository.save(entity);
        return NotificationPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationJpaRepository.findById(id)
                .map(NotificationPersistenceMapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(UUID userId, int offset, int limit) {
        int pageNumber = offset / Math.max(limit, 1);
        var page = notificationJpaRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(pageNumber, limit));
        return NotificationPersistenceMapper.toDomainList(page.getContent());
    }

    @Override
    public long countByUserId(UUID userId) {
        return notificationJpaRepository.countByUserId(userId);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return notificationJpaRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllReadByUserId(UUID userId) {
        notificationJpaRepository.markAllReadByUserId(userId);
    }

    @Override
    public EmailLog saveEmailLog(EmailLog emailLog) {
        EmailLogJpaEntity entity = EmailLogPersistenceMapper.toEntity(emailLog);
        EmailLogJpaEntity saved = emailLogJpaRepository.save(entity);
        return EmailLogPersistenceMapper.toDomain(saved);
    }

    @Override
    public boolean isEventProcessed(String eventId) {
        return processedEventJpaRepository.existsById(eventId);
    }

    @Override
    public void markEventProcessed(String eventId) {
        processedEventJpaRepository.save(new ProcessedEventJpaEntity(eventId));
    }
}
