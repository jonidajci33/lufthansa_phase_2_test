#!/bin/bash
# Post-startup Keycloak configuration:
# 1. Ensures protocol mappers (sub, email, preferred_username) on the SPA client
# 2. Creates the platform admin user if it doesn't exist
#
# All secrets come from environment variables — nothing hardcoded.
# Requires: KC_ADMIN, KC_ADMIN_PASSWORD, KC_URL, PP_ADMIN_USERNAME,
#           PP_ADMIN_PASSWORD, PP_ADMIN_EMAIL env vars.

set -euo pipefail

KC_URL="${KC_URL:-http://localhost:8080}"
KC_ADMIN="${KC_ADMIN:-admin}"
KC_ADMIN_PASSWORD="${KC_ADMIN_PASSWORD:-admin}"
REALM="planning-poker"
CLIENT_ID="planning-poker-spa"
MAX_RETRIES=30
RETRY_INTERVAL=5

# Platform admin credentials (from env — NOT hardcoded)
PP_ADMIN_USERNAME="${PP_ADMIN_USERNAME:-}"
PP_ADMIN_PASSWORD="${PP_ADMIN_PASSWORD:-}"
PP_ADMIN_EMAIL="${PP_ADMIN_EMAIL:-admin@planningpoker.local}"

echo "[keycloak-init] Waiting for Keycloak to be ready..."

for i in $(seq 1 $MAX_RETRIES); do
  if curl -sf "${KC_URL}/realms/${REALM}" > /dev/null 2>&1; then
    echo "[keycloak-init] Keycloak is ready."
    break
  fi
  if [ "$i" -eq "$MAX_RETRIES" ]; then
    echo "[keycloak-init] ERROR: Keycloak not ready after $((MAX_RETRIES * RETRY_INTERVAL))s. Exiting."
    exit 1
  fi
  sleep $RETRY_INTERVAL
done

# Get admin token
TOKEN=$(curl -sf -X POST "${KC_URL}/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli&grant_type=password&username=${KC_ADMIN}&password=${KC_ADMIN_PASSWORD}" \
  | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
  echo "[keycloak-init] ERROR: Failed to get admin token."
  exit 1
fi

# ================================================================
# 1. PROTOCOL MAPPERS
# ================================================================

CLIENT_UUID=$(curl -sf "${KC_URL}/admin/realms/${REALM}/clients?clientId=${CLIENT_ID}" \
  -H "Authorization: Bearer ${TOKEN}" \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)

if [ -z "$CLIENT_UUID" ]; then
  echo "[keycloak-init] ERROR: Client '${CLIENT_ID}' not found."
  exit 1
fi

echo "[keycloak-init] Client UUID: ${CLIENT_UUID}"

MAPPER_COUNT=$(curl -sf "${KC_URL}/admin/realms/${REALM}/clients/${CLIENT_UUID}/protocol-mappers/protocol/openid-connect" \
  -H "Authorization: Bearer ${TOKEN}" \
  | grep -o '"name"' | wc -l || echo "0")

MAPPER_COUNT=$(echo "$MAPPER_COUNT" | tr -d '[:space:]')
if [ "$MAPPER_COUNT" -ge 3 ] 2>/dev/null; then
  echo "[keycloak-init] Mappers already configured (${MAPPER_COUNT}). Skipping."
else
  echo "[keycloak-init] Adding protocol mappers..."

  curl -sf -X POST "${KC_URL}/admin/realms/${REALM}/clients/${CLIENT_UUID}/protocol-mappers/models" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "subject",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-property-mapper",
      "config": {
        "user.attribute": "id",
        "claim.name": "sub",
        "id.token.claim": "true",
        "access.token.claim": "true",
        "userinfo.token.claim": "true",
        "introspection.token.claim": "true",
        "jsonType.label": "String"
      }
    }' && echo " -> sub mapper added" || echo " -> sub mapper exists"

  curl -sf -X POST "${KC_URL}/admin/realms/${REALM}/clients/${CLIENT_UUID}/protocol-mappers/models" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "email",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-property-mapper",
      "config": {
        "user.attribute": "email",
        "claim.name": "email",
        "id.token.claim": "true",
        "access.token.claim": "true",
        "userinfo.token.claim": "true",
        "jsonType.label": "String"
      }
    }' && echo " -> email mapper added" || echo " -> email mapper exists"

  curl -sf -X POST "${KC_URL}/admin/realms/${REALM}/clients/${CLIENT_UUID}/protocol-mappers/models" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "preferred_username",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-property-mapper",
      "config": {
        "user.attribute": "username",
        "claim.name": "preferred_username",
        "id.token.claim": "true",
        "access.token.claim": "true",
        "userinfo.token.claim": "true",
        "jsonType.label": "String"
      }
    }' && echo " -> preferred_username mapper added" || echo " -> preferred_username mapper exists"
