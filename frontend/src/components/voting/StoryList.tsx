import { useState } from 'react';
import { motion } from 'framer-motion';
import { springs } from '../../lib/springs';
import { updateStory, deleteStory } from '../../api/stories';
import type { Story, StoryStatus } from '../../types/api';

interface StoryListProps {
  stories: Story[];
  activeStoryId: string | null;
  onSelectStory: (storyId: string) => void;
  isModerator: boolean;
  onAddStory?: () => void;
  onStoryUpdated?: (story: Story) => void;
  onStoryDeleted?: (storyId: string) => void;
}

const statusIndicator: Record<StoryStatus, { label: string; style: string }> = {
  PENDING: { label: 'Pending', style: 'bg-surface-tertiary text-text-muted' },
  VOTING: { label: 'Voting', style: 'bg-amber-500/20 text-amber-400 animate-pulse' },
  VOTED: { label: 'Voted', style: 'bg-green-500/20 text-green-400' },
  REVEALED: { label: 'Revealed', style: 'bg-blue-500/20 text-blue-400' },
  SKIPPED: { label: 'Skipped', style: 'bg-surface-tertiary text-text-muted' },
};

export function StoryList({
  stories,
  activeStoryId,
  onSelectStory,
  isModerator,
  onAddStory,
  onStoryUpdated,
  onStoryDeleted,
}: StoryListProps) {
  const [editingStoryId, setEditingStoryId] = useState<string | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [deletingStoryId, setDeletingStoryId] = useState<string | null>(null);

  const handleStartEdit = (story: Story, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingStoryId(story.id);
    setEditTitle(story.title);
    setEditDescription(story.description ?? '');
  };

  const handleSaveEdit = async (storyId: string) => {
    if (!editTitle.trim()) return;
    setIsSaving(true);
    try {
      const updated = await updateStory(storyId, {
        title: editTitle.trim(),
        description: editDescription.trim() || undefined,
      });
      onStoryUpdated?.(updated);
      setEditingStoryId(null);
    } catch {
      // Keep edit mode open on failure
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancelEdit = () => {
    setEditingStoryId(null);
    setEditTitle('');
    setEditDescription('');
  };

  const handleDelete = async (storyId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setDeletingStoryId(storyId);
    try {
      await deleteStory(storyId);
      onStoryDeleted?.(storyId);
    } catch {
      // Silently fail — story still in list
    } finally {
      setDeletingStoryId(null);
    }
  };

  const handleEditKeyDown = (e: React.KeyboardEvent, storyId: string) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSaveEdit(storyId);
    }
    if (e.key === 'Escape') {
      handleCancelEdit();
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-medium text-text-secondary uppercase tracking-wider">
          Stories ({stories.length})
        </h3>
        {isModerator && onAddStory && (
          <button
            onClick={onAddStory}
            className="text-xs text-text-muted hover:text-white transition-colors border border-border hover:border-border-hover rounded px-2 py-1"
          >
            + Add
          </button>
        )}
      </div>

      <div className="flex-1 overflow-y-auto space-y-1">
        {stories.map((story, index) => {
          const isActive = story.id === activeStoryId;
          const status = statusIndicator[story.status];
          const isPending = story.status === 'PENDING';
          const isEditing = editingStoryId === story.id;
          const isDeleting = deletingStoryId === story.id;

          if (isEditing) {
            return (
              <div
                key={story.id}
                className="px-3 py-2.5 rounded-md bg-surface-tertiary border border-border-hover"
                onClick={(e) => e.stopPropagation()}
              >
                <input
                  type="text"
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  onKeyDown={(e) => handleEditKeyDown(e, story.id)}
                  placeholder="Story title..."
                  autoFocus
                  className="w-full px-2 py-1 bg-surface border border-border rounded text-sm text-white placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                />
                <input
                  type="text"
                  value={editDescription}
                  onChange={(e) => setEditDescription(e.target.value)}
                  onKeyDown={(e) => handleEditKeyDown(e, story.id)}
                  placeholder="Description (optional)..."
                  className="w-full mt-1 px-2 py-1 bg-surface border border-border rounded text-xs text-text-secondary placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                />
                <div className="flex gap-2 mt-2">
                  <button
                    onClick={() => handleSaveEdit(story.id)}
                    disabled={isSaving || !editTitle.trim()}
                    className="text-xs px-2 py-0.5 bg-white text-black rounded hover:bg-gray-200 transition-colors disabled:opacity-50"
                  >
                    {isSaving ? 'Saving...' : 'Save'}
                  </button>
                  <button
                    onClick={handleCancelEdit}
                    className="text-xs px-2 py-0.5 text-text-muted hover:text-white transition-colors"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            );
          }

          return (
            <motion.button
              key={story.id}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ ...springs.smooth, delay: index * 0.03 }}
              onClick={() => onSelectStory(story.id)}
              className={`
                w-full text-left px-3 py-2.5 rounded-md transition-all duration-150 group/story
                ${
                  isActive
                    ? 'bg-surface-tertiary border border-border-hover'
                    : 'hover:bg-surface-tertiary border border-transparent'
                }
                ${isDeleting ? 'opacity-50' : ''}
              `}
            >
              <div className="flex items-start gap-2">
                <span className="text-xs text-text-muted font-mono mt-0.5 shrink-0">
                  #{index + 1}
                </span>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1">
                    <p className={`text-sm truncate flex-1 ${isActive ? 'text-white font-medium' : 'text-text'}`}>
                      {story.title}
                    </p>
                    {/* Moderator edit/delete — only for PENDING stories */}
                    {isModerator && isPending && (
                      <span className="flex gap-1 opacity-0 group-hover/story:opacity-100 transition-opacity shrink-0">
                        <button
                          onClick={(e) => handleStartEdit(story, e)}
                          className="p-0.5 text-text-muted hover:text-white transition-colors"
                          title="Edit story"
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="w-3 h-3"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                          </svg>
                        </button>
                        <button
                          onClick={(e) => handleDelete(story.id, e)}
                          disabled={isDeleting}
                          className="p-0.5 text-text-muted hover:text-red-400 transition-colors"
                          title="Delete story"
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="w-3 h-3"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </button>
                      </span>
                    )}
                  </div>
                  {/* Description (always visible if present) */}
                  {story.description && (
                    <p className="text-[10px] text-text-muted mt-0.5 truncate">
                      {story.description}
                    </p>
                  )}
                  <div className="flex items-center gap-2 mt-1">
                    <span
                      className={`text-[10px] px-1.5 py-0.5 rounded ${status?.style ?? ''}`}
                    >
                      {status?.label}
                    </span>
                    {(story.status === 'VOTED' || story.status === 'REVEALED') && (
                      <span className="text-xs font-mono font-semibold text-green-400">
                        {story.finalScore != null
                          ? story.finalScore % 1 === 0
                            ? story.finalScore.toFixed(0)
                            : story.finalScore.toFixed(1)
                          : story.finalEstimate ?? '--'}
                        {story.consensusReached && (
                          <span className="ml-1 text-[10px] text-green-500">&#10003;</span>
                        )}
                      </span>
                    )}
                    {story.status === 'VOTING' && story.voteCount != null && story.voteCount > 0 && (
                      <span className="text-[10px] font-mono text-amber-400">
                        {story.voteCount} vote{story.voteCount !== 1 ? 's' : ''}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </motion.button>
          );
        })}
      </div>
    </div>
  );
}
