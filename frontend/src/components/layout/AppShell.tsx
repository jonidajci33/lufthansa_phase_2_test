import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { ConnectionBanner } from '../ui/ConnectionBanner';

/**
 * Main application layout: header + content area.
 * Renders the active route via <Outlet />.
 */
export function AppShell() {
  return (
    <div className="min-h-screen flex flex-col bg-surface">
      <Header />
      <ConnectionBanner />
      <main className="flex-1 flex flex-col">
        <Outlet />
      </main>
    </div>
  );
}
