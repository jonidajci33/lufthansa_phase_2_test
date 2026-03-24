package com.planningpoker.audit.application.service;

import com.planningpoker.audit.application.port.in.QueryAuditUseCase;
import com.planningpoker.audit.application.port.in.RecordAuditUseCase;
import com.planningpoker.audit.application.port.out.AuditPersistencePort;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.Page;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service implementing audit query and record use cases.
 */
@Service
@Transactional(readOnly = true)
public class AuditService implements QueryAuditUseCase, RecordAuditUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditPersistencePort persistencePort;

    public AuditService(AuditPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Page<AuditEntry> list(AuditFilter filter, int offset, int limit) {
        return persistencePort.findAll(filter, offset, limit);
    }

    @Override
    public AuditEntry getById(Long id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditEntry", id));
    }

    @Override
    public List<AuditEntry> getEntityHistory(String entityType, UUID entityId) {
        return persistencePort.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    @Transactional
    public AuditEntry record(AuditEntry entry) {
        if (entry.getEventId() != null && persistencePort.existsByEventId(entry.getEventId())) {
            log.debug("Duplicate event ignored: eventId={}", entry.getEventId());
            return null;
        }
        AuditEntry saved = persistencePort.save(entry);
        log.info("Audit entry recorded: entityType={}, entityId={}, operation={}, eventId={}",
                saved.getEntityType(), saved.getEntityId(), saved.getOperation(), saved.getEventId());
        return saved;
    }
}
