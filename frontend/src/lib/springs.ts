import type { Transition } from 'framer-motion';

export const springs = {
  snappy: {
    type: 'spring' as const,
    stiffness: 400,
    damping: 30,
  },
  smooth: {
    type: 'spring' as const,
    stiffness: 200,
    damping: 25,
  },
  flip: {
    type: 'spring' as const,
    stiffness: 300,
    damping: 20,
    mass: 0.8,
  },
  gentle: {
    type: 'spring' as const,
    stiffness: 100,
    damping: 15,
  },
} satisfies Record<string, Transition>;
