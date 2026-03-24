package com.planningpoker.room.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for inviting a user to a room.
 */
public record InviteRequest(
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "Invitation type is required")
        @Pattern(regexp = "EMAIL|LINK", message = "Type must be EMAIL or LINK")
        String type
) {}
