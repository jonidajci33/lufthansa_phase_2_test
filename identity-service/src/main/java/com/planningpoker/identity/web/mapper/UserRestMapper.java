package com.planningpoker.identity.web.mapper;

import com.planningpoker.identity.domain.User;
import com.planningpoker.identity.domain.UserRole;
import com.planningpoker.identity.web.dto.InternalUserResponse;
import com.planningpoker.identity.web.dto.UserResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Maps between {@link User} (domain) and web-layer DTOs.
 * Pure static utility -- no Spring annotations, no state.
 */
public final class UserRestMapper {

    private UserRestMapper() {
        // utility class -- prevent instantiation
    }

    /**
     * Converts a domain User to a public UserResponse DTO.
     *
     * @param user the domain User (may be null)
     * @return the response DTO, or null if user is null
     */
    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        Set<String> roleNames = user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                UUID.fromString(user.getKeycloakId()),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.isActive(),
                roleNames,
                user.getCreatedAt()
        );
    }

    /**
     * Converts a domain User to a lightweight InternalUserResponse DTO
     * for service-to-service communication.
     *
     * @param user the domain User (may be null)
     * @return the internal response DTO, or null if user is null
     */
    public static InternalUserResponse toInternalResponse(User user) {
        if (user == null) {
            return null;
        }
        return new InternalUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.isActive()
        );
    }

    /**
     * Converts a list of domain Users to public UserResponse DTOs.
     *
     * @param users the domain Users (may be null or empty)
     * @return list of response DTOs, never null
     */
    public static List<UserResponse> toResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        return users.stream()
                .map(UserRestMapper::toResponse)
                .toList();
    }

    /**
     * Converts a list of domain Users to internal response DTOs.
     *
     * @param users the domain Users (may be null or empty)
     * @return list of internal response DTOs, never null
     */
    public static List<InternalUserResponse> toInternalResponseList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        return users.stream()
                .map(UserRestMapper::toInternalResponse)
                .toList();
    }
}
