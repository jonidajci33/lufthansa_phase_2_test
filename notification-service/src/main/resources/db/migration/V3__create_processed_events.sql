-- ============================================================
-- V3: Create processed_events table for Kafka idempotency
-- Prevents duplicate notification creation on event replay
-- ============================================================

CREATE TABLE notification.processed_events (
    event_id        VARCHAR(255)    PRIMARY KEY,
    processed_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_processed_events_at ON notification.processed_events(processed_at);
