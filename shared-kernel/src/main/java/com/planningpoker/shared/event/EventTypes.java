package com.planningpoker.shared.event;

/**
 * Central registry of all event type constants used across services.
 * Type format follows CloudEvents convention: {domain}.{entity}.{action}.v1
 */
public final class EventTypes {

    private EventTypes() {}

    // Identity Service events
    public static final String USER_REGISTERED = "identity.user.registered.v1";
    public static final String USER_UPDATED = "identity.user.updated.v1";
    public static final String USER_DEACTIVATED = "identity.user.deactivated.v1";

    // Room Service events
    public static final String ROOM_CREATED = "room.room.created.v1";
    public static final String ROOM_UPDATED = "room.room.updated.v1";
    public static final String ROOM_DELETED = "room.room.deleted.v1";
    public static final String USER_INVITED = "room.invitation.created.v1";
    public static final String USER_JOINED_ROOM = "room.participant.joined.v1";
    public static final String USER_LEFT_ROOM = "room.participant.left.v1";

    // Estimation Service events
    public static final String STORY_CREATED = "estimation.story.created.v1";
    public static final String STORY_UPDATED = "estimation.story.updated.v1";
    public static final String STORY_DELETED = "estimation.story.deleted.v1";
    public static final String VOTING_STARTED = "estimation.voting.started.v1";
    public static final String VOTE_SUBMITTED = "estimation.vote.submitted.v1";
    public static final String VOTING_FINISHED = "estimation.voting.finished.v1";
}
