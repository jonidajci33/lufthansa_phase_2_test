import api from '../lib/api';
import type { Page, NotificationResponse, UnreadCountResponse } from '../types/api';

export const fetchNotifications = async (offset = 0, limit = 20): Promise<Page<NotificationResponse>> => {
  const res = await api.get<Page<NotificationResponse>>('/notifications', {
    params: { offset, limit },
  });
  return res.data;
};

export const fetchUnreadCount = async (): Promise<UnreadCountResponse> => {
  const res = await api.get<UnreadCountResponse>('/notifications/unread-count');
  return res.data;
};

export const markAsRead = async (id: string): Promise<void> => {
  await api.put(`/notifications/${id}/read`);
};

export const markAllAsRead = async (): Promise<void> => {
  await api.put('/notifications/read-all');
};
