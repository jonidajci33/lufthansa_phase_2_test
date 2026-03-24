import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../stores/useAuthStore';

// All calls go through nginx — never direct to backend
const WS_ENDPOINT = window.location.origin + '/ws/estimation';

const MAX_RECONNECT_ATTEMPTS = 10;
const INITIAL_RECONNECT_DELAY = 1000;
const MAX_RECONNECT_DELAY = 30000;

/**
 * Create a STOMP client using SockJS transport.
 * Token is read from the auth store — no direct Keycloak dependency.
 *
 * IMPORTANT: This function should only be called after auth initialization
 * is complete (i.e., after AuthGuard confirms isAuthReady && isAuthenticated).
 * This is guaranteed by the component tree: RoomPage only renders inside
 * AuthGuard, and useRoomWebSocket only runs inside RoomPage.
 */
export function createStompClient(
  onConnect: () => void,
  onDisconnect: () => void,
  onError: (error: string) => void,
): Client {
  let reconnectAttempts = 0;

  const client = new Client({
    webSocketFactory: () => new SockJS(WS_ENDPOINT),
    connectHeaders: {
      Authorization: `Bearer ${useAuthStore.getState().token ?? ''}`,
    },
    reconnectDelay: INITIAL_RECONNECT_DELAY,
    onConnect: () => {
      reconnectAttempts = 0;
      onConnect();
    },
    onDisconnect: () => {
      onDisconnect();
    },
    onStompError: (frame) => {
      const message = frame.headers['message'] ?? 'STOMP error';

      // If STOMP reports an auth error, attempt to refresh and reconnect
      if (message.toLowerCase().includes('401') || message.toLowerCase().includes('unauthorized')) {
        const { token, refreshToken } = useAuthStore.getState();

        if (!token && !refreshToken) {
          // No tokens available — user should be logged out
          client.deactivate();
          onError('Authentication expired. Please log in again.');
          return;
        }

        // Attempt to refresh the token via the api interceptor by
        // triggering a lightweight call. If the store already has a
        // fresh token (refreshed by another request), reconnect will
        // pick it up via beforeConnect.
        import('./api').then(({ default: api }) => {
          api.get('/auth/me').then(() => {
            // Token refreshed successfully — reconnect will pick up new token via beforeConnect
            client.deactivate().then(() => client.activate());
          }).catch(() => {
            client.deactivate();
            onError('Authentication failed — please log in again.');
          });
        }).catch(() => {
          client.deactivate();
          onError('Authentication failed — please log in again.');
        });
        return;
      }

      onError(message);
    },
    onWebSocketError: () => {
      reconnectAttempts++;
      if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        client.deactivate();
        onError('Maximum reconnection attempts reached');
        return;
      }
      const delay = Math.min(
        INITIAL_RECONNECT_DELAY * Math.pow(2, reconnectAttempts),
        MAX_RECONNECT_DELAY,
      );
      client.reconnectDelay = delay;
    },
    // Refresh token from store before each reconnect.
    // After auth initialization, the store always has the latest valid token
    // (either the original or a refreshed one from the interceptor).
    beforeConnect: () => {
      const token = useAuthStore.getState().token;
      if (token) {
        client.connectHeaders = {
          Authorization: `Bearer ${token}`,
        };
      } else {
        // Token was cleared (logout happened) — stop reconnecting
        client.deactivate();
      }
    },
  });

  return client;
}
