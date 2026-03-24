import api from '../lib/api';
import type { Story, CreateStoryRequest, ReorderStoriesRequest } from '../types/api';

export async function fetchStories(roomId: string): Promise<Story[]> {
  const { data } = await api.get(`/rooms/${roomId}/stories`);
  return Array.isArray(data) ? data : data.data ?? [];
}

export async function createStory(request: CreateStoryRequest): Promise<Story> {
  const { data } = await api.post<Story>('/stories', request);
  return data;
}

export async function updateStory(
  storyId: string,
  updates: Partial<Pick<Story, 'title' | 'description'>>,
): Promise<Story> {
  const { data } = await api.put<Story>(`/stories/${storyId}`, updates);
  return data;
}

export async function deleteStory(storyId: string): Promise<void> {
  await api.delete(`/stories/${storyId}`);
}

export async function reorderStories(
  roomId: string,
  request: ReorderStoriesRequest,
): Promise<void> {
  await api.put(`/rooms/${roomId}/stories/reorder`, request);
}
