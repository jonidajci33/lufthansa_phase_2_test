import { motion, AnimatePresence } from 'framer-motion';
import { useWebSocketStore } from '../../stores/useWebSocketStore';
import { springs } from '../../lib/springs';

/**
 * Shows a banner when WebSocket connection is lost or errored.
 * Automatically hides when connected.
 */
export function ConnectionBanner() {
  const { status, error } = useWebSocketStore();

  const showBanner = status === 'ERROR' || status === 'CONNECTING';

  const message =
    status === 'ERROR'
      ? error ?? 'Connection lost. Attempting to reconnect...'
      : 'Connecting to server...';

  return (
    <AnimatePresence>
      {showBanner && (
        <motion.div
          initial={{ height: 0, opacity: 0 }}
          animate={{ height: 'auto', opacity: 1 }}
          exit={{ height: 0, opacity: 0 }}
          transition={springs.snappy}
          className="overflow-hidden"
        >
          <div className="px-4 py-2 bg-surface-tertiary border-b border-border text-center">
            <span className="text-sm text-text-secondary">
              {status === 'CONNECTING' && (
                <span className="inline-block w-3 h-3 border border-text-muted border-t-transparent rounded-full animate-spin mr-2 align-middle" />
              )}
              {message}
            </span>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
