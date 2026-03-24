package com.planningpoker.notification.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.planningpoker.notification.application.port.out.EmailSenderPort;
import com.planningpoker.notification.application.port.out.NotificationPersistencePort;
import com.planningpoker.notification.domain.EmailLog;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import com.planningpoker.notification.application.port.out.RoomQueryPort;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Processes incoming Kafka domain events and creates notifications + triggers emails.
 * This handler sits in the application layer because it orchestrates domain objects
 * and output ports without depending on infrastructure.
 */
@Component
public class NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    private final NotificationPersistencePort persistencePort;
    private final EmailSenderPort emailSenderPort;
    private final RoomQueryPort roomQueryPort;
    private final String frontendUrl;

    public NotificationEventHandler(NotificationPersistencePort persistencePort,
                                    EmailSenderPort emailSenderPort,
                                    RoomQueryPort roomQueryPort,
                                    @Value("${app.frontend-url}") String frontendUrl) {
        this.persistencePort = persistencePort;
        this.emailSenderPort = emailSenderPort;
        this.roomQueryPort = roomQueryPort;
        this.frontendUrl = frontendUrl;
    }

    /**
     * Routes a domain event to the appropriate handler based on its type.
     */
    public void handle(DomainEvent<JsonNode> event) {
        String eventType = event.type();
        log.debug("Processing event type={} id={}", eventType, event.id());

        // Idempotency check — skip already-processed events
        String eventId = event.id();
        if (eventId != null && persistencePort.isEventProcessed(eventId)) {
            log.debug("Skipping already-processed event id={} type={}", eventId, eventType);
            return;
        }

        switch (eventType) {
            case EventTypes.USER_REGISTERED -> handleUserRegistered(event);
            case EventTypes.USER_INVITED -> handleUserInvited(event);
            case EventTypes.VOTING_STARTED -> handleVotingStarted(event);
            case EventTypes.VOTING_FINISHED -> handleVotingFinished(event);
            default -> log.debug("Ignoring unhandled event type={}", eventType);
        }

        // Mark event as processed
        if (eventId != null) {
            persistencePort.markEventProcessed(eventId);
        }
    }

    // ── UserRegistered ─────────────────────────────────────────────────

    private void handleUserRegistered(DomainEvent<JsonNode> event) {
        JsonNode data = event.data();
        UUID userId = UUID.fromString(data.get("userId").asText());
        String username = data.get("username").asText();
        String email = data.get("email").asText();

        // Create in-app notification
        Notification notification = Notification.create(
                userId,
                NotificationType.WELCOME,
                "Welcome to Planning Poker!",
                "Hi " + username + ", your account has been created successfully. Start by creating or joining a room."
        );
        persistencePort.save(notification);
        log.info("Created WELCOME notification for user={}", userId);

        // Send welcome email
        sendEmail(email, "Welcome to Planning Poker!", "welcome-email",
                Map.of("username", username, "frontendUrl", frontendUrl));
    }

    // ── UserInvited ────────────────────────────────────────────────────

    private void handleUserInvited(DomainEvent<JsonNode> event) {
        JsonNode data = event.data();
        UUID roomId = UUID.fromString(data.get("roomId").asText());
        UUID invitedBy = UUID.fromString(data.get("invitedBy").asText());
        String email = data.has("email") && !data.get("email").isNull() ? data.get("email").asText() : null;
        String invitationType = data.has("type") ? data.get("type").asText() : "EMAIL";
        String token = data.has("token") && !data.get("token").isNull() ? data.get("token").asText() : null;
        String roomName = data.has("roomName") && !data.get("roomName").isNull() ? data.get("roomName").asText() : "a Planning Poker room";

        // LINK-type invitations: do NOT send email — just log a notification for the inviter
        if ("LINK".equals(invitationType)) {
            Notification notification = Notification.create(
                    invitedBy,
                    NotificationType.INVITATION,
                    "Share Link Generated",
                    "A shareable link has been generated for room '" + roomName + "'."
            );
            persistencePort.save(notification);
            log.info("Created LINK-type notification for invitedBy={} (no email)", invitedBy);
            return;
        }

        // EMAIL-type: validate we have what we need
        if (email == null || email.isBlank()) {
            log.warn("USER_INVITED event (EMAIL type) has no email address — skipping. invitedBy={}", invitedBy);
            return;
        }

        if (token == null || token.isBlank()) {
            log.error("USER_INVITED event (EMAIL type) has no token — cannot construct join link. invitedBy={}", invitedBy);
            return;
        }

        // Create in-app notification for the inviter
        Notification notification = Notification.create(
                invitedBy,
                NotificationType.INVITATION,
                "Invitation Sent",
                "An invitation has been sent to " + email + " for room '" + roomName + "'."
        );
        persistencePort.save(notification);
        log.info("Created INVITATION notification for invitedBy={}", invitedBy);

        // Send invitation email with TOKEN-BASED join link
        String joinLink = frontendUrl + "/invitations/" + token + "/accept";
        sendEmail(email, "You've been invited to " + roomName + "!", "invitation-email",
                Map.of("joinLink", joinLink, "roomName", roomName, "roomId", roomId.toString(), "frontendUrl", frontendUrl));
    }

    // ── VotingStarted ─────────────────────────────────────────────────

    private void handleVotingStarted(DomainEvent<JsonNode> event) {
        JsonNode data = event.data();
        UUID storyId = UUID.fromString(data.get("storyId").asText());
        UUID roomId = UUID.fromString(data.get("roomId").asText());
        String storyTitle = data.has("storyTitle") ? data.get("storyTitle").asText() : "Unknown Story";
        UUID moderatorId = data.has("moderatorId") ? UUID.fromString(data.get("moderatorId").asText()) : null;

        // Fetch room name from Room Service
        RoomQueryPort.RoomInfo room = roomQueryPort.getRoom(roomId);
        String roomName = room != null ? room.name() : roomId.toString();

        // Fetch participants from Room Service
        List<UUID> participantIds = roomQueryPort.getParticipantUserIds(roomId);

        if (participantIds.isEmpty()) {
            log.warn("No participants found for roomId={} — skipping VOTING_STARTED notifications", roomId);
            return;
        }

        String metadataJson = String.format(
                "{\"roomId\":\"%s\",\"storyId\":\"%s\",\"actionUrl\":\"/rooms/%s\"}",
                roomId, storyId, roomId
        );

        int created = 0;
        for (UUID participantId : participantIds) {
            // Exclude the moderator who started voting
            if (participantId.equals(moderatorId)) {
                continue;
            }

            Notification notification = Notification.createWithMetadata(
                    participantId,
                    NotificationType.VOTING_STARTED,
                    "Voting Started",
                    "Voting started on '" + storyTitle + "' in room '" + roomName + "'",
                    metadataJson
            );
            persistencePort.save(notification);
            created++;
        }

        log.info("Created {} VOTING_STARTED notifications for story={} in room={}", created, storyId, roomId);
    }

    // ── VotingFinished ─────────────────────────────────────────────────

    private void handleVotingFinished(DomainEvent<JsonNode> event) {
        JsonNode data = event.data();
        UUID storyId = UUID.fromString(data.get("storyId").asText());
        int totalVotes = data.get("totalVotes").asInt();
        boolean consensusReached = data.get("consensusReached").asBoolean();

        String consensusText = consensusReached ? "Consensus was reached" : "No consensus was reached";

        // Note: In a production system this would look up room participants and create
        // a notification for each. For now we create a system-level notification placeholder.
        // The storyId is used as the userId placeholder — in practice you'd iterate over participants.
        log.info("VotingFinished for story={} totalVotes={} consensus={}", storyId, totalVotes, consensusReached);

        // Create a notification — using storyId as a reference. In a real system, we'd
        // query the room-service for participant list and notify each one.
        Notification notification = Notification.create(
                storyId, // placeholder — real impl would iterate participants
                NotificationType.VOTING_FINISHED,
                "Voting Completed",
                "Voting has finished with " + totalVotes + " votes. " + consensusText + "."
        );
        persistencePort.save(notification);
        log.info("Created VOTING_FINISHED notification for story={}", storyId);
    }

    // ── Email helper ───────────────────────────────────────────────────

    private void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        EmailLog emailLog = EmailLog.create(to, subject, templateName);
        try {
            emailSenderPort.sendEmail(to, subject, templateName, variables);
            emailLog.markSent();
            log.info("Email sent to={} subject={}", to, subject);
        } catch (Exception e) {
            emailLog.markFailed(e.getMessage());
            log.error("Failed to send email to={} subject={}: {}", to, subject, e.getMessage());
        }
        persistencePort.saveEmailLog(emailLog);
    }
}
