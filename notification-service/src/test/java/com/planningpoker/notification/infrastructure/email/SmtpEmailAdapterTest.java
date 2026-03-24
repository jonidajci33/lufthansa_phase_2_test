package com.planningpoker.notification.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private SmtpEmailAdapter adapter;

    private static final String FROM_ADDRESS = "noreply@planningpoker.com";
    private static final String FROM_NAME = "Planning Poker";

    @BeforeEach
    void setUp() {
        adapter = new SmtpEmailAdapter(mailSender, templateEngine, FROM_ADDRESS, FROM_NAME);
    }

    // ═══════════════════════════════════════════════════════════════════
    // sendEmail — happy path
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSendEmailSuccessfully() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-email"), any(Context.class)))
                .thenReturn("<html><body>Welcome!</body></html>");

        Map<String, Object> variables = Map.of("username", "john", "frontendUrl", "https://app.example.com");

        adapter.sendEmail("john@example.com", "Welcome!", "welcome-email", variables);

        verify(templateEngine).process(eq("welcome-email"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("username")).isEqualTo("john");
        assertThat(capturedContext.getVariable("frontendUrl")).isEqualTo("https://app.example.com");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldPassCorrectTemplateName() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("invitation-email"), any(Context.class)))
                .thenReturn("<html>Invite</html>");

        adapter.sendEmail("invitee@example.com", "You're invited!", "invitation-email",
                Map.of("joinLink", "https://app.example.com/join/abc"));

        verify(templateEngine).process(eq("invitation-email"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldHandleEmptyVariablesMap() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("simple"), any(Context.class)))
                .thenReturn("<html>Simple</html>");

        adapter.sendEmail("user@example.com", "Simple", "simple", Map.of());

        verify(templateEngine).process(eq("simple"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);
    }

    // ═══════════════════════════════════════════════════════════════════
    // sendEmail — error paths
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldWrapMessagingExceptionInRuntimeException() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-email"), any(Context.class)))
                .thenReturn("<html>Content</html>");
        doThrow(new RuntimeException("Failed to send email to bad@example.com",
                new MessagingException("SMTP error")))
                .when(mailSender).send(mimeMessage);

        assertThatThrownBy(() -> adapter.sendEmail("bad@example.com", "Welcome!", "welcome-email", Map.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bad@example.com");
    }

    @Test
    void shouldWrapTemplateProcessingException() {
        when(templateEngine.process(eq("broken-template"), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found: broken-template"));

        assertThatThrownBy(() -> adapter.sendEmail("user@example.com", "Subject", "broken-template", Map.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("broken-template");
    }
}
