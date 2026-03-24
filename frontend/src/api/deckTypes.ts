import api from '../lib/api';
import type { DeckType, CreateDeckTypeRequest } from '../types/api';

export async function fetchDeckTypes(): Promise<DeckType[]> {
  const { data } = await api.get('/deck-types');
  return Array.isArray(data) ? data : data.data ?? [];
}

export async function createDeckType(request: CreateDeckTypeRequest): Promise<DeckType> {
  const { data } = await api.post<DeckType>('/deck-types', request);
  return data;
}
