package com.planningpoker.identity.web;

import com.planningpoker.identity.application.port.in.DeactivateUserUseCase;
import com.planningpoker.identity.application.port.in.GetUserUseCase;
import com.planningpoker.identity.application.port.in.ListUsersUseCase;
import com.planningpoker.identity.application.port.in.UpdateUserUseCase;
import com.planningpoker.identity.domain.Page;
import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.web.dto.UpdateUserRequest;
import com.planningpoker.identity.web.dto.UserResponse;
import com.planningpoker.identity.web.mapper.UserRestMapper;
import com.planningpoker.shared.error.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for user management operations.
 * Note: The {id} path variable is the Keycloak subject UUID (exposed as "id" in the API).
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;

    public UserController(GetUserUseCase getUserUseCase,
                          ListUsersUseCase listUsersUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeactivateUserUseCase deactivateUserUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "Current user profile")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        User user = getUserUseCase.getCurrentUser(jwt.getSubject());
        return ResponseEntity.ok(UserRestMapper.toResponse(user));
    }

    @GetMapping
    @Operation(summary = "List users (admin)", description = "Returns a paginated list of all users. Admin access only.")
    @ApiResponse(responseCode = "200", description = "Paginated list of users")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<PageResponse<UserResponse>> list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        Page<User> page = listUsersUseCase.list(offset, limit);
        List<UserResponse> data = UserRestMapper.toResponseList(page.content());
        return ResponseEntity.ok(PageResponse.of(data, page.totalElements(), limit, offset));
    }

    @GetMapping("/{keycloakId}")
    @Operation(summary = "Get user by ID", description = "Returns a user by their Keycloak ID.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> getById(@PathVariable String keycloakId) {
        User user = getUserUseCase.getByKeycloakId(keycloakId);
        return ResponseEntity.ok(UserRestMapper.toResponse(user));
    }

    @PutMapping("/{keycloakId}")
    @Operation(summary = "Update user profile", description = "Updates a user's profile. Admin access only.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> update(@PathVariable String keycloakId,
                                               @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUserUseCase.updateByKeycloakId(keycloakId, request);
        return ResponseEntity.ok(UserRestMapper.toResponse(user));
    }

    @DeleteMapping("/{keycloakId}")
    @Operation(summary = "Deactivate user (admin)", description = "Soft-deletes a user account. Admin access only.")
    @ApiResponse(responseCode = "204", description = "User deactivated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deactivate(@PathVariable String keycloakId) {
        deactivateUserUseCase.deactivateByKeycloakId(keycloakId);
        return ResponseEntity.noContent().build();
    }
}
