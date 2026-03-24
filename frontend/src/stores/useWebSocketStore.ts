import { create } from 'zustand';

export type ConnectionStatus = 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'ERROR';

interface WebSocketState {
  status: ConnectionStatus;
  error: string | null;
  reconnectAttempts: number;

  setStatus: (status: ConnectionStatus) => void;
  setError: (error: string | null) => void;
  incrementReconnectAttempts: () => void;
  resetReconnectAttempts: () => void;
}

export const useWebSocketStore = create<WebSocketState>((set) => ({
  status: 'DISCONNECTED',
  error: null,
  reconnectAttempts: 0,

  setStatus: (status) => set({ status, error: status === 'CONNECTED' ? null : undefined }),

  setError: (error) => set({ error, status: error ? 'ERROR' : undefined }),

  incrementReconnectAttempts: () =>
    set((state) => ({ reconnectAttempts: state.reconnectAttempts + 1 })),

  resetReconnectAttempts: () => set({ reconnectAttempts: 0 }),
}));
