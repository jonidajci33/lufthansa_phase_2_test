import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { AppShell } from './components/layout/AppShell';
import { AuthGuard } from './components/auth/AuthGuard';
import { AdminGuard } from './components/auth/AdminGuard';
import { LoginPage } from './pages/LoginPage';
import { RoomsPage } from './pages/RoomsPage';
import { CreateRoomPage } from './pages/CreateRoomPage';
import { RoomPage } from './pages/RoomPage';
import { AdminUsersPage } from './pages/admin/AdminUsersPage';
import { AdminRoomsPage } from './pages/admin/AdminRoomsPage';
import { AdminStoriesPage } from './pages/admin/AdminStoriesPage';
import { AdminAuditPage } from './pages/admin/AdminAuditPage';
import { AuthCallbackPage } from './pages/AuthCallbackPage';
import { JoinRoomPage } from './pages/JoinRoomPage';
import { AcceptInvitationPage } from './pages/AcceptInvitationPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <LoginPage />,
  },
  {
    path: '/auth/callback',
    element: <AuthCallbackPage />,
  },
  {
    element: (
      <AuthGuard>
        <AppShell />
      </AuthGuard>
    ),
    children: [
      {
        path: '/rooms',
        element: <RoomsPage />,
      },
      {
        path: '/rooms/create',
        element: <CreateRoomPage />,
      },
      {
        path: '/rooms/join/:shortCode',
        element: <JoinRoomPage />,
      },
      {
        path: '/rooms/:roomId',
        element: <RoomPage />,
      },
      {
        path: '/invitations/:token/accept',
        element: <AcceptInvitationPage />,
      },
      // Admin routes
      {
        path: '/admin/users',
        element: <AdminGuard><AdminUsersPage /></AdminGuard>,
      },
      {
        path: '/admin/rooms',
        element: <AdminGuard><AdminRoomsPage /></AdminGuard>,
      },
      {
        path: '/admin/stories',
        element: <AdminGuard><AdminStoriesPage /></AdminGuard>,
      },
      {
        path: '/admin/audit',
        element: <AdminGuard><AdminAuditPage /></AdminGuard>,
      },
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
