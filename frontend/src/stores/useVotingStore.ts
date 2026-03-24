import { create } from 'zustand';
import type { VotingResult, VoteCountUpdate } from '../types/api';
import { castVote as apiCastVote, fetchResults } from '../api/voting';

interface VotingState {
  // Current voting session
  selectedCard: string | null;
  hasVoted: boolean;
  votedCount: number;
  totalParticipants: number;

  // Results
  results: VotingResult | null;
  isRevealed: boolean;
  isRevealAnimating: boolean;

  // Actions
  selectCard: (value: string) => void;
  submitVote: (storyId: string, value: string) => Promise<void>;
  changeVote: (storyId: string, value: string) => Promise<void>;
  loadResults: (storyId: string) => Promise<void>;

  // WebSocket-driven updates
  updateVoteCount: (update: VoteCountUpdate) => void;
  setResults: (results: VotingResult) => void;
  startRevealAnimation: () => void;
  finishRevealAnimation: () => void;

  // Reset
  resetVoting: () => void;
}

export const useVotingStore = create<VotingState>((set, get) => ({
  selectedCard: null,
  hasVoted: false,
  votedCount: 0,
  totalParticipants: 0,

  results: null,
  isRevealed: false,
  isRevealAnimating: false,

  selectCard: (value) => set({ selectedCard: value }),

  submitVote: async (storyId, value) => {
    await apiCastVote(storyId, { value });
    set({ selectedCard: value, hasVoted: true });
  },

  changeVote: async (storyId, value) => {
    await apiCastVote(storyId, { value });
    set({ selectedCard: value });
  },

  loadResults: async (storyId) => {
    const results = await fetchResults(storyId);
    set({ results, isRevealed: true });
  },

  updateVoteCount: (update) =>
    set({
      votedCount: update.voteCount,
      // Keep existing totalParticipants if backend doesn't send it
      totalParticipants: get().totalParticipants || update.voteCount,
    }),

  setResults: (results) => set({ results }),

  startRevealAnimation: () => set({ isRevealAnimating: true, isRevealed: true }),

  finishRevealAnimation: () => set({ isRevealAnimating: false }),

  resetVoting: () =>
    set({
      selectedCard: null,
      hasVoted: false,
      votedCount: 0,
      results: null,
      isRevealed: false,
      isRevealAnimating: false,
    }),
}));
