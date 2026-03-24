package com.planningpoker.audit.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditOperationTest {

    @Test
    void shouldHaveExactlyThreeOperations() {
        assertThat(AuditOperation.values()).containsExactlyInAnyOrder(
                AuditOperation.CREATED,
                AuditOperation.UPDATED,
                AuditOperation.DELETED
        );
    }

    @Test
    void shouldResolveFromName() {
        assertThat(AuditOperation.valueOf("CREATED")).isEqualTo(AuditOperation.CREATED);
        assertThat(AuditOperation.valueOf("UPDATED")).isEqualTo(AuditOperation.UPDATED);
        assertThat(AuditOperation.valueOf("DELETED")).isEqualTo(AuditOperation.DELETED);
    }
}
