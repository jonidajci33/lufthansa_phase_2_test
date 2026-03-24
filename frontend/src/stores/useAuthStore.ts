import { create } from 'zustand';
import type { UserProfile } from '../types/api';

// ── JWT expiry helper ────────────────────────────────────────────
// Decodes the payload of a JWT without a library. Returns the `exp`
// claim (seconds since epoch) or null if unparseable.
function getTokenExp(token: string): number | null {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) return null;
    const payload = JSON.parse(atob(payloadBase64));
    return typeof payload.exp === 'number' ? payload.exp : null;
  } catch {
    return null;
  }
}

/**
 * Returns true if the token will expire within `bufferSeconds`.
 * Used by the request interceptor for proactive refresh.
 */
export function isTokenExpiringSoon(token: string | null, bufferSeconds = 60): boolean {
  if (!token) return true;
  const exp = getTokenExp(token);
  if (exp === null) return false; // Can't decode → let backend decide
  return Date.now() / 1000 >= exp - bufferSeconds;
}

/** Extract realm_roles from JWT payload */
function extractRolesFromToken(token: string): string[] {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) return [];
    const payload = JSON.parse(atob(payloadBase64));
    return Array.isArray(payload.realm_roles) ? payload.realm_roles : [];
  } catch {
    return [];
  }
}

interface AuthState {
  isAuthenticated: boolean;
  isAuthReady: boolean;
  isLoading: boolean;
  isAdmin: boolean;
  token: string | null;
  refreshToken: string | null;
  user: UserProfile | null;
  error: string | null;

  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string, firstName: string, lastName: string) => Promise<void>;
  logout: () => void;
  setTokens: (access: string, refresh: string) => void;
  initializeAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  isAuthenticated: false,
  isAuthReady: false,
  isLoading: false,
  isAdmin: false,
  // localStorage smoke tests:
  // - getItem returns null if the key doesn't exist
  // - getItem returns the string value if the key exists (never undefined)
  // - setItem/removeItem are synchronous and never throw in modern browsers
  token: localStorage.getItem('pp_token'),
  refreshToken: localStorage.getItem('pp_refresh'),
  user: null,
  error: null,

  login: async (username: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      // Use the api instance so interceptors are active.
      // Import dynamically to avoid circular dependency
      // (api.ts imports useAuthStore, useAuthStore would import api).
      const { default: api } = await import('../lib/api');
      const res = await api.post('/auth/login', { username, password });
      const { accessToken, refreshToken, user } = res.data;

      localStorage.setItem('pp_token', accessToken);
      localStorage.setItem('pp_refresh', refreshToken);
      const roles = extractRolesFromToken(accessToken);

      set({
        isAuthenticated: true,
        isAuthReady: true,
        isLoading: false,
        isAdmin: roles.includes('ADMIN'),
        token: accessToken,
        refreshToken,
        user,
        error: null,
      });
    } catch (err) {
      const axios = (await import('axios')).default;
      const message =
        axios.isAxiosError(err) && err.response?.data?.message
          ? err.response.data.message
          : 'Login failed. Check your credentials.';
      set({ isLoading: false, error: message });
      throw err;
    }
  },

  register: async (username: string, email: string, password: string, firstName: string, lastName: string) => {
    set({ isLoading: true, error: null });
    try {
      const { default: api } = await import('../lib/api');
      const res = await api.post('/auth/register', {
        username,
        email,
        password,
        firstName,
        lastName,
      });
      const { accessToken, refreshToken, user } = res.data;

      localStorage.setItem('pp_token', accessToken);
      localStorage.setItem('pp_refresh', refreshToken);
      const roles = extractRolesFromToken(accessToken);

      set({
        isAuthenticated: true,
        isAuthReady: true,
        isLoading: false,
        isAdmin: roles.includes('ADMIN'),
        token: accessToken,
        refreshToken,
        user,
        error: null,
      });
    } catch (err) {
      const axios = (await import('axios')).default;
      const message =
        axios.isAxiosError(err) && err.response?.data?.message
          ? err.response.data.message
          : 'Registration failed. Try a different username.';
      set({ isLoading: false, error: message });
      throw err;
    }
  },

  logout: () => {
    const token = get().token;
    localStorage.removeItem('pp_token');
    localStorage.removeItem('pp_refresh');
    set({
      isAuthenticated: false,
      token: null,
      refreshToken: null,
      user: null,
    });
    // Notify backend (fire-and-forget)
    if (token) {
      import('axios').then(({ default: axios }) => {
        axios.post('/api/v1/auth/logout', null, {
          headers: { Authorization: `Bearer ${token}` },
        }).catch(() => {});
      });
    }
  },

  setTokens: (access: string, refresh: string) => {
    localStorage.setItem('pp_token', access);
    localStorage.setItem('pp_refresh', refresh);
    set({ token: access, refreshToken: refresh });
  },

  /**
   * Initialize auth state on app startup.
   * If a token exists in localStorage, call /auth/me to restore the user
   * profile. If the token is expired, the api interceptor handles refresh.
   */
  initializeAuth: async () => {
    if (get().isAuthReady) return;

    const token = get().token;

    if (!token) {
      set({ isAuthenticated: false, isAuthReady: true });
      return;
    }

    const roles = extractRolesFromToken(token);
    // Set authenticated immediately so AuthGuard doesn't redirect
    set({ isAuthenticated: true, isAdmin: roles.includes('ADMIN') });

    try {
      const { default: api } = await import('../lib/api');
      const res = await api.get('/auth/me');
      set({
        user: res.data,
        isAuthenticated: true,
        isAuthReady: true,
        isAdmin: roles.includes('ADMIN'),
      });
    } catch {
      // Token invalid or expired and refresh failed — clear auth
      localStorage.removeItem('pp_token');
      localStorage.removeItem('pp_refresh');
      set({
        isAuthenticated: false,
        isAuthReady: true,
        isAdmin: false,
        token: null,
        refreshToken: null,
        user: null,
      });
    }
  },
}));

// NOTE: No module-level checkSession() call. Initialization is triggered
// by AuthGuard on mount, ensuring the auth gate is in place before any
// authenticated routes render.
