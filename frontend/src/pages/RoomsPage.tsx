import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useRoomStore } from '../stores/useRoomStore';
import { RoomCard } from '../components/rooms/RoomCard';
import { PageTransition } from '../components/animations/PageTransition';
import { springs } from '../lib/springs';

export function RoomsPage() {
  const navigate = useNavigate();
  const { rooms, isLoadingRooms, totalPages, currentPage, loadRooms, joinRoom } =
    useRoomStore();

  const [joinCode, setJoinCode] = useState('');
  const [joinError, setJoinError] = useState<string | null>(null);

  useEffect(() => {
    loadRooms();
  }, [loadRooms]);

  const handleJoin = async () => {
    if (!joinCode.trim()) return;
    setJoinError(null);
    try {
      const room = await joinRoom(joinCode.trim());
      navigate(`/rooms/${room.id}`);
    } catch {
      setJoinError('Room not found. Check the code and try again.');
    }
  };

  return (
    <PageTransition>
      <div className="max-w-4xl mx-auto px-6 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-white">Your Rooms</h1>
            <p className="text-sm text-text-secondary mt-1">
              Create a new room or join an existing one
            </p>
          </div>
          <Link
            to="/rooms/create"
            className="px-4 py-2 bg-white text-black rounded-lg font-medium text-sm hover:bg-gray-200 transition-colors"
          >
            + Create Room
          </Link>
        </div>

        {/* Join by code */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={springs.smooth}
          className="mb-8 p-4 rounded-lg border border-border bg-surface-secondary"
        >
          <label className="text-sm text-text-secondary block mb-2">
            Join by Short Code
          </label>
          <div className="flex gap-3">
            <input
              type="text"
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleJoin()}
              placeholder="Enter room code..."
              className="flex-1 px-3 py-2 bg-surface-tertiary border border-border rounded-md text-white text-sm placeholder:text-text-muted focus:outline-none focus:border-border-hover font-mono"
            />
            <button
              onClick={handleJoin}
              className="px-4 py-2 bg-surface-tertiary border border-border hover:border-border-hover text-white rounded-md text-sm transition-colors"
            >
              Join
            </button>
          </div>
          {joinError && (
            <p className="text-sm text-text-muted mt-2">{joinError}</p>
          )}
        </motion.div>

        {/* Room list */}
        {isLoadingRooms ? (
          <div className="flex justify-center py-16">
            <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : rooms.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-text-secondary text-sm">
              No rooms yet. Create one to get started.
            </p>
          </div>
        ) : (
          <>
            <div className="grid gap-3">
              {rooms.map((room, index) => (
                <RoomCard key={room.id} room={room} index={index} />
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-8">
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => loadRooms(i)}
                    className={`w-8 h-8 rounded text-sm transition-colors ${
                      i === currentPage
                        ? 'bg-white text-black font-bold'
                        : 'bg-surface-tertiary text-text-secondary hover:text-white'
                    }`}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </PageTransition>
  );
}
