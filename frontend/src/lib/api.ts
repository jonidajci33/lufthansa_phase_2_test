import axios, { type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../stores/useAuthStore';
// Note: useAuthStore is still used in the response interceptor below

// Extend config to track retry
interface RetryableConfig extends InternalAxiosRequestConfig {
  _retried?: boolean;
}

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor: attach JWT from localStorage.
 * Reads directly from localStorage to avoid any Zustand timing issues.
 * If the token is expired, the response interceptor handles refresh on 401.
 */
const PUBLIC_PATHS = ['/auth/login', '/auth/register'];

api.interceptors.request.use((config) => {
  const isPublic = PUBLIC_PATHS.some((p) => config.url?.startsWith(p));
  if (!isPublic) {
    const token = localStorage.getItem('pp_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

/**
 * Response interceptor: handle 401 by attempting token refresh, then logout.
 */
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!axios.isAxiosError(error) || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    const config = error.config as RetryableConfig | undefined;
    if (!config) return Promise.reject(error);

    const { refreshToken, setTokens, logout } = useAuthStore.getState();

    // Try refresh once
    if (refreshToken && !config._retried) {
      try {
        const res = await axios.post('/api/v1/auth/refresh', { refreshToken });
        const { accessToken: newAccess, refreshToken: newRefresh } = res.data;
        setTokens(newAccess, newRefresh);

        // Retry original request with new token
        config._retried = true;
        config.headers.Authorization = `Bearer ${newAccess}`;
        return api.request(config);
      } catch {
        logout();
      }
    } else {
      logout();
    }

    return Promise.reject(error);
  },
);

export default api;
