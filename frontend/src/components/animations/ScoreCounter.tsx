import { useEffect, useRef, useState } from 'react';
import { useSpring, motion, useMotionValue } from 'framer-motion';

interface ScoreCounterProps {
  value: number;
  precision?: number;
  className?: string;
  label?: string;
}

/**
 * Animated number counter using framer-motion spring interpolation.
 */
export function ScoreCounter({
  value,
  precision = 1,
  className = '',
  label,
}: ScoreCounterProps) {
  const motionValue = useMotionValue(0);
  const springValue = useSpring(motionValue, {
    stiffness: 200,
    damping: 25,
  });
  const [display, setDisplay] = useState('0');
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    motionValue.set(value);
  }, [value, motionValue]);

  useEffect(() => {
    const unsubscribe = springValue.on('change', (latest) => {
      setDisplay(latest.toFixed(precision));
    });
    return unsubscribe;
  }, [springValue, precision]);

  return (
    <div className={`flex flex-col items-center gap-1 ${className}`}>
      <motion.span
        ref={ref}
        className="text-5xl font-mono font-bold text-white tabular-nums"
        initial={{ scale: 0.8, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ type: 'spring', stiffness: 300, damping: 20 }}
      >
        {display}
      </motion.span>
      {label && (
        <span className="text-sm text-text-secondary uppercase tracking-wider">
          {label}
        </span>
      )}
    </div>
  );
}
