import { motion } from 'framer-motion';
import { springs } from '../../lib/springs';
import type { ReactNode } from 'react';

interface CardFlipProps {
  isFlipped: boolean;
  front: ReactNode;
  back?: ReactNode;
  size?: 'sm' | 'md' | 'lg';
  onClick?: () => void;
  className?: string;
}

const sizeMap = {
  sm: 'w-14 h-20',
  md: 'w-20 h-28',
  lg: 'w-24 h-36',
};

export function CardFlip({
  isFlipped,
  front,
  back,
  size = 'md',
  onClick,
  className = '',
}: CardFlipProps) {
  return (
    <div
      className={`perspective-1000 cursor-pointer ${sizeMap[size]} ${className}`}
      onClick={onClick}
    >
      <motion.div
        className="relative w-full h-full preserve-3d"
        animate={{ rotateY: isFlipped ? 180 : 0 }}
        transition={springs.flip}
      >
        {/* Front face */}
        <div className="absolute inset-0 backface-hidden rounded-lg border border-border bg-card-front text-black flex items-center justify-center font-mono font-bold text-xl shadow-lg">
          {front}
        </div>

        {/* Back face */}
        <div
          className="absolute inset-0 backface-hidden rounded-lg border border-border bg-card-back flex items-center justify-center"
          style={{ transform: 'rotateY(180deg)' }}
        >
          {back ?? (
            <div className="w-full h-full rounded-lg cross-hatch opacity-40" />
          )}
        </div>
      </motion.div>
    </div>
  );
}
