import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { SESSION_EXPIRED_EVENT } from '../api/client';
import { type AuthResponse, type Role } from '../types/auth';

interface AuthContextValue {
  token: string | null;
  user: { id: string; username: string; email: string; roles: Role[] } | null;
  login: (response: AuthResponse) => void;
  logout: () => void;
  isAuthenticated: boolean;
  sessionExpired: boolean;
  hasRole: (role: Role) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('aquaconnect.jwt'));
  const [sessionExpired, setSessionExpired] = useState(false);
  const [user, setUser] = useState<AuthContextValue['user'] | null>(() => {
    const raw = localStorage.getItem('aquaconnect.user');
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (token) {
      localStorage.setItem('aquaconnect.jwt', token);
    } else {
      localStorage.removeItem('aquaconnect.jwt');
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('aquaconnect.user', JSON.stringify(user));
    } else {
      localStorage.removeItem('aquaconnect.user');
    }
  }, [user]);

  useEffect(() => {
    const expireSession = () => {
      setToken(null);
      setUser(null);
      setSessionExpired(true);
    };
    window.addEventListener(SESSION_EXPIRED_EVENT, expireSession);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, expireSession);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    token,
    user,
    isAuthenticated: Boolean(token && user),
    sessionExpired,
    login: (response) => {
      setSessionExpired(false);
      setToken(response.token);
      setUser({ id: response.userId, username: response.email, email: response.email, roles: response.roles });
    },
    logout: () => {
      setSessionExpired(false);
      setToken(null);
      setUser(null);
    },
    hasRole: (role) => Boolean(user && user.roles.includes(role))
  }), [token, user, sessionExpired]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
