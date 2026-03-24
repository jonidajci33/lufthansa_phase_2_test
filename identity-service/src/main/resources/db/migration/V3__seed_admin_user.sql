-- =============================================================================
-- V3__seed_admin_user.sql
-- PURPOSE: Seed default admin user for local development and testing.
-- WARNING: This is DEV SEED DATA. Do NOT rely on this user in production.
--          The admin password is 'admin' (set in Keycloak realm-export.json).
--          The keycloak_id below MUST match the user ID in realm-export.json.
-- =============================================================================

-- Insert admin user
INSERT INTO identity.users (
    id,
    keycloak_id,
    username,
    email,
    display_name,
    first_name,
    last_name,
    avatar_url,
    is_active,
    created_at,
    updated_at
) VALUES (
    '10000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@planningpoker.local',
    'Platform Admin',
    'Platform',
    'Admin',
    NULL,
    true,
    now(),
    now()
)
ON CONFLICT (id) DO NOTHING;

-- Insert ADMIN and PARTICIPANT roles
INSERT INTO identity.user_roles (user_id, role)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'ADMIN'),
    ('10000000-0000-0000-0000-000000000001', 'PARTICIPANT')
ON CONFLICT (user_id, role) DO NOTHING;

-- Log seed execution (cross-pollinated from DB-02)
DO $$ BEGIN RAISE NOTICE '[V3] Admin user seed migration executed.'; END $$;
