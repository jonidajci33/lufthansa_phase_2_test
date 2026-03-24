package com.planningpoker.identity.web;

import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.web.dto.InternalUserResponse;
import com.planningpoker.identity.web.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal REST controller for service-to-service user lookups.
 * <p>
 * No authentication required — access is controlled at the  SecurityConfiglevel
 * via {@code permitAll} on {@code /internal/**}.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final GetUserUseCase getUserUseCase;

    public InternalUserController(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (internal)", description = "Lightweight user lookup for service-to-service communication.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<InternalUserResponse> getById(@PathVariable UUID id) {
        User user = getUserUseCase.getById(id);
        return ResponseEntity.ok(UserRestMapper.toInternalResponse(user));
    }
}
