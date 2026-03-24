package com.planningpoker.shared.event;

/**
 * Kafka topic name constants. Topic naming follows: {domain}.{entity}.events
 */
public final class Topics {

    private Topics() {}

    public static final String IDENTITY_USER_EVENTS = "identity.user.events";
    public static final String ROOM_EVENTS = "room.events";
    public static final String ESTIMATION_STORY_EVENTS = "estimation.story.events";
    public static final String ESTIMATION_VOTE_EVENTS = "estimation.vote.events";
}
