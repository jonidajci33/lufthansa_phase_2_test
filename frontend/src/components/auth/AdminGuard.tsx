import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/useAuthStore';
import type { ReactNode } from 'react';

export function AdminGuard({ children }: { children: ReactNode }) {
  const isAdmin = useAuthStore((s) => s.isAdmin);

  if (!isAdmin) {
    return <Navigate to="/rooms" replace />;
  }

  return <>{children}</>;
}
