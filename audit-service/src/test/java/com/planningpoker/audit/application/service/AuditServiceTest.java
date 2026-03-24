package com.planningpoker.audit.application.service;

import com.planningpoker.audit.application.port.out.AuditPersistencePort;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.AuditOperation;
import com.planningpoker.audit.domain.Page;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditPersistencePort persistencePort;

    @InjectMocks
    private AuditService auditService;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID ENTITY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private static AuditEntry sampleEntry() {
        return new AuditEntry(1L, "user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.now(), null,
                "{\"name\":\"John\"}", "corr-1", "evt-1");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Record
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldRecordNewAuditEntry() {
        AuditEntry entry = AuditEntry.create("user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.now(), null, "{}", "corr-1", "evt-new");
        AuditEntry saved = new AuditEntry(1L, "user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.now(), null, "{}", "corr-1", "evt-new");

        when(persistencePort.existsByEventId("evt-new")).thenReturn(false);
        when(persistencePort.save(entry)).thenReturn(saved);

        AuditEntry result = auditService.record(entry);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(persistencePort).save(entry);
    }

    @Test
    void shouldDeduplicateByEventId() {
        AuditEntry entry = AuditEntry.create("user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.now(), null, "{}", "corr-1", "evt-dup");

        when(persistencePort.existsByEventId("evt-dup")).thenReturn(true);

        AuditEntry result = auditService.record(entry);

        assertThat(result).isNull();
        verify(persistencePort, never()).save(any());
    }

    @Test
    void shouldRecordEntryWithNullEventId() {
        AuditEntry entry = AuditEntry.create("user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.now(), null, "{}", "corr-1", null);
        AuditEntry saved = new AuditEntry(2L, "user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.now(), null, "{}", "corr-1", null);

        when(persistencePort.save(entry)).thenReturn(saved);

        AuditEntry result = auditService.record(entry);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        verify(persistencePort).save(entry);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Query — list
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldListWithFilters() {
        AuditFilter filter = new AuditFilter("user", AuditOperation.CREATED, null, null, null);
        Page<AuditEntry> page = new Page<>(List.of(sampleEntry()), 1L);

        when(persistencePort.findAll(filter, 0, 20)).thenReturn(page);

        Page<AuditEntry> result = auditService.list(filter, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmptyPageWhenNoResults() {
        AuditFilter filter = AuditFilter.empty();
        Page<AuditEntry> page = new Page<>(List.of(), 0L);

        when(persistencePort.findAll(filter, 0, 20)).thenReturn(page);

        Page<AuditEntry> result = auditService.list(filter, 0, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Query — getById
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnEntryById() {
        AuditEntry entry = sampleEntry();
        when(persistencePort.findById(1L)).thenReturn(Optional.of(entry));

        AuditEntry result = auditService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEntityType()).isEqualTo("user");
    }

    @Test
    void shouldThrowWhenEntryNotFound() {
        when(persistencePort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Query — getEntityHistory
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnEntityHistory() {
        AuditEntry e1 = sampleEntry();
        AuditEntry e2 = new AuditEntry(2L, "user", ENTITY_ID, AuditOperation.UPDATED,
                USER_ID, "identity-service", Instant.now(), "{\"name\":\"John\"}",
                "{\"name\":\"Jane\"}", "corr-2", "evt-2");

        when(persistencePort.findByEntityTypeAndEntityId("user", ENTITY_ID))
                .thenReturn(List.of(e1, e2));

        List<AuditEntry> result = auditService.getEntityHistory("user", ENTITY_ID);

        assertThat(result).hasSize(2);
    }
}
