import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';
import { exchangeCodeForTokens } from '../lib/sso';

/**
 * Handles the OAuth2 callback from Keycloak after SSO login.
 * Exchanges the authorization code for tokens, stores them, and redirects to /rooms.
 */
export function AuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setTokens = useAuthStore((s) => s.setTokens);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = searchParams.get('code');
    const errorParam = searchParams.get('error');

    if (errorParam) {
      setError(`Login failed: ${searchParams.get('error_description') || errorParam}`);
      return;
    }

    if (!code) {
      setError('No authorization code received');
      return;
    }

    exchangeCodeForTokens(code)
      .then(({ accessToken, refreshToken }) => {
        // Store tokens
        localStorage.setItem('pp_token', accessToken);
        localStorage.setItem('pp_refresh', refreshToken);
        setTokens(accessToken, refreshToken);

        // Extract roles and set auth state
        const extractRoles = (token: string): string[] => {
          try {
            const payload = JSON.parse(atob(token.split('.')[1] ?? ''));
            return Array.isArray(payload.realm_roles) ? payload.realm_roles : [];
          } catch {
            return [];
          }
        };

        const roles = extractRoles(accessToken);
        useAuthStore.setState({
          isAuthenticated: true,
          isAuthReady: true,
          isAdmin: roles.includes('ADMIN'),
        });

        // Redirect to saved return URL (invitation link) or default to rooms
        const returnTo = sessionStorage.getItem('pp_return_to') || '/rooms';
        sessionStorage.removeItem('pp_return_to');
        navigate(returnTo, { replace: true });
      })
      .catch((err) => {
        setError(err.message || 'Failed to complete login');
      });
  }, [searchParams, navigate, setTokens]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface px-4">
        <div className="text-center">
          <p className="text-red-400 text-sm mb-4">{error}</p>
          <a href="/" className="text-sm text-text-muted hover:text-white transition-colors">
            Back to login
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface">
      <div className="flex flex-col items-center gap-4">
        <div className="w-8 h-8 border-2 border-white border-t-transparent rounded-full animate-spin" />
        <span className="text-text-secondary text-sm">Completing sign in...</span>
      </div>
    </div>
  );
}
