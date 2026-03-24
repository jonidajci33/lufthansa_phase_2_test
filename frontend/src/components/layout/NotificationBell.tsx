import { useRef } from 'react';
import { useNotificationStore } from '../../stores/useNotificationStore';
import { NotificationPanel } from './NotificationPanel';

/**
 * Bell icon with unread badge. Toggles the notification dropdown on click.
 */
export function NotificationBell() {
  const unreadCount = useNotificationStore((s) => s.unreadCount);
  const isDropdownOpen = useNotificationStore((s) => s.isDropdownOpen);
  const toggleDropdown = useNotificationStore((s) => s.toggleDropdown);
  const containerRef = useRef<HTMLDivElement>(null);

  return (
    <div ref={containerRef} className="relative">
      <button
        onClick={toggleDropdown}
        className="relative p-1.5 rounded-md text-text-muted hover:text-white transition-colors"
        aria-label={unreadCount > 0 ? `${unreadCount} unread notifications` : 'Notifications'}
        aria-expanded={isDropdownOpen}
        aria-haspopup="true"
      >
        {/* Bell SVG icon */}
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>

        {/* Unread badge */}
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-[10px] font-bold text-white bg-red-500 rounded-full">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isDropdownOpen && <NotificationPanel containerRef={containerRef} />}
    </div>
  );
}
