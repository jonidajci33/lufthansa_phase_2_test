CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE audit.audit_entries (
    id         BIGSERIAL    PRIMARY KEY,
    entity_type    VARCHAR(50)  NOT NULL,
    entity_id      UUID         NOT NULL,
    operation      VARCHAR(20)  NOT NULL,
    user_id        UUID,
    source_service VARCHAR(50)  NOT NULL,
    timestamp      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    previous_state JSONB,
    new_state      JSONB,
    correlation_id VARCHAR(100),
    event_id       VARCHAR(100) UNIQUE
);

CREATE INDEX idx_audit_entity ON audit.audit_entries(entity_type, entity_id);
CREATE INDEX idx_audit_user   ON audit.audit_entries(user_id);
CREATE INDEX idx_audit_ts     ON audit.audit_entries(timestamp);
