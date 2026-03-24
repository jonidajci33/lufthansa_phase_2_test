package com.planningpoker.estimation.web.dto;

/**
 * Request body for updating a story's editable fields.
 */
public record UpdateStoryRequest(
        String title,
        String description
) {}
