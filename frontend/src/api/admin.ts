import api from '../lib/api';
import type { UserProfile, Story, AuditEntry, Page } from '../types/api';

// ---- User Management ----

export async function fetchUsers(offset = 0, limit = 20): Promise<Page<UserProfile>> {
  const { data } = await api.get('/users', { params: { offset, limit } });
  return data;
}

export async function updateUser(keycloakId: string, updates: { displayName?: string; avatarUrl?: string }): Promise<UserProfile> {
  const { data } = await api.put<UserProfile>(`/users/${keycloakId}`, updates);
  return data;
}

export async function deactivateUser(keycloakId: string): Promise<void> {
  await api.delete(`/users/${keycloakId}`);
}

// ---- Story Oversight ----

export async function fetchAllStories(offset = 0, limit = 20): Promise<Page<Story>> {
  const { data } = await api.get('/stories', { params: { offset, limit } });
  return data;
}

// ---- Audit Trail ----

export async function fetchAuditEntries(params: {
  offset?: number;
  limit?: number;
  entityType?: string;
  operation?: string;
  userId?: string;
  from?: string;
  to?: string;
}): Promise<Page<AuditEntry>> {
  const { data } = await api.get('/audit', { params: { offset: 0, limit: 20, ...params } });
  return data;
}
