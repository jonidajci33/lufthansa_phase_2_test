import { create } from 'zustand';
import type { Room, Participant, Story, CreateRoomRequest } from '../types/api';
import { fetchRoom, fetchRooms, createRoom as apiCreateRoom, joinRoom as apiJoinRoom, updateRoom as apiUpdateRoom, removeParticipant as apiRemoveParticipant } from '../api/rooms';
import { fetchStories } from '../api/stories';

interface RoomState {
  // Room list
  rooms: Room[];
  totalPages: number;
  currentPage: number;
  isLoadingRooms: boolean;

  // Active room
  activeRoom: Room | null;
  participants: Participant[];
  stories: Story[];
  activeStoryId: string | null;
  isLoadingRoom: boolean;

  // Actions - room list
  loadRooms: (page?: number) => Promise<void>;
  createRoom: (request: CreateRoomRequest) => Promise<Room>;
  joinRoom: (shortCode: string) => Promise<Room>;

  // Actions - active room
  loadRoom: (roomId: string) => Promise<void>;
  loadStories: (roomId: string) => Promise<void>;
  setActiveStory: (storyId: string | null) => void;

  // Moderator actions
  updateRoom: (roomId: string, updates: { name?: string; description?: string }) => Promise<void>;
  removeParticipantByUserId: (roomId: string, userId: string) => Promise<void>;

  // WebSocket-driven updates
  addParticipant: (participant: Participant) => void;
  removeParticipant: (participantId: string) => void;
  updateStories: (stories: Story[]) => void;
  updateStory: (story: Story) => void;
  addStory: (story: Story) => void;
  removeStory: (storyId: string) => void;
  clearActiveRoom: () => void;
}

export const useRoomStore = create<RoomState>((set, get) => ({
  rooms: [],
  totalPages: 0,
  currentPage: 0,
  isLoadingRooms: false,

  activeRoom: null,
  participants: [],
  stories: [],
  activeStoryId: null,
  isLoadingRoom: false,

  loadRooms: async (page = 0) => {
    set({ isLoadingRooms: true });
    try {
      const data = await fetchRooms(page);
      const totalPages = Math.ceil(data.meta.total / data.meta.limit) || 1;
      set({
        rooms: data.data,
        totalPages,
        currentPage: Math.floor(data.meta.offset / data.meta.limit),
        isLoadingRooms: false,
      });
    } catch {
      set({ isLoadingRooms: false });
    }
  },

  createRoom: async (request) => {
    const room = await apiCreateRoom(request);
    set((state) => ({ rooms: [room, ...state.rooms] }));
    return room;
  },

  joinRoom: async (shortCode) => {
    const room = await apiJoinRoom(shortCode);
    return room;
  },

  loadRoom: async (roomId) => {
    set({ isLoadingRoom: true });
    try {
      const room = await fetchRoom(roomId);
      set({ activeRoom: room, isLoadingRoom: false });
      await get().loadStories(roomId);
    } catch {
      set({ isLoadingRoom: false });
    }
  },

  loadStories: async (roomId) => {
    try {
      const stories = await fetchStories(roomId);
      set({ stories });

      // Auto-select the active voting story, or first pending
      const votingStory = stories.find((s) => s.status === 'VOTING');
      const pendingStory = stories.find((s) => s.status === 'PENDING');
      set({ activeStoryId: votingStory?.id ?? pendingStory?.id ?? null });
    } catch {
      // Silently fail - stories will be empty
    }
  },

  setActiveStory: (storyId) => set({ activeStoryId: storyId }),

  updateRoom: async (roomId, updates) => {
    const updatedRoom = await apiUpdateRoom(roomId, updates);
    set({ activeRoom: updatedRoom });
  },

  removeParticipantByUserId: async (roomId, userId) => {
    await apiRemoveParticipant(roomId, userId);
    // Remove from local state — filter by userId, not participant.id
    set((state) => ({
      participants: state.participants.filter((p) => p.userId !== userId),
    }));
  },

  addParticipant: (participant) =>
    set((state) => ({
      participants: [...state.participants.filter((p) => p.id !== participant.id), participant],
    })),

  removeParticipant: (participantId) =>
    set((state) => ({
      participants: state.participants.filter((p) => p.id !== participantId),
    })),

  updateStories: (stories) => set({ stories }),

  updateStory: (story) =>
    set((state) => ({
      stories: state.stories.map((s) => (s.id === story.id ? story : s)),
    })),

  addStory: (story) =>
    set((state) => ({
      stories: state.stories.some((s) => s.id === story.id)
        ? state.stories
        : [...state.stories, story],
    })),

  removeStory: (storyId) =>
    set((state) => ({
      stories: state.stories.filter((s) => s.id !== storyId),
      activeStoryId: state.activeStoryId === storyId ? null : state.activeStoryId,
    })),

  clearActiveRoom: () =>
    set({
      activeRoom: null,
      participants: [],
      stories: [],
      activeStoryId: null,
    }),
}));
