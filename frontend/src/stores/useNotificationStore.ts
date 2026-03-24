import { create } from 'zustand';
import type { NotificationResponse } from '../types/api';
import {
  fetchNotifications,
  fetchUnreadCount,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
} from '../api/notifications';

interface NotificationState {
  notifications: NotificationResponse[];
  unreadCount: number;
  isDropdownOpen: boolean;
  isLoading: boolean;

  // Actions
  loadNotifications: () => Promise<void>;
  loadUnreadCount: () => Promise<void>;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  toggleDropdown: () => void;
  closeDropdown: () => void;

  // WebSocket-driven: increment count without polling
  incrementUnread: () => void;

  // Called on login to fetch initial count (single REST call, no polling)
  initialize: () => void;

  reset: () => void;

  // Keep startPolling/stopPolling as no-ops for backward compat with Header
  startPolling: () => void;
  stopPolling: () => void;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isDropdownOpen: false,
  isLoading: false,

  loadNotifications: async () => {
    set({ isLoading: true });
    try {
      const page = await fetchNotifications(0, 20);
      set({ notifications: page.data, isLoading: false });
    } catch {
      set({ isLoading: false });
    }
  },

  loadUnreadCount: async () => {
    try {
      const { count } = await fetchUnreadCount();
      set({ unreadCount: count });
    } catch {
      // Silently fail
    }
  },

  markAsRead: async (id: string) => {
    try {
      await apiMarkAsRead(id);
      set((state) => ({
        notifications: state.notifications.map((n) =>
          n.id === id ? { ...n, isRead: true } : n,
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      }));
    } catch {
      // Silently fail
    }
  },

  markAllAsRead: async () => {
    try {
      await apiMarkAllAsRead();
      set((state) => ({
        notifications: state.notifications.map((n) => ({ ...n, isRead: true })),
        unreadCount: 0,
      }));
    } catch {
      // Silently fail
    }
  },

  toggleDropdown: () => {
    const isOpen = !get().isDropdownOpen;
    set({ isDropdownOpen: isOpen });
    if (isOpen) {
      get().loadNotifications();
      get().loadUnreadCount();
    }
  },

  closeDropdown: () => set({ isDropdownOpen: false }),

  // Called from WebSocket handlers when a relevant event happens
  incrementUnread: () =>
    set((state) => ({ unreadCount: state.unreadCount + 1 })),

  // Fetch initial count once on login — no recurring polling
  initialize: () => {
    get().loadUnreadCount();
  },

  // No-ops for backward compatibility with Header.tsx
  startPolling: () => {
    get().loadUnreadCount();
  },
  stopPolling: () => {},

  reset: () => {
    set({
      notifications: [],
      unreadCount: 0,
      isDropdownOpen: false,
      isLoading: false,
    });
  },
}));
