package com.planningpoker.notification.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planningpoker.notification.application.port.out.EmailSenderPort;
import com.planningpoker.notification.application.port.out.NotificationPersistencePort;
import com.planningpoker.notification.application.port.out.RoomQueryPort;
import com.planningpoker.notification.domain.EmailLog;
import com.planningpoker.notification.domain.Notification;
import com.planningpoker.notification.domain.NotificationType;
import com.planningpoker.shared.event.DomainEvent;
import com.planningpoker.shared.event.EventTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String FRONTEND_URL = "http://localhost:3000";

    @Mock
    private NotificationPersistencePort persistencePort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private RoomQueryPort roomQueryPort;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    @Captor
    private ArgumentCaptor<EmailLog> emailLogCaptor;

    @InjectMocks
    private NotificationEventHandler eventHandler;

    // Constructor injection for @Value — we need to create the handler manually
    private NotificationEventHandler createHandler() {
        return new NotificationEventHandler(persistencePort, emailSenderPort, roomQueryPort, FRONTEND_URL);
    }

    // ── UserRegistered ────────────────────────────────────────────────

    @Test
    void shouldCreateWelcomeNotificationOnUserRegistered() {
        NotificationEventHandler handler = createHandler();
        UUID userId = UUID.randomUUID();
        DomainEvent<JsonNode> event = userRegisteredEvent(userId, "johndoe", "john@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        verify(persistencePort).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(NotificationType.WELCOME);
        assertThat(saved.getTitle()).contains("Welcome");
    }

    @Test
    void shouldSendWelcomeEmailOnUserRegistered() {
        NotificationEventHandler handler = createHandler();
        UUID userId = UUID.randomUUID();
        DomainEvent<JsonNode> event = userRegisteredEvent(userId, "johndoe", "john@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        verify(emailSenderPort).sendEmail(eq("john@example.com"), anyString(), eq("welcome-email"), anyMap());
        verify(persistencePort).saveEmailLog(emailLogCaptor.capture());
        assertThat(emailLogCaptor.getValue().getStatus()).isEqualTo("SENT");
    }

    // ── UserInvited ───────────────────────────────────────────────────

    @Test
    void shouldCreateInvitationNotificationOnUserInvited() {
        NotificationEventHandler handler = createHandler();
        UUID roomId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();
        DomainEvent<JsonNode> event = userInvitedEvent(UUID.randomUUID(), roomId, invitedBy, "invitee@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        verify(persistencePort).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.INVITATION);
        assertThat(saved.getUserId()).isEqualTo(invitedBy);
    }

    @Test
    void shouldSendInvitationEmailOnUserInvited() {
        NotificationEventHandler handler = createHandler();
        UUID roomId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();
        DomainEvent<JsonNode> event = userInvitedEvent(UUID.randomUUID(), roomId, invitedBy, "invitee@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        verify(emailSenderPort).sendEmail(eq("invitee@example.com"), anyString(), eq("invitation-email"), anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseTokenBasedLinkInInvitationEmail() {
        NotificationEventHandler handler = createHandler();
        UUID roomId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();
        DomainEvent<JsonNode> event = userInvitedEvent(UUID.randomUUID(), roomId, invitedBy, "invitee@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailSenderPort).sendEmail(eq("invitee@example.com"), anyString(), eq("invitation-email"), varsCaptor.capture());

        Map<String, Object> vars = varsCaptor.getValue();
        assertThat(vars.get("joinLink")).isEqualTo(FRONTEND_URL + "/invitations/abc12345/accept");
        assertThat(vars.get("roomName")).isEqualTo("Sprint Review");
    }

    @Test
    void shouldNotSendEmailForLinkTypeInvitation() {
        NotificationEventHandler handler = createHandler();
        ObjectNode data = MAPPER.createObjectNode();
        data.put("invitationId", UUID.randomUUID().toString());
        data.put("roomId", UUID.randomUUID().toString());
        UUID invitedBy = UUID.randomUUID();
        data.put("invitedBy", invitedBy.toString());
        data.putNull("email");
        data.put("token", "link1234");
        data.put("type", "LINK");
        data.put("roomName", "My Room");
        DomainEvent<JsonNode> event = createEvent(EventTypes.USER_INVITED, data);

        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        verify(emailSenderPort, never()).sendEmail(anyString(), anyString(), anyString(), anyMap());
        verify(persistencePort).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getTitle()).isEqualTo("Share Link Generated");
    }

    // ── VotingFinished ────────────────────────────────────────────────

    @Test
    void shouldCreateVotingFinishedNotification() {
        NotificationEventHandler handler = createHandler();
        UUID storyId = UUID.randomUUID();
        DomainEvent<JsonNode> event = votingFinishedEvent(storyId, 5, true);
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(event);

        verify(persistencePort).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.VOTING_FINISHED);
        assertThat(saved.getMessage()).contains("5 votes");
        assertThat(saved.getMessage()).contains("Consensus was reached");
    }

    // ── Unknown event ─────────────────────────────────────────────────

    @Test
    void shouldIgnoreUnknownEventType() {
        NotificationEventHandler handler = createHandler();
        DomainEvent<JsonNode> event = unknownEvent();

        handler.handle(event);

        verify(persistencePort, never()).save(any());
        verify(emailSenderPort, never()).sendEmail(anyString(), anyString(), anyString(), anyMap());
    }

    // ── Email failure ─────────────────────────────────────────────────

    @Test
    void shouldLogEmailFailureWithoutThrowingException() {
        NotificationEventHandler handler = createHandler();
        UUID userId = UUID.randomUUID();
        DomainEvent<JsonNode> event = userRegisteredEvent(userId, "johndoe", "john@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(emailSenderPort).sendEmail(anyString(), anyString(), anyString(), anyMap());

        // Should not throw — email failures are caught and logged
        handler.handle(event);

        verify(persistencePort).saveEmailLog(emailLogCaptor.capture());
        EmailLog saved = emailLogCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("FAILED");
        assertThat(saved.getErrorMessage()).contains("SMTP connection refused");
    }

    @Test
    void shouldCreateNotificationEvenWhenEmailFails() {
        NotificationEventHandler handler = createHandler();
        UUID userId = UUID.randomUUID();
        DomainEvent<JsonNode> event = userRegisteredEvent(userId, "johndoe", "john@example.com");
        when(persistencePort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(persistencePort.saveEmailLog(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSenderPort).sendEmail(anyString(), anyString(), anyString(), anyMap());

        handler.handle(event);

        // Notification should still be saved
        verify(persistencePort).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getType()).isEqualTo(NotificationType.WELCOME);
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private DomainEvent<JsonNode> userRegisteredEvent(UUID userId, String username, String email) {
        ObjectNode data = MAPPER.createObjectNode();
        data.put("userId", userId.toString());
        data.put("username", username);
        data.put("email", email);
        data.put("displayName", username);
        return createEvent(EventTypes.USER_REGISTERED, data);
    }

    private DomainEvent<JsonNode> userInvitedEvent(UUID invitationId, UUID roomId, UUID invitedBy, String email) {
        ObjectNode data = MAPPER.createObjectNode();
        data.put("invitationId", invitationId.toString());
        data.put("roomId", roomId.toString());
        data.put("invitedBy", invitedBy.toString());
        data.put("email", email);
        data.put("token", "abc12345");
        data.put("type", "EMAIL");
        data.put("roomName", "Sprint Review");
        return createEvent(EventTypes.USER_INVITED, data);
    }

    private DomainEvent<JsonNode> votingFinishedEvent(UUID storyId, int totalVotes, boolean consensus) {
        ObjectNode data = MAPPER.createObjectNode();
        data.put("storyId", storyId.toString());
        data.put("averageScore", 5.0);
        data.put("totalVotes", totalVotes);
        data.put("consensusReached", consensus);
        return createEvent(EventTypes.VOTING_FINISHED, data);
    }

    private DomainEvent<JsonNode> unknownEvent() {
        ObjectNode data = MAPPER.createObjectNode();
        data.put("foo", "bar");
        return createEvent("some.unknown.event.v1", data);
    }

    private DomainEvent<JsonNode> createEvent(String type, JsonNode data) {
        return new DomainEvent<>(
                "1.0",
                UUID.randomUUID().toString(),
                "test-source",
                type,
                Instant.now(),
                "application/json",
                UUID.randomUUID().toString(),
                null,
                data
        );
    }
}
