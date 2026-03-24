package com.planningpoker.notification.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an email delivery log entry.
 */
public class EmailLog {

    private UUID id;
    private String recipient;
    private String subject;
    private String template;
    private String status; // PENDING, SENT, FAILED
    private Instant sentAt;
    private String errorMessage;
    private Instant createdAt;

    public EmailLog() {
    }

    public EmailLog(UUID id, String recipient, String subject, String template,
                    String status, Instant sentAt, String errorMessage, Instant createdAt) {
        this.id = id;
        this.recipient = recipient;
        this.subject = subject;
        this.template = template;
        this.status = status;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    // ── Factory ────────────────────────────────────────────────────────

    public static EmailLog create(String recipient, String subject, String template) {
        return new EmailLog(
                UUID.randomUUID(),
                recipient,
                subject,
                template,
                "PENDING",
                null,
                null,
                Instant.now()
        );
    }

    public void markSent() {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }

    public void markFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
