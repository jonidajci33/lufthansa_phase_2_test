import { useState } from 'react';
import { useAuthStore } from '../stores/useAuthStore';
import { Navigate, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { springs } from '../lib/springs';
import { startSsoLogin } from '../lib/sso';

type Tab = 'login' | 'register';

export function LoginPage() {
  const { isAuthenticated, isLoading, error } = useAuthStore();
  const login = useAuthStore((s) => s.login);
  const register = useAuthStore((s) => s.register);
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<Tab>('login');

  // Login form
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register form
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regFirstName, setRegFirstName] = useState('');
  const [regLastName, setRegLastName] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');
  const [formError, setFormError] = useState('');

  if (isAuthenticated) {
    const returnTo = sessionStorage.getItem('pp_return_to') || '/rooms';
    sessionStorage.removeItem('pp_return_to');
    return <Navigate to={returnTo} replace />;
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    try {
      await login(loginUsername, loginPassword);
      const returnTo = sessionStorage.getItem('pp_return_to') || '/rooms';
      sessionStorage.removeItem('pp_return_to');
      navigate(returnTo);
    } catch {
      // error is set in store
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');

    if (regPassword !== regConfirm) {
      setFormError('Passwords do not match');
      return;
    }
    if (regPassword.length < 8) {
      setFormError('Password must be at least 8 characters');
      return;
    }

    try {
      await register(regUsername, regEmail, regPassword, regFirstName, regLastName);
      const returnTo = sessionStorage.getItem('pp_return_to') || '/rooms';
      sessionStorage.removeItem('pp_return_to');
      navigate(returnTo);
    } catch {
      // error is set in store
    }
  };

  const displayError = formError || error;

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface px-4">
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={springs.smooth}
        className="w-full max-w-md"
      >
        {/* Logo */}
        <div className="text-center mb-10">
          <motion.div
            className="mx-auto w-20 h-28 rounded-lg border-2 border-border bg-surface-secondary mb-6 flex items-center justify-center perspective-1000"
            animate={{ rotateY: [0, 180, 0] }}
            transition={{
              duration: 3,
              repeat: Infinity,
              repeatDelay: 3,
              ease: 'easeInOut',
            }}
          >
            <span className="text-2xl font-mono font-bold text-white">?</span>
          </motion.div>

          <h1 className="text-4xl font-bold text-white tracking-tight mb-2">
            Planning Poker
          </h1>
          <p className="text-text-secondary text-sm">
            Collaborative estimation for agile teams
          </p>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-border mb-8">
          <button
            onClick={() => { setActiveTab('login'); setFormError(''); }}
            className={`flex-1 pb-3 text-sm font-medium transition-colors duration-150 border-b-2 ${
              activeTab === 'login'
                ? 'text-white border-white'
                : 'text-text-muted border-transparent hover:text-text-secondary'
            }`}
          >
            Sign In
          </button>
          <button
            onClick={() => { setActiveTab('register'); setFormError(''); }}
            className={`flex-1 pb-3 text-sm font-medium transition-colors duration-150 border-b-2 ${
              activeTab === 'register'
                ? 'text-white border-white'
                : 'text-text-muted border-transparent hover:text-text-secondary'
            }`}
          >
            Create Account
          </button>
        </div>

        {/* Forms */}
        <AnimatePresence mode="wait">
          {activeTab === 'login' ? (
            <motion.form
              key="login"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.2 }}
              onSubmit={handleLogin}
              className="space-y-4"
            >
              <div>
                <label htmlFor="login-username" className="block text-xs text-text-secondary mb-1.5">
                  Username
                </label>
                <input
                  id="login-username"
                  type="text"
                  value={loginUsername}
                  onChange={(e) => setLoginUsername(e.target.value)}
                  required
                  autoComplete="username"
                  className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                             text-white text-sm placeholder-text-muted
                             focus:outline-none focus:border-white transition-colors"
                  placeholder="Enter your username"
                />
              </div>

              <div>
                <label htmlFor="login-password" className="block text-xs text-text-secondary mb-1.5">
                  Password
                </label>
                <input
                  id="login-password"
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                  className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                             text-white text-sm placeholder-text-muted
                             focus:outline-none focus:border-white transition-colors"
                  placeholder="Enter your password"
                />
              </div>

              <motion.button
                type="submit"
                disabled={isLoading || !loginUsername || !loginPassword}
                className="w-full py-3 px-6 rounded-lg bg-white text-black font-semibold text-sm
                           hover:bg-gray-200 transition-colors duration-150
                           disabled:opacity-50 disabled:cursor-not-allowed mt-2"
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
              >
                {isLoading ? 'Signing in...' : 'Sign In'}
              </motion.button>
            </motion.form>
          ) : (
            <motion.form
              key="register"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
              onSubmit={handleRegister}
              className="space-y-4"
            >
              <div>
                <label htmlFor="reg-username" className="block text-xs text-text-secondary mb-1.5">
                  Username
                </label>
                <input
                  id="reg-username"
                  type="text"
                  value={regUsername}
                  onChange={(e) => setRegUsername(e.target.value)}
                  required
                  autoComplete="username"
                  className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                             text-white text-sm placeholder-text-muted
                             focus:outline-none focus:border-white transition-colors"
                  placeholder="Choose a username"
                />
              </div>

              <div>
                <label htmlFor="reg-email" className="block text-xs text-text-secondary mb-1.5">
                  Email
                </label>
                <input
                  id="reg-email"
                  type="email"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  required
                  autoComplete="email"
                  className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                             text-white text-sm placeholder-text-muted
                             focus:outline-none focus:border-white transition-colors"
                  placeholder="you@example.com"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label htmlFor="reg-first-name" className="block text-xs text-text-secondary mb-1.5">
                    First Name
                  </label>
                  <input
                    id="reg-first-name"
                    type="text"
                    value={regFirstName}
                    onChange={(e) => setRegFirstName(e.target.value)}
                    required
                    autoComplete="given-name"
                    className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                               text-white text-sm placeholder-text-muted
                               focus:outline-none focus:border-white transition-colors"
                    placeholder="First name"
                  />
                </div>
                <div>
                  <label htmlFor="reg-last-name" className="block text-xs text-text-secondary mb-1.5">
                    Last Name
                  </label>
                  <input
                    id="reg-last-name"
                    type="text"
                    value={regLastName}
                    onChange={(e) => setRegLastName(e.target.value)}
                    required
                    autoComplete="family-name"
                    className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                               text-white text-sm placeholder-text-muted
                               focus:outline-none focus:border-white transition-colors"
                    placeholder="Last name"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="reg-password" className="block text-xs text-text-secondary mb-1.5">
                  Password
                </label>
                <input
                  id="reg-password"
                  type="password"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  required
                  minLength={6}
                  autoComplete="new-password"
                  className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                             text-white text-sm placeholder-text-muted
                             focus:outline-none focus:border-white transition-colors"
                  placeholder="Min. 6 characters"
                />
              </div>

              <div>
                <label htmlFor="reg-confirm" className="block text-xs text-text-secondary mb-1.5">
                  Confirm Password
                </label>
                <input
                  id="reg-confirm"
                  type="password"
                  value={regConfirm}
                  onChange={(e) => setRegConfirm(e.target.value)}
                  required
                  autoComplete="new-password"
                  className="w-full px-4 py-2.5 rounded-lg bg-surface-secondary border border-border
                             text-white text-sm placeholder-text-muted
                             focus:outline-none focus:border-white transition-colors"
                  placeholder="Repeat your password"
                />
              </div>

              <motion.button
                type="submit"
                disabled={isLoading || !regUsername || !regEmail || !regFirstName || !regLastName || !regPassword || !regConfirm}
                className="w-full py-3 px-6 rounded-lg bg-white text-black font-semibold text-sm
                           hover:bg-gray-200 transition-colors duration-150
                           disabled:opacity-50 disabled:cursor-not-allowed mt-2"
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
              >
                {isLoading ? 'Creating account...' : 'Create Account'}
              </motion.button>

              {/* Features */}
              <div className="pt-4 border-t border-border mt-4">
                <div className="grid grid-cols-2 gap-2 text-xs text-text-secondary">
                  <div className="flex items-center gap-2">
                    <span className="text-white">&#10003;</span> Create rooms
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-white">&#10003;</span> Real-time voting
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-white">&#10003;</span> Multiple deck types
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-white">&#10003;</span> Invite teammates
                  </div>
                </div>
              </div>
            </motion.form>
          )}
        </AnimatePresence>

        {/* Error */}
        {displayError && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-4 p-3 rounded-lg border border-red-500/30 bg-red-500/10"
          >
            <p className="text-red-400 text-sm">{displayError}</p>
          </motion.div>
        )}

        {/* SSO Divider */}
        <div className="mt-8 flex items-center gap-4">
          <div className="flex-1 h-px bg-border" />
          <span className="text-xs text-text-muted">or continue with</span>
          <div className="flex-1 h-px bg-border" />
        </div>

        {/* SSO Buttons */}
        <div className="mt-4 flex flex-col gap-3">
          <button
            onClick={() => startSsoLogin('google')}
            className="w-full flex items-center justify-center gap-3 py-2.5 px-4 rounded-lg border border-border bg-surface-secondary hover:bg-surface-tertiary transition-colors"
          >
            <svg className="w-5 h-5" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" />
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
            </svg>
            <span className="text-sm text-white">Sign in with Google</span>
          </button>

          <button
            onClick={() => startSsoLogin('facebook')}
            className="w-full flex items-center justify-center gap-3 py-2.5 px-4 rounded-lg border border-border bg-surface-secondary hover:bg-surface-tertiary transition-colors"
          >
            <svg className="w-5 h-5" viewBox="0 0 24 24" fill="#1877F2">
              <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
            </svg>
            <span className="text-sm text-white">Sign in with Facebook</span>
          </button>

          <button
            onClick={() => startSsoLogin()}
            className="w-full py-2.5 px-4 rounded-lg border border-border text-text-muted text-sm hover:text-white hover:border-border-hover transition-colors"
          >
            Sign in with Keycloak
          </button>
        </div>

        <p className="mt-6 text-center text-xs text-text-muted">
          Secured by Keycloak &middot; All traffic via API Gateway
        </p>
      </motion.div>
    </div>
  );
}
