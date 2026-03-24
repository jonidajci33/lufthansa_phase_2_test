// ---- Pagination ----

export interface Page<T> {
  data: T[];
  meta: {
    total: number;
    limit: number;
    offset: number;
    hasNext: boolean;
  };
}

// ---- WebSocket ----

export interface WsEnvelope<T> {
  type: string;
  roomId: string;
  payload: T;
  timestamp: string;
}

// ---- Auth ----

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  displayName?: string;
  avatarUrl?: string;
  isActive?: boolean;
  roles?: string[];
  createdAt?: string;
}

// ---- Audit ----

export interface AuditEntry {
  id: number;
  entityType: string;
  entityId: string;
  operation: string;
  userId: string;
  timestamp: string;
  previousState: Record<string, unknown> | null;
  newState: Record<string, unknown> | null;
}

// ---- Deck Types ----

export interface DeckValue {
  id: string;
  label: string;
  numericValue: number | null;
  sortOrder: number;
}

export interface DeckType {
  id: string;
  name: string;
  category: string;
  isSystem: boolean;
  values: DeckValue[];
}

export interface CreateDeckTypeRequest {
  name: string;
  values: { label: string; numericValue: number | null }[];
}

// ---- Rooms ----

export type RoomStatus = 'ACTIVE' | 'CLOSED' | 'ARCHIVED';

export interface Room {
  id: string;
  name: string;
  description: string;
  shortCode: string;
  deckType: DeckType;
  maxParticipants: number;
  autoReveal: boolean;
  timerSeconds: number | null;
  status: RoomStatus;
  moderatorId: string;
  participantCount: number;
  createdAt: string;
}

export interface CreateRoomRequest {
  name: string;
  description?: string;
  deckTypeId: string;
  maxParticipants?: number;
  autoReveal?: boolean;
  timerSeconds?: number | null;
}

// ---- Participants ----

export type ParticipantRole = 'MODERATOR' | 'VOTER' | 'OBSERVER';

export interface Participant {
  id: string;
  userId: string;
  username: string;
  role: ParticipantRole;
  connected: boolean;
  joinedAt: string;
}

// ---- Stories ----

export type StoryStatus = 'PENDING' | 'VOTING' | 'VOTED' | 'REVEALED' | 'SKIPPED';

export interface Story {
  id: string;
  roomId: string;
  title: string;
  description: string;
  status: StoryStatus;
  finalEstimate: string | null;
  finalScore?: number | null;
  consensusReached?: boolean;
  voteCount?: number;
  sortOrder: number;
  createdAt: string;
}

export interface CreateStoryRequest {
  roomId: string;
  title: string;
  description?: string;
}

export interface ReorderStoriesRequest {
  storyIds: string[];
}

// ---- Voting ----

export interface Vote {
  id: string;
  storyId: string;
  participantId: string;
  username: string;
  value: string | null;
  votedAt: string;
}

export interface CastVoteRequest {
  value: string;
}

export interface VotingResult {
  storyId: string;
  averageScore: number | null;
  totalVotes: number;
  consensusReached: boolean;
  // REST-only fields (from GET /stories/{id}/results)
  votes?: Vote[];
  distribution?: Record<string, number>;
}

// ---- WebSocket Payloads ----

export interface VoteCountUpdate {
  storyId: string;
  voteCount: number;
}

export interface RoomStateUpdate {
  type: 'PARTICIPANT_JOINED' | 'PARTICIPANT_LEFT' | 'TIMER_STARTED' | 'TIMER_EXPIRED' | 'ROOM_CLOSED';
  participant?: Participant;
  timerSeconds?: number;
}

export interface StoryUpdate {
  type: 'STORY_ADDED' | 'STORY_UPDATED' | 'STORY_REORDERED' | 'STORY_DELETED' | 'VOTING_STARTED' | 'voting-started' | 'added' | 'updated' | 'deleted';
  story?: Story & { storyId?: string };
  stories?: Story[];
}

// ---- Notifications ----

export type NotificationTypeName = 'WELCOME' | 'INVITATION' | 'VOTING_STARTED' | 'VOTING_FINISHED' | 'SYSTEM';

export interface NotificationResponse {
  id: string;
  userId: string;
  type: NotificationTypeName;
  title: string;
  message: string;
  metadata?: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationMetadata {
  roomId?: string;
  storyId?: string;
  actionUrl?: string;
}

export interface UnreadCountResponse {
  count: number;
}
