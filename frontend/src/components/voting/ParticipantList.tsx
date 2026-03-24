import { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { springs } from '../../lib/springs';
import type { Participant, Vote } from '../../types/api';

interface ParticipantListProps {
  participants: Participant[];
  votes: Vote[];
  isRevealed: boolean;
  revealIndex: number;
  isModerator?: boolean;
  currentUserId?: string;
  onRemoveParticipant?: (userId: string) => Promise<void>;
}

/**
 * Shows all participants with their vote status.
 * During voting: shows checkmark if voted, empty if not.
 * After reveal: shows the card value next to each participant.
 * Uses AnimatePresence for join/leave animations.
 * Moderator can remove non-moderator participants via the X button.
 */
export function ParticipantList({
  participants,
  votes,
  isRevealed,
  revealIndex,
  isModerator = false,
  currentUserId,
  onRemoveParticipant,
}: ParticipantListProps) {
  const [removingUserId, setRemovingUserId] = useState<string | null>(null);

  const getVoteForParticipant = (participantId: string) =>
    votes.find((v) => v.participantId === participantId);

  const handleRemove = async (userId: string) => {
    if (!onRemoveParticipant) return;
    setRemovingUserId(userId);
    try {
      await onRemoveParticipant(userId);
    } catch {
      // Participant stays in the list on error
    } finally {
      setRemovingUserId(null);
    }
  };

  return (
    <div className="space-y-1">
      <h3 className="text-sm font-medium text-text-secondary uppercase tracking-wider mb-3">
        Participants ({participants.length})
      </h3>
      <AnimatePresence>
        {participants.map((participant, index) => {
          const vote = getVoteForParticipant(participant.id);
          const hasVoted = !!vote;
          const shouldShowValue = isRevealed && index <= revealIndex;
          const isSelf = participant.userId === currentUserId;
          const isMod = participant.role === 'MODERATOR';
          const isRemoving = removingUserId === participant.userId;
          const canRemove = isModerator && !isSelf && !isMod;

          return (
            <motion.div
              key={participant.id}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ ...springs.smooth, delay: index * 0.03 }}
              className={`
                flex items-center justify-between py-2 px-3 rounded-md
                hover:bg-surface-tertiary transition-colors group/participant
                ${isRemoving ? 'opacity-50' : ''}
              `}
            >
              <div className="flex items-center gap-3 min-w-0">
                {/* Avatar initial */}
                <div className="w-7 h-7 rounded-full bg-surface-tertiary border border-border flex items-center justify-center text-xs font-medium text-text-secondary shrink-0">
                  {(participant.username ?? '?').charAt(0).toUpperCase()}
                </div>
                <span className="text-sm text-text truncate">
                  {participant.username ?? 'Unknown'}
                </span>
                {isMod && (
                  <span className="text-xs text-text-muted shrink-0">(mod)</span>
                )}
              </div>

              <div className="flex items-center gap-2">
                {/* Vote status */}
                {shouldShowValue && vote?.value ? (
                  <motion.span
                    initial={{ scale: 0, rotateY: 180 }}
                    animate={{ scale: 1, rotateY: 0 }}
                    transition={springs.flip}
                    className="w-8 h-10 rounded bg-white text-black font-mono font-bold text-sm flex items-center justify-center"
                  >
                    {vote.value}
                  </motion.span>
                ) : hasVoted ? (
                  <span className="text-white text-lg" title="Voted">
                    &#10003;
                  </span>
                ) : (
                  <span className="text-text-muted text-sm">---</span>
                )}

                {/* Remove button — moderator only, not on self or other mod */}
                {canRemove && (
                  <button
                    onClick={() => handleRemove(participant.userId)}
                    disabled={isRemoving}
                    className="opacity-0 group-hover/participant:opacity-100 transition-opacity p-1 text-text-muted hover:text-red-400 disabled:opacity-50"
                    title={`Remove ${participant.username}`}
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="w-3.5 h-3.5"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                    >
                      <path
                        fillRule="evenodd"
                        d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </button>
                )}
              </div>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
}
