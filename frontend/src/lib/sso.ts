/**
 * SSO / OIDC Authorization Code + PKCE utilities.
 * Handles redirect to Keycloak and token exchange on callback.
 */

const KEYCLOAK_URL = '/auth/realms/planning-poker/protocol/openid-connect';
const CLIENT_ID = 'planning-poker-spa';
const REDIRECT_URI = `${window.location.origin}/auth/callback`;

// ── PKCE helpers ────────────────────────────────────────────────

function generateRandomString(length: number): string {
  const array = new Uint8Array(length);
  crypto.getRandomValues(array);
  return Array.from(array, (b) => b.toString(16).padStart(2, '0')).join('').substring(0, length);
}

async function sha256(plain: string): Promise<ArrayBuffer> {
  const encoder = new TextEncoder();
  return crypto.subtle.digest('SHA-256', encoder.encode(plain));
}

function base64UrlEncode(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  bytes.forEach((b) => (binary += String.fromCharCode(b)));
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

// ── Public API ──────────────────────────────────────────────────

/**
 * Redirect the browser to Keycloak's authorization endpoint.
 * @param idpHint - 'google' | 'facebook' | undefined (shows Keycloak login page)
 */
export async function startSsoLogin(idpHint?: 'google' | 'facebook') {
  const codeVerifier = generateRandomString(64);
  const codeChallenge = base64UrlEncode(await sha256(codeVerifier));

  // Store verifier for the callback
  sessionStorage.setItem('pkce_verifier', codeVerifier);

  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    response_type: 'code',
    scope: 'openid',
    code_challenge: codeChallenge,
    code_challenge_method: 'S256',
  });

  if (idpHint) {
    params.set('kc_idp_hint', idpHint);
  }

  // Redirect to Keycloak — goes through frontend nginx proxy → gateway → keycloak
  window.location.href = `${KEYCLOAK_URL}/auth?${params.toString()}`;
}

/**
 * Exchange the authorization code for tokens.
 * Called from the /auth/callback page.
 */
export async function exchangeCodeForTokens(code: string): Promise<{
  accessToken: string;
  refreshToken: string;
}> {
  const codeVerifier = sessionStorage.getItem('pkce_verifier');
  sessionStorage.removeItem('pkce_verifier');

  if (!codeVerifier) {
    throw new Error('PKCE verifier not found — login flow may have been interrupted');
  }

  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    client_id: CLIENT_ID,
    code,
    redirect_uri: REDIRECT_URI,
    code_verifier: codeVerifier,
  });

  const res = await fetch(`${KEYCLOAK_URL}/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString(),
  });

  if (!res.ok) {
    throw new Error(`Token exchange failed: ${res.status}`);
  }

  const data = await res.json();
  return {
    accessToken: data.access_token,
    refreshToken: data.refresh_token,
  };
}
