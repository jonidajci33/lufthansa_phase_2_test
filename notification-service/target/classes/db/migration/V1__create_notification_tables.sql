-- ============================================================
-- V1: Create notification schema tables
-- ============================================================

CREATE SCHEMA IF NOT EXISTS notification;

-- In-app notifications
CREATE TABLE notification.notifications (
    id          UUID            PRIMARY KEY,
    user_id     UUID            NOT NULL,
    type        VARCHAR(50)     NOT NULL,
    title       VARCHAR(200)    NOT NULL,
    message     TEXT,
    is_read     BOOLEAN         NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notification.notifications (user_id);
CREATE INDEX idx_notifications_user_unread ON notification.notifications (user_id) WHERE is_read = false;

-- User notification channel preferences
CREATE TABLE notification.notification_preferences (
    user_id     UUID            NOT NULL,
    channel     VARCHAR(20)     NOT NULL,
    enabled     BOOLEAN         NOT NULL DEFAULT true,
    PRIMARY KEY (user_id, channel)
);

-- Email delivery log
CREATE TABLE notification.email_log (
    id              UUID            PRIMARY KEY,
    recipient       VARCHAR(255)    NOT NULL,
    subject         VARCHAR(200)    NOT NULL,
    template        VARCHAR(100),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    sent_at         TIMESTAMPTZ,
    error_message   TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_email_log_recipient ON notification.email_log (recipient);
CREATE INDEX idx_email_log_status ON notification.email_log (status);
