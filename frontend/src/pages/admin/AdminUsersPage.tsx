import { useEffect, useState } from 'react';
import { fetchUsers, deactivateUser, updateUser } from '../../api/admin';
import { PageTransition } from '../../components/animations/PageTransition';
import type { UserProfile } from '../../types/api';

export function AdminUsersPage() {
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editForm, setEditForm] = useState({ displayName: '', avatarUrl: '' });
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const loadUsers = () => {
    setIsLoading(true);
    fetchUsers(0, 50).then((page) => {
      setUsers(page.data);
      setIsLoading(false);
    }).catch(() => setIsLoading(false));
  };

  useEffect(() => { loadUsers(); }, []);

  const handleDeactivate = async (userId: string, username: string) => {
    if (!confirm(`Deactivate user "${username}"? This will disable their Keycloak account.`)) return;
    try {
      await deactivateUser(userId);
      setUsers(users.map((u) => u.id === userId ? { ...u, isActive: false } : u));
    } catch {
      alert('Failed to deactivate user');
    }
  };

  const startEditing = (user: UserProfile) => {
    setEditingId(user.id);
    setEditForm({
      displayName: user.displayName || '',
      avatarUrl: user.avatarUrl || '',
    });
  };

  const handleSave = async (userId: string) => {
    try {
      const updated = await updateUser(userId, {
        displayName: editForm.displayName || undefined,
        avatarUrl: editForm.avatarUrl || undefined,
      });
      setUsers(users.map((u) => u.id === userId ? { ...u, ...updated } : u));
      setEditingId(null);
    } catch {
      alert('Failed to update user');
    }
  };

  return (
    <PageTransition>
      <div className="max-w-5xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-2">User Management</h1>
        <p className="text-sm text-text-secondary mb-6">
          View, edit, and manage all platform users ({users.length} total)
        </p>

        {isLoading ? (
          <div className="flex justify-center py-16">
            <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <div className="space-y-2">
            {users.map((user) => (
              <div
                key={user.id}
                className={`rounded-md border border-border bg-surface-secondary ${
                  user.isActive === false ? 'opacity-50' : ''
                }`}
              >
                {/* Summary row */}
                <div
                  className="grid grid-cols-[auto_1fr_1fr_auto_auto] gap-4 items-center px-4 py-3 cursor-pointer hover:bg-surface-tertiary transition-colors"
                  onClick={() => setExpandedId(expandedId === user.id ? null : user.id)}
                >
                  {/* Avatar */}
                  <div className="w-9 h-9 rounded-full bg-surface-tertiary border border-border flex items-center justify-center text-sm font-bold text-text-secondary">
                    {(user.username ?? '?').charAt(0).toUpperCase()}
                  </div>

                  {/* Name + Username */}
                  <div>
                    <span className="text-sm text-white font-medium">{user.username}</span>
                    {user.displayName && user.displayName !== user.username && (
                      <span className="text-xs text-text-muted ml-2">({user.displayName})</span>
                    )}
                    {user.isActive === false && (
                      <span className="ml-2 text-[10px] px-1.5 py-0.5 rounded bg-red-500/20 text-red-400">
                        Inactive
                      </span>
                    )}
                  </div>

                  {/* Email */}
                  <span className="text-sm text-text-secondary truncate">{user.email}</span>

                  {/* Roles */}
                  <div className="flex gap-1">
                    {user.roles?.map((role) => (
                      <span
                        key={role}
                        className={`text-[10px] px-1.5 py-0.5 rounded ${
                          role === 'ADMIN' ? 'bg-white/10 text-white font-medium' : 'bg-surface-tertiary text-text-muted'
                        }`}
                      >
                        {role}
                      </span>
                    ))}
                  </div>

                  {/* Expand arrow */}
                  <span className="text-text-muted text-xs">{expandedId === user.id ? '▲' : '▼'}</span>
                </div>

                {/* Expanded detail panel */}
                {expandedId === user.id && (
                  <div className="px-4 pb-4 border-t border-border">
                    <div className="grid grid-cols-2 gap-x-8 gap-y-3 mt-3">
                      {/* Left column: details */}
                      <div className="space-y-2">
                        <div className="flex justify-between text-xs">
                          <span className="text-text-muted">ID (Keycloak)</span>
                          <span className="text-text-secondary font-mono">{user.id}</span>
                        </div>
                        <div className="flex justify-between text-xs">
                          <span className="text-text-muted">First Name</span>
                          <span className="text-text-secondary">{user.firstName}</span>
                        </div>
                        <div className="flex justify-between text-xs">
                          <span className="text-text-muted">Last Name</span>
                          <span className="text-text-secondary">{user.lastName}</span>
                        </div>
                        <div className="flex justify-between text-xs">
                          <span className="text-text-muted">Display Name</span>
                          <span className="text-text-secondary">{user.displayName || '—'}</span>
                        </div>
                        <div className="flex justify-between text-xs">
                          <span className="text-text-muted">Created</span>
                          <span className="text-text-secondary">
                            {user.createdAt ? new Date(user.createdAt).toLocaleString() : '—'}
                          </span>
                        </div>
                        <div className="flex justify-between text-xs">
                          <span className="text-text-muted">Status</span>
                          <span className={user.isActive !== false ? 'text-green-400' : 'text-red-400'}>
                            {user.isActive !== false ? 'Active' : 'Inactive'}
                          </span>
                        </div>
                      </div>

                      {/* Right column: edit form */}
                      <div>
                        {editingId === user.id ? (
                          <div className="space-y-2">
                            <div>
                              <label className="text-[10px] text-text-muted uppercase block mb-1">Display Name</label>
                              <input
                                type="text"
                                value={editForm.displayName}
                                onChange={(e) => setEditForm({ ...editForm, displayName: e.target.value })}
                                className="w-full px-2 py-1.5 bg-surface border border-border rounded text-sm text-white focus:outline-none focus:border-border-hover"
                              />
                            </div>
                            <div>
                              <label className="text-[10px] text-text-muted uppercase block mb-1">Avatar URL</label>
                              <input
                                type="text"
                                value={editForm.avatarUrl}
                                onChange={(e) => setEditForm({ ...editForm, avatarUrl: e.target.value })}
                                placeholder="https://..."
                                className="w-full px-2 py-1.5 bg-surface border border-border rounded text-sm text-white placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                              />
                            </div>
                            <div className="flex gap-2 mt-2">
                              <button
                                onClick={() => handleSave(user.id)}
                                className="text-xs px-3 py-1.5 bg-white text-black rounded hover:bg-gray-200 transition-colors"
                              >
                                Save
                              </button>
                              <button
                                onClick={() => setEditingId(null)}
                                className="text-xs px-3 py-1.5 text-text-muted hover:text-white transition-colors"
                              >
                                Cancel
                              </button>
                            </div>
                          </div>
                        ) : (
                          <div className="flex flex-col gap-2">
                            <button
                              onClick={() => startEditing(user)}
                              className="text-xs px-3 py-1.5 border border-border text-text-secondary rounded hover:border-border-hover hover:text-white transition-colors w-fit"
                            >
                              Edit Profile
                            </button>
                            {user.isActive !== false && (
                              <button
                                onClick={() => handleDeactivate(user.id, user.username)}
                                className="text-xs px-3 py-1.5 border border-red-500/30 text-red-400 rounded hover:bg-red-500/10 transition-colors w-fit"
                              >
                                Deactivate Account
                              </button>
                            )}
                          </div>
                        )}
                      </div>
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
