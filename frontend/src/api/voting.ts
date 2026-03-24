import api from '../lib/api';
import type { CastVoteRequest, VotingResult } from '../types/api';

export async function startVoting(storyId: string): Promise<void> {
  await api.post(`/stories/${storyId}/voting/start`);
}

export async function castVote(
  storyId: string,
  request: CastVoteRequest,
): Promise<void> {
  await api.post(`/stories/${storyId}/votes`, request);
}

export async function finishVoting(storyId: string): Promise<void> {
  await api.post(`/stories/${storyId}/voting/finish`);
}

export async function fetchResults(storyId: string): Promise<VotingResult> {
  const { data } = await api.get<VotingResult>(`/stories/${storyId}/results`);
  return data;
}
