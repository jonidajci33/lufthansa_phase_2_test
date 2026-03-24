package com.planningpoker.audit.web;

import com.planningpoker.audit.application.port.in.QueryAuditUseCase;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditOperation;
import com.planningpoker.audit.domain.Page;
import com.planningpoker.audit.infrastructure.config.SecurityConfig;
import com.planningpoker.shared.error.GlobalExceptionHandler;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueryAuditUseCase queryAuditUseCase;

    // ── Helpers ──────────────────────────────────────────────────────

    private static final UUID ENTITY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String ADMIN_SUB = USER_ID.toString();

    private static AuditEntry sampleEntry() {
        return new AuditEntry(1L, "user", ENTITY_ID, AuditOperation.CREATED,
                USER_ID, "identity-service", Instant.parse("2026-03-18T10:00:00Z"),
                null, "{\"name\":\"John\"}", "corr-1", "evt-1");
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET /api/v1/audit
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldListAuditEntriesWithPagination() throws Exception {
        AuditEntry entry = sampleEntry();
        Page<AuditEntry> page = new Page<>(List.of(entry), 1L);
        given(queryAuditUseCase.list(any(), eq(0), eq(20))).willReturn(page);

        mockMvc.perform(get("/api/v1/audit")
                        .param("offset", "0")
                        .param("limit", "20")
                        .with(JwtTestHelper.withAdmin(ADMIN_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].entityType", is("user")))
                .andExpect(jsonPath("$.data[0].operation", is("CREATED")))
                .andExpect(jsonPath("$.meta.total", is(1)))
                .andExpect(jsonPath("$.meta.limit", is(20)))
                .andExpect(jsonPath("$.meta.offset", is(0)))
                .andExpect(jsonPath("$.meta.hasNext", is(false)));
    }

    @Test
    void shouldFilterByEntityType() throws Exception {
        AuditEntry entry = sampleEntry();
        Page<AuditEntry> page = new Page<>(List.of(entry), 1L);
        given(queryAuditUseCase.list(any(), eq(0), eq(20))).willReturn(page);

        mockMvc.perform(get("/api/v1/audit")
                        .param("entityType", "user")
                        .with(JwtTestHelper.withAdmin(ADMIN_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].entityType", is("user")));
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET /api/v1/audit/{id}
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldGetAuditEntryById() throws Exception {
        AuditEntry entry = sampleEntry();
        given(queryAuditUseCase.getById(1L)).willReturn(entry);

        mockMvc.perform(get("/api/v1/audit/{id}", 1L)
                        .with(JwtTestHelper.withAdmin(ADMIN_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.entityType", is("user")))
                .andExpect(jsonPath("$.entityId", is(ENTITY_ID.toString())))
                .andExpect(jsonPath("$.operation", is("CREATED")))
                .andExpect(jsonPath("$.sourceService", is("identity-service")));
    }

    @Test
    void shouldReturn404WhenEntryNotFound() throws Exception {
        given(queryAuditUseCase.getById(999L))
                .willThrow(new ResourceNotFoundException("AuditEntry", 999L));

        mockMvc.perform(get("/api/v1/audit/{id}", 999L)
                        .with(JwtTestHelper.withAdmin(ADMIN_SUB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("AUDITENTRY_NOT_FOUND")));
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET /api/v1/audit/entity/{entityType}/{entityId}
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldGetEntityHistory() throws Exception {
        AuditEntry e1 = sampleEntry();
        AuditEntry e2 = new AuditEntry(2L, "user", ENTITY_ID, AuditOperation.UPDATED,
                USER_ID, "identity-service", Instant.parse("2026-03-18T11:00:00Z"),
                "{\"name\":\"John\"}", "{\"name\":\"Jane\"}", "corr-2", "evt-2");

        given(queryAuditUseCase.getEntityHistory("user", ENTITY_ID))
                .willReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/v1/audit/entity/{entityType}/{entityId}", "user", ENTITY_ID)
                        .with(JwtTestHelper.withAdmin(ADMIN_SUB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operation", is("CREATED")))
                .andExpect(jsonPath("$[1].operation", is("UPDATED")));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Security
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturn403WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .with(JwtTestHelper.withUser(ADMIN_SUB)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/audit"))
                .andExpect(status().isUnauthorized());
    }
}
