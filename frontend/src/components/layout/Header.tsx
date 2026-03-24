import { useEffect } from 'react';
import { useAuthStore } from '../../stores/useAuthStore';
import { useWebSocketStore } from '../../stores/useWebSocketStore';
import { useNotificationStore } from '../../stores/useNotificationStore';
import { NotificationBell } from './NotificationBell';
import { Link, useLocation } from 'react-router-dom';

export function Header() {
  const { user, logout, isAdmin, isAuthenticated } = useAuthStore();
  const wsStatus = useWebSocketStore((s) => s.status);
  const location = useLocation();
  const startPolling = useNotificationStore((s) => s.startPolling);
  const stopPolling = useNotificationStore((s) => s.stopPolling);
  const resetNotifications = useNotificationStore((s) => s.reset);

  // Start/stop notification polling based on auth state
  useEffect(() => {
    if (isAuthenticated) {
      startPolling();
    } else {
      resetNotifications();
    }
    return () => {
      stopPolling();
    };
  }, [isAuthenticated, startPolling, stopPolling, resetNotifications]);

  const initial = user?.firstName?.charAt(0)?.toUpperCase() ?? user?.username?.charAt(0)?.toUpperCase() ?? '?';
  const isAdminRoute = location.pathname.startsWith('/admin');

  return (
    <header className="h-14 border-b border-border bg-surface-secondary flex items-center justify-between px-6">
      {/* Logo + Nav */}
      <div className="flex items-center gap-6">
        <Link to="/rooms" className="flex items-center gap-3 hover:opacity-80 transition-opacity">
          <span className="text-xl font-bold tracking-tight text-white">
            Planning Poker
          </span>
          {wsStatus === 'CONNECTED' && (
            <span className="w-2 h-2 rounded-full bg-white animate-pulse" title="Connected" />
          )}
          {wsStatus === 'ERROR' && (
            <span className="w-2 h-2 rounded-full bg-text-muted" title="Disconnected" />
          )}
        </Link>

        {/* Nav links */}
        <nav className="flex items-center gap-4 text-sm">
          <Link
            to="/rooms"
            className={`transition-colors ${!isAdminRoute ? 'text-white' : 'text-text-muted hover:text-white'}`}
          >
            Rooms
          </Link>
          {isAdmin && (
            <>
              <span className="text-border">|</span>
              <Link
                to="/admin/users"
                className={`transition-colors ${location.pathname === '/admin/users' ? 'text-white' : 'text-text-muted hover:text-white'}`}
              >
                Users
              </Link>
              <Link
                to="/admin/rooms"
                className={`transition-colors ${location.pathname === '/admin/rooms' ? 'text-white' : 'text-text-muted hover:text-white'}`}
              >
                All Rooms
              </Link>
              <Link
                to="/admin/stories"
                className={`transition-colors ${location.pathname === '/admin/stories' ? 'text-white' : 'text-text-muted hover:text-white'}`}
              >
                Stories
              </Link>
              <Link
                to="/admin/audit"
                className={`transition-colors ${location.pathname === '/admin/audit' ? 'text-white' : 'text-text-muted hover:text-white'}`}
              >
                Audit
              </Link>
            </>
          )}
        </nav>
      </div>

      {/* User menu */}
      {user && (
        <div className="flex items-center gap-4">
          <NotificationBell />
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-white text-black flex items-center justify-center font-bold text-sm">
              {initial}
            </div>
            <span className="text-sm text-text-secondary hidden sm:inline">
              {user.username}
            </span>
            {isAdmin && (
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-white/10 text-white">
                ADMIN
              </span>
            )}
          </div>
          <button
            onClick={() => {
              resetNotifications();
              logout();
            }}
            className="text-sm text-text-muted hover:text-white transition-colors"
          >
            Logout
          </button>
        </div>
      )}
    </header>
  );
}
