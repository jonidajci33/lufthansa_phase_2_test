import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useRoomStore } from '../stores/useRoomStore';
import { useVotingStore } from '../stores/useVotingStore';
import { useAuthStore } from '../stores/useAuthStore';
import { useRoomWebSocket } from '../hooks/useRoomWebSocket';
import { fetchParticipants } from '../api/rooms';
import { createStory } from '../api/stories';
import { startVoting, finishVoting } from '../api/voting';
import { StoryList } from '../components/voting/StoryList';
import { CardHand } from '../components/voting/CardHand';
import { ParticipantList } from '../components/voting/ParticipantList';
import { RoomHeader } from '../components/voting/RoomHeader';
import { InviteSection } from '../components/voting/InviteSection';
import { ScoreCounter } from '../components/animations/ScoreCounter';
import { PageTransition } from '../components/animations/PageTransition';

export function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const user = useAuthStore((s) => s.user);
  const {
    activeRoom,
    participants,
    stories,
    activeStoryId,
    isLoadingRoom,
    loadRoom,
    setActiveStory,
    clearActiveRoom,
    addStory,
    updateStory,
    removeStory,
    updateRoom,
    removeParticipantByUserId,
  } = useRoomStore();

  const {
    results,
    isRevealed,
    votedCount,
    resetVoting,
  } = useVotingStore();

  const [showAddStory, setShowAddStory] = useState(false);
  const [newStoryTitle, setNewStoryTitle] = useState('');
  const [newStoryDescription, setNewStoryDescription] = useState('');

  // Connect WebSocket
  useRoomWebSocket(roomId);

  // Load room data + set totalParticipants
  useEffect(() => {
    if (roomId) {
      loadRoom(roomId);
      fetchParticipants(roomId).then((p) => {
        p.forEach((participant) => useRoomStore.getState().addParticipant(participant));
        useVotingStore.setState({ totalParticipants: p.length });
      });
    }
    return () => {
      clearActiveRoom();
      resetVoting();
    };
  }, [roomId, loadRoom, clearActiveRoom, resetVoting]);

  const activeStory = stories.find((s) => s.id === activeStoryId);
  const isModerator = activeRoom?.moderatorId === user?.id;
  const isVotingActive = activeStory?.status === 'VOTING';

  // Reset voting state when switching stories
  useEffect(() => {
    if (activeStoryId) {
      resetVoting();
    }
  }, [activeStoryId, resetVoting]);
  const deckValues = activeRoom?.deckType?.values?.map((v) => v.label) ?? [];

  const handleStartVoting = async () => {
    if (activeStoryId) {
      resetVoting();
      useVotingStore.setState({ totalParticipants: participants.length });
      await startVoting(activeStoryId);
    }
  };

  const handleFinishVoting = async () => {
    if (activeStoryId) {
      await finishVoting(activeStoryId);
    }
  };

  const handleAddStory = useCallback(async () => {
    if (!newStoryTitle.trim() || !roomId) return;
    try {
      const story = await createStory({
        roomId,
        title: newStoryTitle.trim(),
        description: newStoryDescription.trim() || undefined,
      });
      addStory(story);
      setNewStoryTitle('');
      setNewStoryDescription('');
      setShowAddStory(false);
    } catch {
      // Handle error silently
    }
  }, [newStoryTitle, newStoryDescription, roomId, addStory]);

  // Room header save handler
  const handleRoomSave = useCallback(async (updates: { name?: string; description?: string }) => {
    if (!roomId) return;
    await updateRoom(roomId, updates);
  }, [roomId, updateRoom]);

  // Remove participant handler
  const handleRemoveParticipant = useCallback(async (userId: string) => {
    if (!roomId) return;
    await removeParticipantByUserId(roomId, userId);
  }, [roomId, removeParticipantByUserId]);

  if (isLoadingRoom) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!activeRoom) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <p className="text-text-secondary">Room not found</p>
      </div>
    );
  }

  return (
    <PageTransition>
      <div className="flex-1 flex flex-col min-h-0">
        {/* Room header bar */}
        <div className="px-6 py-3 border-b border-border bg-surface-secondary flex items-center justify-between">
          <RoomHeader
            name={activeRoom.name}
            description={activeRoom.description}
            shortCode={activeRoom.shortCode}
            isModerator={isModerator}
            onSave={handleRoomSave}
          />
          <div className="flex items-center gap-3">
            {/* Vote counter — visible during voting */}
            {isVotingActive && (
              <span className="text-xs text-text-secondary font-mono">
                {votedCount}/{participants.length} voted
              </span>
            )}

            {/* Moderator: Start Voting (only on pending stories) */}
            {isModerator && activeStoryId && !isVotingActive && !isRevealed && activeStory?.status === 'PENDING' && (
              <button
                onClick={handleStartVoting}
                className="px-4 py-1.5 bg-white text-black rounded text-sm font-medium hover:bg-gray-200 transition-colors"
              >
                Start Voting
              </button>
            )}

            {/* Moderator: Reveal (during voting) */}
            {isModerator && isVotingActive && (
              <button
                onClick={handleFinishVoting}
                className="px-4 py-1.5 bg-white text-black rounded text-sm font-medium hover:bg-gray-200 transition-colors"
              >
                Reveal
              </button>
            )}

            {/* After reveal: show average in header */}
            {isRevealed && results && (
              <span className="text-sm font-mono text-white">
                Avg: {results.averageScore != null ? Number(results.averageScore).toFixed(1) : '—'}
              </span>
            )}
          </div>
        </div>

        {/* Main content: 3-column layout */}
        <div className="flex-1 flex min-h-0">
          {/* Left: Story list */}
          <aside className="w-64 border-r border-border p-4 overflow-y-auto shrink-0 hidden md:block">
            <StoryList
              stories={stories}
              activeStoryId={activeStoryId}
              onSelectStory={setActiveStory}
              isModerator={isModerator}
              onAddStory={() => setShowAddStory(true)}
              onStoryUpdated={updateStory}
              onStoryDeleted={removeStory}
            />

            {/* Add story form */}
            {showAddStory && (
              <div className="mt-3 p-3 rounded-md bg-surface-tertiary border border-border">
                <input
                  type="text"
                  value={newStoryTitle}
                  onChange={(e) => setNewStoryTitle(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleAddStory()}
                  placeholder="Story title..."
                  autoFocus
                  className="w-full px-2 py-1.5 bg-surface border border-border rounded text-sm text-white placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                />
                <input
                  type="text"
                  value={newStoryDescription}
                  onChange={(e) => setNewStoryDescription(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleAddStory()}
                  placeholder="Description (optional)..."
                  className="w-full mt-1.5 px-2 py-1.5 bg-surface border border-border rounded text-xs text-text-secondary placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                />
                <div className="flex gap-2 mt-2">
                  <button
                    onClick={handleAddStory}
                    className="text-xs px-2 py-1 bg-white text-black rounded hover:bg-gray-200 transition-colors"
                  >
                    Add
                  </button>
                  <button
                    onClick={() => {
                      setShowAddStory(false);
                      setNewStoryTitle('');
                      setNewStoryDescription('');
                    }}
                    className="text-xs px-2 py-1 text-text-muted hover:text-white transition-colors"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </aside>

          {/* Center: Voting area */}
          <div className="flex-1 flex flex-col items-center justify-center px-6 py-8">
            {activeStory ? (
              <>
                {/* Active story title */}
                <div className="mb-6 text-center">
                  <p className="text-xs text-text-muted uppercase tracking-wider mb-1">
                    {activeStory.status === 'VOTED' || isRevealed ? 'Results' : isVotingActive ? 'Voting' : 'Estimating'}
                  </p>
                  <h3 className="text-xl font-semibold text-white">
                    {activeStory.title}
                  </h3>
                  {activeStory.description && (
                    <p className="text-sm text-text-secondary mt-2 max-w-md">
                      {activeStory.description}
                    </p>
                  )}
                </div>

                {/* Results display — shown after reveal OR when viewing a VOTED story */}
                {activeStory.status === 'VOTED' || (isRevealed && results) ? (
                  <div className="flex flex-col items-center gap-6">
                    <ScoreCounter
                      value={
                        results?.averageScore != null
                          ? Number(results.averageScore)
                          : activeStory.finalScore != null
                            ? Number(activeStory.finalScore)
                            : activeStory.finalEstimate != null
                              ? Number(activeStory.finalEstimate)
                              : 0
                      }
                      label="Average Score"
                    />
                    <div className="flex items-center gap-4">
                      <span className="text-sm text-text-secondary">
                        {results
                          ? `${results.totalVotes} vote${results.totalVotes !== 1 ? 's' : ''}`
                          : activeStory.voteCount != null
                            ? `${activeStory.voteCount} vote${activeStory.voteCount !== 1 ? 's' : ''}`
                            : 'Voting complete'}
                      </span>
                      {(results?.consensusReached || activeStory.consensusReached) && (
                        <span className="px-3 py-1 rounded-full border border-white text-sm text-white">
                          Consensus!
                        </span>
                      )}
                    </div>
                    {results?.distribution && (
                      <div className="flex flex-wrap gap-2 mt-4">
                        {Object.entries(results.distribution).map(([value, count]) => (
                          <div
                            key={value}
                            className="flex flex-col items-center px-3 py-2 rounded-md bg-surface-secondary border border-border"
                          >
                            <span className="text-lg font-mono font-bold text-white">
                              {value}
                            </span>
                            <span className="text-xs text-text-muted">
                              {count as number}x
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                    {/* Moderator: Next story after reveal */}
                    {isModerator && (
                      <button
                        onClick={() => {
                          resetVoting();
                          const nextPending = stories.find((s) => s.status === 'PENDING' && s.id !== activeStoryId);
                          if (nextPending) setActiveStory(nextPending.id);
                        }}
                        className="mt-4 px-4 py-2 border border-border text-text-secondary rounded text-sm hover:border-border-hover hover:text-white transition-colors"
                      >
                        Next Story
                      </button>
                    )}
                  </div>
                ) : isVotingActive ? (
                  /* Card hand for voting — only during active VOTING */
                  <CardHand
                    deckValues={deckValues}
                    storyId={activeStory.id}
                    disabled={false}
                  />
                ) : (
                  /* PENDING story — waiting for moderator to start */
                  <div className="text-center text-text-secondary">
                    <p>{isModerator ? 'Click "Start Voting" to begin' : 'Waiting for moderator to start voting...'}</p>
                  </div>
                )}
              </>
            ) : (
              <div className="text-center">
                <p className="text-text-secondary">
                  {stories.length === 0
                    ? isModerator
                      ? 'Add a story to start estimating'
                      : 'Waiting for moderator to add stories...'
                    : 'Select a story to begin'}
                </p>
              </div>
            )}
          </div>

          {/* Right: Participants + Invite */}
          <aside className="w-56 border-l border-border p-4 overflow-y-auto shrink-0 hidden lg:block">
            <ParticipantList
              participants={participants}
              votes={results?.votes ?? []}
              isRevealed={isRevealed}
              revealIndex={isRevealed ? participants.length : -1}
              isModerator={isModerator}
              currentUserId={user?.id}
              onRemoveParticipant={handleRemoveParticipant}
            />

            {/* Moderator-only invite section */}
            {isModerator && roomId && (
              <InviteSection roomId={roomId} />
            )}
          </aside>
        </div>
      </div>
    </PageTransition>
  );
}