fi

# ================================================================
# 2. PLATFORM ADMIN USER
# ================================================================

if [ -z "$PP_ADMIN_USERNAME" ] || [ -z "$PP_ADMIN_PASSWORD" ]; then
  echo "[keycloak-init] PP_ADMIN_USERNAME or PP_ADMIN_PASSWORD not set — skipping admin user creation."
else
  # Check if user already exists
  EXISTING=$(curl -sf "${KC_URL}/admin/realms/${REALM}/users?username=${PP_ADMIN_USERNAME}&exact=true" \
    -H "Authorization: Bearer ${TOKEN}")

  if echo "$EXISTING" | grep -q '"id"' 2>/dev/null; then
    echo "[keycloak-init] Admin user '${PP_ADMIN_USERNAME}' already exists. Skipping."
  else
    echo "[keycloak-init] Creating admin user '${PP_ADMIN_USERNAME}'..."

    # Create user
    curl -sf -X POST "${KC_URL}/admin/realms/${REALM}/users" \
      -H "Authorization: Bearer ${TOKEN}" \
      -H "Content-Type: application/json" \
      -d "{
        \"username\": \"${PP_ADMIN_USERNAME}\",
        \"email\": \"${PP_ADMIN_EMAIL}\",
        \"emailVerified\": true,
        \"enabled\": true,
        \"firstName\": \"Platform\",
        \"lastName\": \"Admin\"
      }" && echo " -> user created" || { echo " -> ERROR creating user"; exit 1; }

    # Get the new user's ID
    USER_ID=$(curl -sf "${KC_URL}/admin/realms/${REALM}/users?username=${PP_ADMIN_USERNAME}&exact=true" \
      -H "Authorization: Bearer ${TOKEN}" \
      | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)

    # Set password
    curl -sf -X PUT "${KC_URL}/admin/realms/${REALM}/users/${USER_ID}/reset-password" \
      -H "Authorization: Bearer ${TOKEN}" \
      -H "Content-Type: application/json" \
      -d "{
        \"type\": \"password\",
        \"value\": \"${PP_ADMIN_PASSWORD}\",
        \"temporary\": false
      }" && echo " -> password set" || echo " -> ERROR setting password"

    # Assign ADMIN realm role
    ADMIN_ROLE_ID=$(curl -sf "${KC_URL}/admin/realms/${REALM}/roles/ADMIN" \
      -H "Authorization: Bearer ${TOKEN}" \
      | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')

    if [ -n "$ADMIN_ROLE_ID" ]; then
      curl -sf -X POST "${KC_URL}/admin/realms/${REALM}/users/${USER_ID}/role-mappings/realm" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" \
        -d "[{\"id\": \"${ADMIN_ROLE_ID}\", \"name\": \"ADMIN\"}]" \
        && echo " -> ADMIN role assigned" || echo " -> ERROR assigning role"
    fi

    echo "[keycloak-init] Admin user '${PP_ADMIN_USERNAME}' created successfully."
  fi
fi

echo "[keycloak-init] Done."
