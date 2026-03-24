import { motion } from 'framer-motion';
import { springs } from '../../lib/springs';
import type { ReactNode } from 'react';

interface PageTransitionProps {
  children: ReactNode;
}

/**
 * Wraps page content with a fade + slide-up transition on route changes.
 */
export function PageTransition({ children }: PageTransitionProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -12 }}
      transition={springs.smooth}
      className="w-full"
    >
      {children}
    </motion.div>
  );
}
