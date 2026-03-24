import { useEffect, useRef, useCallback } from 'react';
import type { Client, StompSubscription } from '@stomp/stompjs';
import { createStompClient } from '../lib/stomp';
import { useWebSocketStore } from '../stores/useWebSocketStore';
import { useRoomStore } from '../stores/useRoomStore';
import { useVotingStore } from '../stores/useVotingStore';
import { useNotificationStore } from '../stores/useNotificationStore';
import type { VoteCountUpdate, VotingResult, RoomStateUpdate, StoryUpdate } from '../types/api';

/**
 * Manages WebSocket lifecycle for a specific room.
 * Subscribes to 4 STOMP topics on mount, cleans up on unmount.
 */
export function useRoomWebSocket(roomId: string | undefined) {
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<StompSubscription[]>([]);

  const setWsStatus = useWebSocketStore((s) => s.setStatus);
  const setWsError = useWebSocketStore((s) => s.setError);

  const addParticipant = useRoomStore((s) => s.addParticipant);
  const removeParticipant = useRoomStore((s) => s.removeParticipant);
  const updateStory = useRoomStore((s) => s.updateStory);
  const updateStories = useRoomStore((s) => s.updateStories);
  const addStory = useRoomStore((s) => s.addStory);
  const removeStory = useRoomStore((s) => s.removeStory);
  const setActiveStory = useRoomStore((s) => s.setActiveStory);
  const loadRoom = useRoomStore((s) => s.loadRoom);

  const updateVoteCount = useVotingStore((s) => s.updateVoteCount);
  const setResults = useVotingStore((s) => s.setResults);
  const startRevealAnimation = useVotingStore((s) => s.startRevealAnimation);
  const resetVoting = useVotingStore((s) => s.resetVoting);

  const incrementUnread = useNotificationStore((s) => s.incrementUnread);

  const subscribe = useCallback(
    (client: Client, currentRoomId: string) => {
      const subs: StompSubscription[] = [];

      // Helper: parse message body — handles both raw JSON and WsEnvelope wrapper
      function parse<T>(body: string): T {
        const data = JSON.parse(body);
        // If wrapped in envelope, unwrap
        return data.payload !== undefined ? data.payload : data;
      }

      // 1. Vote count updates
      subs.push(
        client.subscribe(`/topic/rooms/${currentRoomId}/votes`, (msg) => {
          const update = parse<VoteCountUpdate>(msg.body);
          updateVoteCount(update);
        }),
      );

      // 2. Voting results (reveal)
      subs.push(
        client.subscribe(`/topic/rooms/${currentRoomId}/results`, (msg) => {
          const result = parse<VotingResult>(msg.body);
          startRevealAnimation();
          setResults(result);
          incrementUnread();
        }),
      );

      // 3. Room state (join/leave/timer)
      subs.push(
        client.subscribe(`/topic/rooms/${currentRoomId}/state`, (msg) => {
          const update = parse<RoomStateUpdate>(msg.body);
          switch (update.type) {
            case 'PARTICIPANT_JOINED':
              if (update.participant) addParticipant(update.participant);
              incrementUnread();
              break;
            case 'PARTICIPANT_LEFT':
              if (update.participant) removeParticipant(update.participant.id);
              break;
            case 'ROOM_CLOSED':
              loadRoom(currentRoomId);
              break;
          }
        }),
      );

      // 4. Story changes (add, update, voting started)
      subs.push(
        client.subscribe(`/topic/rooms/${currentRoomId}/stories`, (msg) => {
          const raw = JSON.parse(msg.body);
          // Handle WsEnvelope format (backend-02) — unwrap payload
          const update: StoryUpdate = raw.payload !== undefined ? raw.payload : raw;

          switch (update.type) {
            case 'STORY_ADDED':
            case 'added':
              if (update.story) addStory(update.story);
              break;
            case 'VOTING_STARTED':
            case 'voting-started':
              if (update.story) {
                updateStory(update.story);
                setActiveStory(update.story.id);
                resetVoting();
              }
              break;
            case 'STORY_UPDATED':
            case 'updated':
              if (update.story) updateStory(update.story);
              break;
            case 'STORY_DELETED':
            case 'deleted': {
              const sid = (update.story as any)?.storyId ?? update.story?.id;
              if (sid) removeStory(sid);
              break;
            }
            case 'STORY_REORDERED':
              if (update.stories) updateStories(update.stories);
              break;
          }
        }),
      );

      subscriptionsRef.current = subs;
    },
    [
      addParticipant,
      removeParticipant,
      updateStory,
      updateStories,
      addStory,
      removeStory,
      setActiveStory,
      updateVoteCount,
      setResults,
      startRevealAnimation,
      resetVoting,
      loadRoom,
      incrementUnread,
    ],
  );

  useEffect(() => {
    if (!roomId) return;

    setWsStatus('CONNECTING');

    const client = createStompClient(
      () => {
        setWsStatus('CONNECTED');
        subscribe(client, roomId);
      },
      () => {
        setWsStatus('DISCONNECTED');
      },
      (error) => {
        setWsError(error);
      },
    );

    client.activate();
    clientRef.current = client;

    return () => {
      subscriptionsRef.current.forEach((sub) => {
        try { sub.unsubscribe(); } catch { /* already unsubscribed */ }
      });
      subscriptionsRef.current = [];
      if (clientRef.current?.active) {
        clientRef.current.deactivate();
      }
      clientRef.current = null;
      setWsStatus('DISCONNECTED');
    };
  }, [roomId, subscribe, setWsStatus, setWsError]);
}
