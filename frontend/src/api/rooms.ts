import api from '../lib/api';
import type { Room, Page, CreateRoomRequest, Participant } from '../types/api';

export async function fetchRooms(page = 0, size = 10): Promise<Page<Room>> {
  const { data } = await api.get<Page<Room>>('/rooms', {
    params: { offset: page * size, limit: size },
  });
  return data;
}

export async function fetchRoom(roomId: string): Promise<Room> {
  const { data } = await api.get<Room>(`/rooms/${roomId}`);
  return data;
}

export async function createRoom(request: CreateRoomRequest): Promise<Room> {
  const { data } = await api.post<Room>('/rooms', request);
  return data;
}

export async function joinRoom(shortCode: string): Promise<Room> {
  const { data } = await api.post<Room>(`/rooms/join/${shortCode}`);
  return data;
}

export async function fetchParticipants(roomId: string): Promise<Participant[]> {
  const { data } = await api.get(`/rooms/${roomId}/participants`);
  return Array.isArray(data) ? data : data.data ?? [];
}

// ---- Update room (moderator only) ----
export async function updateRoom(
  roomId: string,
  updates: { name?: string; description?: string; maxParticipants?: number },
): Promise<Room> {
  const { data } = await api.put<Room>(`/rooms/${roomId}`, updates);
  return data;
}

// ---- Invite user by email (moderator only) ----
export interface InviteRequest {
  email: string;
  type: 'EMAIL';
}

export interface InviteResponse {
  id: string;
  roomId: string;
  email: string;
  token: string;
  type: string;
  status: string;
  expiresAt: string;
}

export async function inviteUser(
  roomId: string,
  request: InviteRequest,
): Promise<InviteResponse> {
  const { data } = await api.post<InviteResponse>(
    `/rooms/${roomId}/invite`,
    request,
  );
  return data;
}

// ---- Generate share link (moderator only) ----
export interface ShareLinkResponse {
  shortCode: string;
  shareLink: string;
}

export async function generateShareLink(
  roomId: string,
): Promise<ShareLinkResponse> {
  const { data } = await api.post<ShareLinkResponse>(
    `/rooms/${roomId}/share-link`,
  );
  return data;
}

// ---- Accept invitation by token ----
export async function acceptInvitation(token: string): Promise<Room> {
  const { data } = await api.post<Room>(`/invitations/${token}/accept`);
  return data;
}

// ---- Remove participant (moderator only) ----
export async function removeParticipant(
  roomId: string,
  userId: string,
): Promise<void> {
  await api.delete(`/rooms/${roomId}/participants/${userId}`);
}
