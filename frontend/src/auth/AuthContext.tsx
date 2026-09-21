import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { type AuthResponse, type Role } from '../types/auth';

interface AuthContextValue {
  token: string | null;
  user: { id: string; username: string; email: string; roles: Role[] } | null;
  login: (response: AuthResponse) => void;
  logout: () => void;
  isAuthenticated: boolean;
  hasRole: (role: Role) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('aquaconnect.jwt'));
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

  const value = useMemo<AuthContextValue>(() => ({
    token,
    user,
    isAuthenticated: Boolean(token && user),
    login: (response) => {
      setToken(response.token);
      setUser({ id: response.userId, username: response.email, email: response.email, roles: response.roles });
    },
    logout: () => {
      setToken(null);
      setUser(null);
    },
    hasRole: (role) => Boolean(user && user.roles.includes(role))
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
