-- Drop ALL check constraints on notifications table (including Hibernate-generated ones)
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'notifications'
          AND nsp.nspname = 'notification'
          AND con.contype = 'c'
    ) LOOP
        EXECUTE 'ALTER TABLE notification.notifications DROP CONSTRAINT ' || quote_ident(r.conname);
    END LOOP;
END $$;
