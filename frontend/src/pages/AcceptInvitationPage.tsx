import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { acceptInvitation } from '../api/rooms';

export function AcceptInvitationPage() {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;

    acceptInvitation(token)
      .then((room) => {
        navigate(`/rooms/${room.id}`, { replace: true });
      })
      .catch((err) => {
        const msg =
          err?.response?.data?.message || 'Failed to accept invitation. It may be expired or already used.';
        setError(msg);
      });
  }, [token, navigate]);

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="bg-white rounded-lg shadow-md p-8 max-w-md text-center">
          <h2 className="text-xl font-semibold text-red-600 mb-2">Invitation Error</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={() => navigate('/rooms')}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
          >
            Go to My Rooms
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <div className="text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mx-auto mb-4" />
        <p className="text-gray-600">Accepting invitation...</p>
      </div>
    </div>
  );
}
