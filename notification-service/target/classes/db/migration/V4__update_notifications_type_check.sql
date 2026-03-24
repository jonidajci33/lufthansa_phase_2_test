-- Drop the Hibernate-generated check constraint on type column
-- and replace with one that includes VOTING_STARTED
ALTER TABLE notification.notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notification.notifications ADD CONSTRAINT notifications_type_check
    CHECK (type IN ('WELCOME', 'INVITATION', 'VOTING_STARTED', 'VOTING_FINISHED', 'SYSTEM'));
