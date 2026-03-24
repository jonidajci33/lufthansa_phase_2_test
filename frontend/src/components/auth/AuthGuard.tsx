import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';
import type { ReactNode } from 'react';

interface AuthGuardProps {
  children: ReactNode;
}

/**
 * Protected route wrapper.
 *
 * On first render, triggers auth initialization (token validation/refresh).
 * While initialization is in progress, shows a loading spinner.
 * Once complete:
 *   - If authenticated: renders children
 *   - If not authenticated: redirects to login
 *
 * This guarantees that no child component (RoomsPage, RoomPage, etc.) mounts
 * until the auth state is confirmed, eliminating the race condition between
 * auth initialization and API calls.
 */
export function AuthGuard({ children }: AuthGuardProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const isAuthReady = useAuthStore((s) => s.isAuthReady);
  const isLoading = useAuthStore((s) => s.isLoading);
  const initializeAuth = useAuthStore((s) => s.initializeAuth);
  const location = useLocation();

  // Trigger auth initialization on mount.
  // initializeAuth is idempotent — safe to call multiple times.
  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  // Show loading spinner while:
  // 1. Auth initialization is in progress (isAuthReady === false), OR
  // 2. A login/register action is in progress (isLoading === true)
  if (!isAuthReady || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface">
        <div className="flex flex-col items-center gap-4">
          <div className="w-8 h-8 border-2 border-white border-t-transparent rounded-full animate-spin" />
          <span className="text-text-secondary text-sm">
            {!isAuthReady ? 'Verifying session...' : 'Loading...'}
          </span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Save the URL the user was trying to access so we can redirect back after login
    const returnTo = location.pathname + location.search;
    if (returnTo !== '/' && returnTo !== '/rooms') {
      sessionStorage.setItem('pp_return_to', returnTo);
    }
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
