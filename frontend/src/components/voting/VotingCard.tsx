import { motion } from 'framer-motion';
import { springs } from '../../lib/springs';

interface VotingCardProps {
  value: string;
  isSelected: boolean;
  onClick: () => void;
  disabled?: boolean;
}

/**
 * Individual card in the voting hand.
 * Highlights with white border when selected, subtle lift on hover.
 */
export function VotingCard({
  value,
  isSelected,
  onClick,
  disabled = false,
}: VotingCardProps) {
  return (
    <motion.button
      onClick={onClick}
      disabled={disabled}
      className={`
        relative w-16 h-24 sm:w-20 sm:h-28 rounded-lg border-2 font-mono font-bold text-xl
        transition-colors duration-150 select-none
        ${
          isSelected
            ? 'border-white bg-white text-black shadow-[0_0_20px_rgba(255,255,255,0.2)]'
            : 'border-border bg-surface-secondary text-white hover:border-border-hover'
        }
        ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
      `}
      whileHover={disabled ? {} : { y: -8, scale: 1.05 }}
      whileTap={disabled ? {} : { scale: 0.95 }}
      animate={isSelected ? { y: -12 } : { y: 0 }}
      transition={springs.snappy}
    >
      {value}
    </motion.button>
  );
}
