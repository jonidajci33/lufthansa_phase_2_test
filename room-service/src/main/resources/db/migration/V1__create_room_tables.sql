CREATE SCHEMA IF NOT EXISTS room;

CREATE TABLE IF NOT EXISTS room.deck_types (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS room.deck_values (
    id UUID PRIMARY KEY,
    deck_type_id UUID NOT NULL REFERENCES room.deck_types(id) ON DELETE CASCADE,
    label VARCHAR(20) NOT NULL,
    numeric_value NUMERIC,
    sort_order INT NOT NULL
);

CREATE TABLE IF NOT EXISTS room.rooms (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    moderator_id UUID NOT NULL,
    deck_type_id UUID REFERENCES room.deck_types(id),
    short_code VARCHAR(10) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    max_participants INT NOT NULL DEFAULT 50,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS room.room_participants (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'PARTICIPANT',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at TIMESTAMPTZ,
    is_connected BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(room_id, user_id)
);

CREATE TABLE IF NOT EXISTS room.invitations (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    invited_by UUID NOT NULL,
    email VARCHAR(255),
    token VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rooms_moderator ON room.rooms(moderator_id);
CREATE INDEX IF NOT EXISTS idx_rooms_short_code ON room.rooms(short_code);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON room.rooms(status) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_participants_room ON room.room_participants(room_id);
CREATE INDEX IF NOT EXISTS idx_participants_user ON room.room_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_invitations_room ON room.invitations(room_id);
CREATE INDEX IF NOT EXISTS idx_invitations_token ON room.invitations(token);
CREATE INDEX IF NOT EXISTS idx_invitations_pending ON room.invitations(status) WHERE status = 'PENDING';

CREATE OR REPLACE FUNCTION room.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_rooms_updated_at') THEN
        CREATE TRIGGER trg_rooms_updated_at BEFORE UPDATE ON room.rooms
        FOR EACH ROW EXECUTE FUNCTION room.set_updated_at();
    END IF;
END $$;
