-- ============================================================
-- V2: Add metadata JSONB column to notifications table
-- Stores structured data like roomId, storyId, actionUrl
-- ============================================================

ALTER TABLE notification.notifications ADD COLUMN metadata JSONB;
