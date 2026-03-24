package com.planningpoker.room.web.dto;

/**
 * Response DTO for the share-link endpoint.
 * Contains the room's short code and the full shareable join URL.
 */
public record ShareLinkResponse(
        String shortCode,
        String shareLink
) {}
