CREATE SCHEMA IF NOT EXISTS estimation;

-- Replicated deck types (same as Room Service seeds)
CREATE TABLE IF NOT EXISTS estimation.deck_types (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS estimation.deck_values (
    id UUID PRIMARY KEY,
    deck_type_id UUID NOT NULL REFERENCES estimation.deck_types(id) ON DELETE CASCADE,
    label VARCHAR(20) NOT NULL,
    numeric_value NUMERIC,
    sort_order INT NOT NULL
);

CREATE TABLE IF NOT EXISTS estimation.stories (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sort_order INT NOT NULL DEFAULT 0,
    final_score NUMERIC,
    consensus_reached BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS estimation.votes (
    id UUID PRIMARY KEY,
    story_id UUID NOT NULL REFERENCES estimation.stories(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    value VARCHAR(20) NOT NULL,
    numeric_value NUMERIC,
    is_anonymous BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(story_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_stories_room ON estimation.stories(room_id);
CREATE INDEX IF NOT EXISTS idx_stories_room_status ON estimation.stories(room_id, status);
CREATE INDEX IF NOT EXISTS idx_stories_room_sort ON estimation.stories(room_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_votes_story ON estimation.votes(story_id);
CREATE INDEX IF NOT EXISTS idx_votes_story_covering ON estimation.votes(story_id) INCLUDE (user_id, value, numeric_value);

CREATE OR REPLACE FUNCTION estimation.set_updated_at()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = now(); RETURN NEW; END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_stories_updated_at') THEN
        CREATE TRIGGER trg_stories_updated_at BEFORE UPDATE ON estimation.stories
        FOR EACH ROW EXECUTE FUNCTION estimation.set_updated_at();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_votes_updated_at') THEN
        CREATE TRIGGER trg_votes_updated_at BEFORE UPDATE ON estimation.votes
        FOR EACH ROW EXECUTE FUNCTION estimation.set_updated_at();
    END IF;
END $$;
