#!/bin/bash
# ================================================================
# init-databases.sh — Create logical databases for each microservice
# Runs automatically on first PostgreSQL startup via
# /docker-entrypoint-initdb.d/
# ================================================================

set -e
set -u

# Function to create a database if it does not exist
create_database() {
    local database=$1
    echo "  Creating database '$database'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $database OWNER $POSTGRES_USER'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
EOSQL
    echo "  Database '$database' created (or already exists)."
}

echo "=== Initializing Planning Poker databases ==="

# The default POSTGRES_DB (pp_identity) is created automatically by the
# postgres image entrypoint. We create the remaining 4 databases plus
# the Keycloak database here.

create_database "identity_db"
create_database "room_db"
create_database "estimation_db"
create_database "notification_db"
create_database "audit_db"

echo "=== Creating schemas per service ==="

# Each service uses its own schema within its database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "identity_db" -c "CREATE SCHEMA IF NOT EXISTS identity;"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "room_db" -c "CREATE SCHEMA IF NOT EXISTS room;"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "estimation_db" -c "CREATE SCHEMA IF NOT EXISTS estimation;"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "notification_db" -c "CREATE SCHEMA IF NOT EXISTS notification;"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "audit_db" -c "CREATE SCHEMA IF NOT EXISTS audit;"

echo "=== All Planning Poker databases and schemas initialized ==="
