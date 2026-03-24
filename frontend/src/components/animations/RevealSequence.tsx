import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { springs } from '../../lib/springs';

interface RevealSequenceProps {
  isActive: boolean;
  onCountdownComplete: () => void;
  children: (revealIndex: number) => React.ReactNode;
  totalCards: number;
  staggerDelayMs?: number;
}

/**
 * 3-2-1 countdown overlay followed by staggered card reveals.
 */
export function RevealSequence({
  isActive,
  onCountdownComplete,
  children,
  totalCards,
  staggerDelayMs = 120,
}: RevealSequenceProps) {
  const [countdown, setCountdown] = useState<number | null>(null);
  const [revealIndex, setRevealIndex] = useState(-1);

  const startStaggeredReveal = useCallback(() => {
    let currentIndex = 0;
    const interval = setInterval(() => {
      setRevealIndex(currentIndex);
      currentIndex++;
      if (currentIndex >= totalCards) {
        clearInterval(interval);
      }
    }, staggerDelayMs);

    return () => clearInterval(interval);
  }, [totalCards, staggerDelayMs]);

  useEffect(() => {
    if (!isActive) {
      setCountdown(null);
      setRevealIndex(-1);
      return;
    }

    // Start countdown: 3, 2, 1
    setCountdown(3);
    const timers: ReturnType<typeof setTimeout>[] = [];

    timers.push(setTimeout(() => setCountdown(2), 700));
    timers.push(setTimeout(() => setCountdown(1), 1400));
    timers.push(
      setTimeout(() => {
        setCountdown(null);
        onCountdownComplete();
        startStaggeredReveal();
      }, 2100),
    );

    return () => timers.forEach(clearTimeout);
  }, [isActive, onCountdownComplete, startStaggeredReveal]);

  return (
    <>
      {/* Countdown overlay */}
      <AnimatePresence>
        {countdown !== null && (
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/80"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.span
              key={countdown}
              className="text-9xl font-mono font-bold text-white"
              initial={{ scale: 0.5, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 1.5, opacity: 0 }}
              transition={springs.snappy}
            >
              {countdown}
            </motion.span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Staggered card reveals */}
      {children(revealIndex)}
    </>
  );
}
