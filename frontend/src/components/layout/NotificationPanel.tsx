import { useEffect, useCallback, type RefObject } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotificationStore } from '../../stores/useNotificationStore';
import type { NotificationResponse, NotificationMetadata } from '../../types/api';

interface NotificationPanelProps {
  containerRef: RefObject<HTMLDivElement | null>;
}

/**
 * Dropdown panel listing recent notifications.
 * Closes on click outside or Escape key.
 */
export function NotificationPanel({ containerRef }: NotificationPanelProps) {
  const notifications = useNotificationStore((s) => s.notifications);
  const isLoading = useNotificationStore((s) => s.isLoading);
  const markAsRead = useNotificationStore((s) => s.markAsRead);
  const markAllAsRead = useNotificationStore((s) => s.markAllAsRead);
  const closeDropdown = useNotificationStore((s) => s.closeDropdown);
  const navigate = useNavigate();

  // Close on click outside
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        closeDropdown();
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [containerRef, closeDropdown]);

  // Close on Escape key
  useEffect(() => {
    function handleEscape(e: KeyboardEvent) {
      if (e.key === 'Escape') {
        closeDropdown();
      }
    }
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [closeDropdown]);

  const handleNotificationClick = useCallback(
    (notification: NotificationResponse) => {
      if (!notification.isRead) {
        markAsRead(notification.id);
      }

      // Parse metadata for navigation
      if (notification.metadata) {
        try {
          const meta: NotificationMetadata = JSON.parse(notification.metadata);
          if (meta.actionUrl) {
            navigate(meta.actionUrl);
            closeDropdown();
            return;
          }
          if (meta.roomId) {
            navigate(`/rooms/${meta.roomId}`);
            closeDropdown();
            return;
          }
        } catch {
          // Invalid JSON — ignore
        }
      }
    },
    [markAsRead, navigate, closeDropdown],
  );

  return (
    <div className="absolute right-0 top-full mt-2 w-80 max-h-[400px] bg-surface-secondary border border-border rounded-lg shadow-xl z-50 flex flex-col overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-border">
        <h3 className="text-sm font-semibold text-white">Notifications</h3>
        <button
          onClick={markAllAsRead}
          className="text-xs text-text-muted hover:text-white transition-colors"
        >
          Mark all read
        </button>
      </div>

      {/* Notification list */}
      <div className="flex-1 overflow-y-auto">
        {isLoading && notifications.length === 0 && (
          <div className="px-4 py-6 text-center text-sm text-text-muted">Loading...</div>
        )}

        {!isLoading && notifications.length === 0 && (
          <div className="px-4 py-6 text-center text-sm text-text-muted">No notifications yet</div>
        )}

        {notifications.map((notification) => (
          <button
            key={notification.id}
            onClick={() => handleNotificationClick(notification)}
            className={`w-full text-left px-4 py-3 border-b border-border/50 hover:bg-white/5 transition-colors ${
              !notification.isRead ? 'bg-white/[0.03]' : ''
            }`}
          >
            <div className="flex items-start gap-2">
              {!notification.isRead && (
                <span className="mt-1.5 w-2 h-2 rounded-full bg-blue-400 flex-shrink-0" />
              )}
              <div className={`flex-1 min-w-0 ${notification.isRead ? 'ml-4' : ''}`}>
                <p className={`text-sm truncate ${notification.isRead ? 'text-text-muted' : 'text-white font-medium'}`}>
                  {notification.title}
                </p>
                <p className="text-xs text-text-muted mt-0.5 line-clamp-2">{notification.message}</p>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-[10px] text-text-muted">{formatRelativeTime(notification.createdAt)}</span>
                  {notification.type === 'VOTING_STARTED' && notification.metadata && (
                    <span className="text-[10px] text-blue-400 font-medium">Join Room</span>
                  )}
                </div>
              </div>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

/**
 * Formats an ISO timestamp into a relative time string like "2m ago", "1h ago", "3d ago".
 */
function formatRelativeTime(isoDate: string): string {
  const now = Date.now();
  const then = new Date(isoDate).getTime();
  const diffMs = now - then;
  const diffSec = Math.floor(diffMs / 1000);

  if (diffSec < 60) return 'just now';
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}h ago`;
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay < 7) return `${diffDay}d ago`;
  const diffWeek = Math.floor(diffDay / 7);
  return `${diffWeek}w ago`;
}
