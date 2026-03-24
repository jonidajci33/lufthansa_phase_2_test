package com.planningpoker.estimation.web.dto;

import java.util.UUID;

/**
 * WebSocket message sent during voting to indicate how many votes
 * have been submitted (without revealing actual values).
 */
public record VoteCountMessage(
        UUID storyId,
        int voteCount
) {}
