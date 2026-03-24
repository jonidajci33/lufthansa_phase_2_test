import { useEffect, useState } from 'react';
import { fetchAllStories } from '../../api/admin';
import { PageTransition } from '../../components/animations/PageTransition';
import type { Story } from '../../types/api';

export function AdminStoriesPage() {
  const [stories, setStories] = useState<Story[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchAllStories(0, 50).then((page) => {
      setStories(page.data);
      setIsLoading(false);
    }).catch(() => setIsLoading(false));
  }, []);

  const statusColor: Record<string, string> = {
    PENDING: 'bg-surface-tertiary text-text-muted',
    VOTING: 'bg-white text-black',
    REVEALED: 'bg-surface-tertiary text-text-secondary',
    SKIPPED: 'bg-surface-tertiary text-text-muted',
  };

  return (
    <PageTransition>
      <div className="max-w-4xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-2">Story Oversight</h1>
        <p className="text-sm text-text-secondary mb-6">Read-only view of all stories across all rooms</p>

        {isLoading ? (
          <div className="flex justify-center py-16">
            <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : stories.length === 0 ? (
          <p className="text-text-secondary text-center py-16">No stories found</p>
        ) : (
          <div className="space-y-2">
            <div className="grid grid-cols-[1fr_auto_auto] gap-4 px-4 py-2 text-xs text-text-muted uppercase tracking-wider">
              <span>Story</span>
              <span>Status</span>
              <span>Estimate</span>
            </div>
            {stories.map((story) => (
              <div
                key={story.id}
                className="grid grid-cols-[1fr_auto_auto] gap-4 items-center px-4 py-3 rounded-md border border-border bg-surface-secondary"
              >
                <div>
                  <span className="text-sm text-white font-medium">{story.title}</span>
                  <span className="text-xs text-text-muted ml-2 font-mono">Room: {story.roomId.substring(0, 8)}</span>
                  {story.description && (
                    <p className="text-xs text-text-muted mt-0.5 truncate max-w-md">{story.description}</p>
                  )}
                </div>
                <span className={`text-[10px] px-1.5 py-0.5 rounded ${statusColor[story.status] ?? ''}`}>
                  {story.status}
                </span>
                <span className="text-sm font-mono text-text-secondary">
                  {story.finalEstimate ?? '—'}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </PageTransition>
  );
}
