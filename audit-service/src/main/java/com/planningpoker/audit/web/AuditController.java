package com.planningpoker.audit.web;

import com.planningpoker.audit.application.port.in.QueryAuditUseCase;
import com.planningpoker.audit.domain.AuditEntry;
import com.planningpoker.audit.domain.AuditFilter;
import com.planningpoker.audit.domain.AuditOperation;
import com.planningpoker.audit.domain.Page;
import com.planningpoker.audit.web.dto.AuditEntryResponse;
import com.planningpoker.audit.web.mapper.AuditEntryRestMapper;
import com.planningpoker.shared.error.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for querying audit trail entries.
 * All endpoints require ADMIN role (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit")
public class AuditController {

    private final QueryAuditUseCase queryAuditUseCase;

    public AuditController(QueryAuditUseCase queryAuditUseCase) {
        this.queryAuditUseCase = queryAuditUseCase;
    }

    @GetMapping
    @Operation(summary = "List audit entries", description = "Returns a paginated, filterable list of audit trail entries.")
    @ApiResponse(responseCode = "200", description = "Paginated list of audit entries")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    public ResponseEntity<PageResponse<AuditEntryResponse>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        AuditOperation op = null;
        if (operation != null && !operation.isBlank()) {
            op = AuditOperation.valueOf(operation.toUpperCase());
        }

        AuditFilter filter = new AuditFilter(entityType, op, userId, from, to);
        Page<AuditEntry> page = queryAuditUseCase.list(filter, offset, limit);

        List<AuditEntryResponse> data = AuditEntryRestMapper.toResponseList(page.content());

        return ResponseEntity.ok(PageResponse.of(data, page.totalElements(), limit, offset));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit entry by ID", description = "Returns a single audit entry by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Audit entry found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    @ApiResponse(responseCode = "404", description = "Audit entry not found")
    public ResponseEntity<AuditEntryResponse> getById(@PathVariable Long id) {
        AuditEntry entry = queryAuditUseCase.getById(id);
        return ResponseEntity.ok(AuditEntryRestMapper.toResponse(entry));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get entity history", description = "Returns the full audit history for a specific entity.")
    @ApiResponse(responseCode = "200", description = "List of audit entries for the entity")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    public ResponseEntity<List<AuditEntryResponse>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {

        List<AuditEntry> history = queryAuditUseCase.getEntityHistory(entityType, entityId);
        List<AuditEntryResponse> response = AuditEntryRestMapper.toResponseList(history);
        return ResponseEntity.ok(response);
    }
}
