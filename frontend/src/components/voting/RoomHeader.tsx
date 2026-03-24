import { useState, useRef, useEffect } from 'react';

interface RoomHeaderProps {
  name: string;
  description?: string;
  shortCode: string;
  isModerator: boolean;
  onSave: (updates: { name?: string; description?: string }) => Promise<void>;
}

export function RoomHeader({
  name,
  description,
  shortCode,
  isModerator,
  onSave,
}: RoomHeaderProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState(name);
  const [editDescription, setEditDescription] = useState(description ?? '');
  const [isSaving, setIsSaving] = useState(false);
  const nameRef = useRef<HTMLInputElement>(null);

  // Sync local state when props change (e.g., after save)
  useEffect(() => {
    if (!isEditing) {
      setEditName(name);
      setEditDescription(description ?? '');
    }
  }, [name, description, isEditing]);

  const handleStartEdit = () => {
    if (!isModerator) return;
    setIsEditing(true);
    // Focus name input after render
    setTimeout(() => nameRef.current?.focus(), 0);
  };

  const handleSave = async () => {
    const updates: { name?: string; description?: string } = {};
    if (editName.trim() && editName.trim() !== name) {
      updates.name = editName.trim();
    }
    if (editDescription.trim() !== (description ?? '')) {
      updates.description = editDescription.trim();
    }

    if (Object.keys(updates).length === 0) {
      setIsEditing(false);
      return;
    }

    setIsSaving(true);
    try {
      await onSave(updates);
      setIsEditing(false);
    } catch {
      // Keep editing open so user can retry
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    setEditName(name);
    setEditDescription(description ?? '');
    setIsEditing(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSave();
    }
    if (e.key === 'Escape') {
      handleCancel();
    }
  };

  if (isEditing) {
    return (
      <div className="flex-1">
        <input
          ref={nameRef}
          type="text"
          value={editName}
          onChange={(e) => setEditName(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Room name..."
          className="w-full px-2 py-1 bg-surface border border-border rounded text-lg font-semibold text-white placeholder:text-text-muted focus:outline-none focus:border-border-hover"
        />
        <input
          type="text"
          value={editDescription}
          onChange={(e) => setEditDescription(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Description (optional)..."
          className="w-full mt-1 px-2 py-1 bg-surface border border-border rounded text-xs text-text-secondary placeholder:text-text-muted focus:outline-none focus:border-border-hover"
        />
        <div className="flex gap-2 mt-1.5">
          <button
            onClick={handleSave}
            disabled={isSaving || !editName.trim()}
            className="text-xs px-2 py-0.5 bg-white text-black rounded hover:bg-gray-200 transition-colors disabled:opacity-50"
          >
            {isSaving ? 'Saving...' : 'Save'}
          </button>
          <button
            onClick={handleCancel}
            className="text-xs px-2 py-0.5 text-text-muted hover:text-white transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-start gap-2 group">
      <div>
        <h2 className="text-lg font-semibold text-white">{name}</h2>
        {description && (
          <p className="text-xs text-text-secondary mt-0.5">{description}</p>
        )}
        <span className="text-xs text-text-muted font-mono">
          Code: {shortCode}
        </span>
      </div>
      {isModerator && (
        <button
          onClick={handleStartEdit}
          className="opacity-0 group-hover:opacity-100 transition-opacity mt-1 p-1 text-text-muted hover:text-white"
          title="Edit room name and description"
        >
          {/* Pencil icon (inline SVG) */}
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="w-3.5 h-3.5"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
          </svg>
        </button>
      )}
    </div>
  );
}
