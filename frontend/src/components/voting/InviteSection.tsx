import { useState } from 'react';
import { inviteUser, generateShareLink } from '../../api/rooms';
import type { ShareLinkResponse } from '../../api/rooms';

interface InviteSectionProps {
  roomId: string;
}

export function InviteSection({ roomId }: InviteSectionProps) {
  const [email, setEmail] = useState('');
  const [isInviting, setIsInviting] = useState(false);
  const [inviteSuccess, setInviteSuccess] = useState<string | null>(null);
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [shareLink, setShareLink] = useState<ShareLinkResponse | null>(null);
  const [copied, setCopied] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;

    setIsInviting(true);
    setInviteError(null);
    setInviteSuccess(null);

    try {
      await inviteUser(roomId, { email: email.trim(), type: 'EMAIL' });
      setInviteSuccess(`Invitation sent to ${email.trim()}`);
      setEmail('');
    } catch (err: any) {
      const message =
        err?.response?.data?.message ?? 'Failed to send invitation';
      setInviteError(message);
    } finally {
      setIsInviting(false);
    }
  };

  const handleGenerateLink = async () => {
    setIsGenerating(true);
    try {
      const link = await generateShareLink(roomId);
      setShareLink(link);
    } catch {
      setInviteError('Failed to generate share link');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleCopyLink = async () => {
    if (!shareLink) return;
    try {
      await navigator.clipboard.writeText(shareLink.shareLink);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback: select text for manual copy
    }
  };

  return (
    <div className="mt-4 pt-4 border-t border-border">
      <h4 className="text-xs font-medium text-text-secondary uppercase tracking-wider mb-3">
        Invite
      </h4>

      {/* Email invite form */}
      <form onSubmit={handleInvite} className="space-y-2">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="participant@email.com"
          className="w-full px-2.5 py-1.5 bg-surface border border-border rounded text-sm text-white placeholder:text-text-muted focus:outline-none focus:border-border-hover"
        />
        <button
          type="submit"
          disabled={isInviting || !email.trim()}
          className="w-full py-1.5 bg-surface-tertiary border border-border hover:border-border-hover text-white rounded text-xs font-medium transition-colors disabled:opacity-50"
        >
          {isInviting ? 'Sending...' : 'Send Invite'}
        </button>
      </form>

      {/* Feedback messages */}
      {inviteSuccess && (
        <p className="mt-2 text-xs text-green-400">{inviteSuccess}</p>
      )}
      {inviteError && (
        <p className="mt-2 text-xs text-red-400">{inviteError}</p>
      )}

      {/* Share link section */}
      <div className="mt-3">
        {!shareLink ? (
          <button
            onClick={handleGenerateLink}
            disabled={isGenerating}
            className="w-full py-1.5 border border-border hover:border-border-hover text-text-secondary hover:text-white rounded text-xs transition-colors disabled:opacity-50"
          >
            {isGenerating ? 'Generating...' : 'Generate Share Link'}
          </button>
        ) : (
          <div className="space-y-1.5">
            <div className="flex items-center gap-1.5">
              <input
                type="text"
                readOnly
                value={shareLink.shareLink}
                className="flex-1 px-2 py-1 bg-surface-tertiary border border-border rounded text-xs text-text-muted font-mono truncate"
              />
              <button
                onClick={handleCopyLink}
                className="shrink-0 px-2 py-1 border border-border hover:border-border-hover text-xs text-text-secondary hover:text-white rounded transition-colors"
              >
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
