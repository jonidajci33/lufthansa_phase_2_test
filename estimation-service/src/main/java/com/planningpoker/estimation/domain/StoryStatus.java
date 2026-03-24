package com.planningpoker.estimation.domain;

/**
 * Lifecycle status of a story in a planning poker session.
 */
public enum StoryStatus {
    PENDING,
    VOTING,
    VOTED,
    SKIPPED
}
