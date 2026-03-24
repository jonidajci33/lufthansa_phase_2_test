import { useEffect, useState } from 'react';
import { fetchAuditEntries } from '../../api/admin';
import { PageTransition } from '../../components/animations/PageTransition';
import type { AuditEntry } from '../../types/api';

export function AdminAuditPage() {
  const [entries, setEntries] = useState<AuditEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [entityTypeFilter, setEntityTypeFilter] = useState('');
  const [operationFilter, setOperationFilter] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const loadEntries = () => {
    setIsLoading(true);
    fetchAuditEntries({
      offset: 0,
      limit: 50,
      entityType: entityTypeFilter || undefined,
      operation: operationFilter || undefined,
    }).then((page) => {
      setEntries(page.data);
      setIsLoading(false);
    }).catch(() => setIsLoading(false));
  };

  useEffect(() => { loadEntries(); }, [entityTypeFilter, operationFilter]);

  const opColor: Record<string, string> = {
    CREATE: 'text-green-400',
    UPDATE: 'text-yellow-400',
    DELETE: 'text-red-400',
  };

  return (
    <PageTransition>
      <div className="max-w-5xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-2">Audit Trail</h1>
        <p className="text-sm text-text-secondary mb-6">Full history of create, update, and delete operations</p>

        {/* Filters */}
        <div className="flex gap-3 mb-6">
          <select
            value={entityTypeFilter}
            onChange={(e) => setEntityTypeFilter(e.target.value)}
            className="px-3 py-1.5 bg-surface-secondary border border-border rounded text-sm text-white focus:outline-none focus:border-border-hover"
          >
            <option value="">All Entities</option>
            <option value="User">User</option>
            <option value="Room">Room</option>
            <option value="Story">Story</option>
            <option value="Vote">Vote</option>
            <option value="DeckType">DeckType</option>
          </select>
          <select
            value={operationFilter}
            onChange={(e) => setOperationFilter(e.target.value)}
            className="px-3 py-1.5 bg-surface-secondary border border-border rounded text-sm text-white focus:outline-none focus:border-border-hover"
          >
            <option value="">All Operations</option>
            <option value="CREATE">CREATE</option>
            <option value="UPDATE">UPDATE</option>
            <option value="DELETE">DELETE</option>
          </select>
        </div>

        {isLoading ? (
          <div className="flex justify-center py-16">
            <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : entries.length === 0 ? (
          <p className="text-text-secondary text-center py-16">No audit entries found</p>
        ) : (
          <div className="space-y-1">
            {entries.map((entry) => (
              <div key={entry.id} className="rounded-md border border-border bg-surface-secondary">
                <button
                  onClick={() => setExpandedId(expandedId === entry.id ? null : entry.id)}
                  className="w-full grid grid-cols-[auto_1fr_auto_auto] gap-4 items-center px-4 py-3 text-left hover:bg-surface-tertiary transition-colors"
                >
                  <span className={`text-xs font-mono font-bold ${opColor[entry.operation] ?? 'text-text-muted'}`}>
                    {entry.operation}
                  </span>
                  <span className="text-sm text-white">
                    {entry.entityType}
                    <span className="text-text-muted ml-1 font-mono text-xs">
                      {entry.entityId.substring(0, 8)}
                    </span>
                  </span>
                  <span className="text-xs text-text-muted font-mono">
                    {entry.userId?.substring(0, 8) ?? '—'}
                  </span>
                  <span className="text-xs text-text-muted">
                    {new Date(entry.timestamp).toLocaleString()}
                  </span>
                </button>
                {expandedId === entry.id && (
                  <div className="px-4 pb-4 border-t border-border">
                    <div className="grid grid-cols-2 gap-4 mt-3">
                      {entry.previousState && (
                        <div>
                          <span className="text-[10px] text-text-muted uppercase tracking-wider block mb-1">Previous</span>
                          <pre className="text-xs text-text-secondary bg-surface p-2 rounded overflow-auto max-h-40">
                            {JSON.stringify(entry.previousState, null, 2)}
                          </pre>
                        </div>
                      )}
                      {entry.newState && (
                        <div>
                          <span className="text-[10px] text-text-muted uppercase tracking-wider block mb-1">New</span>
                          <pre className="text-xs text-text-secondary bg-surface p-2 rounded overflow-auto max-h-40">
                            {JSON.stringify(entry.newState, null, 2)}
                          </pre>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </PageTransition>
  );
}
