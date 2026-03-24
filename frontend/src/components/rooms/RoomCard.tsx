import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import type { Room } from '../../types/api';

interface RoomCardProps {
  room: Room;
  index: number;
}

const statusStyles: Record<string, string> = {
  ACTIVE: 'bg-white text-black',
  CLOSED: 'bg-surface-tertiary text-text-muted',
  ARCHIVED: 'bg-surface-tertiary text-text-muted',
};

export function RoomCard({ room, index }: RoomCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: 'spring', stiffness: 200, damping: 25, delay: index * 0.05 }}
    >
      <Link
        to={`/rooms/${room.id}`}
        className="block p-5 rounded-lg border border-border bg-surface-secondary hover:border-border-hover hover:bg-surface-tertiary transition-all duration-200 group"
      >
        <div className="flex items-start justify-between mb-3">
          <h3 className="text-lg font-semibold text-white group-hover:text-accent transition-colors truncate mr-3">
            {room.name}
          </h3>
          <span
            className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${statusStyles[room.status] ?? ''}`}
          >
            {room.status}
          </span>
        </div>

        {room.description && (
          <p className="text-sm text-text-secondary mb-3 line-clamp-2">
            {room.description}
          </p>
        )}

        <div className="flex items-center gap-4 text-xs text-text-muted">
          <span>{room.participantCount} participants</span>
          <span>{room.deckType.name}</span>
          <span className="font-mono">{room.shortCode}</span>
        </div>
      </Link>
    </motion.div>
  );
}
