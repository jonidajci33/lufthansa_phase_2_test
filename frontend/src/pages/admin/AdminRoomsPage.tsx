import { useEffect, useState } from 'react';
import { fetchRooms } from '../../api/rooms';
import api from '../../lib/api';
import { PageTransition } from '../../components/animations/PageTransition';
import type { Room } from '../../types/api';

export function AdminRoomsPage() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const loadRooms = () => {
    setIsLoading(true);
    fetchRooms(0, 50).then((page) => {
      setRooms(page.data);
      setIsLoading(false);
    }).catch(() => setIsLoading(false));
  };

  useEffect(() => { loadRooms(); }, []);

  const handleDelete = async (roomId: string, roomName: string) => {
    if (!confirm(`Delete room "${roomName}"? This cannot be undone.`)) return;
    try {
      await api.delete(`/rooms/${roomId}`);
      setRooms(rooms.filter((r) => r.id !== roomId));
    } catch {
      alert('Failed to delete room');
    }
  };

  return (
    <PageTransition>
      <div className="max-w-4xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-2">Room Oversight</h1>
        <p className="text-sm text-text-secondary mb-6">View and manage all rooms in the system</p>

        {isLoading ? (
          <div className="flex justify-center py-16">
            <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : rooms.length === 0 ? (
          <p className="text-text-secondary text-center py-16">No rooms found</p>
        ) : (
          <div className="space-y-2">
            <div className="grid grid-cols-[1fr_auto_auto_auto] gap-4 px-4 py-2 text-xs text-text-muted uppercase tracking-wider">
              <span>Room</span>
              <span>Status</span>
              <span>Participants</span>
              <span>Actions</span>
            </div>
            {rooms.map((room) => (
              <div
                key={room.id}
                className="grid grid-cols-[1fr_auto_auto_auto] gap-4 items-center px-4 py-3 rounded-md border border-border bg-surface-secondary"
              >
                <div>
                  <span className="text-sm text-white font-medium">{room.name}</span>
                  <span className="text-xs text-text-muted ml-2 font-mono">{room.shortCode}</span>
                  {room.description && (
                    <p className="text-xs text-text-muted mt-0.5 truncate max-w-md">{room.description}</p>
                  )}
                </div>
                <span className={`text-[10px] px-1.5 py-0.5 rounded ${
                  room.status === 'ACTIVE' ? 'bg-green-500/20 text-green-400' : 'bg-surface-tertiary text-text-muted'
                }`}>
                  {room.status}
                </span>
                <span className="text-xs text-text-secondary">{room.participantCount}</span>
                <button
                  onClick={() => handleDelete(room.id, room.name)}
                  className="text-xs text-text-muted hover:text-red-400 transition-colors"
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </PageTransition>
  );
}
