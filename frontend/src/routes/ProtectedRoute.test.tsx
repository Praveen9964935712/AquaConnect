import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';

const { authState } = vi.hoisted(() => ({ authState: {
  isAuthenticated: true,
  user: null as null | { id: string; email: string; username: string; roles: string[] },
  hasRole: ((role: string): boolean => role === 'CITIZEN') as (role: string) => boolean
} }));
authState.user = { id: 'citizen-1', email: 'citizen@example.com', username: 'citizen', roles: ['CITIZEN'] };
vi.mock('../auth/AuthContext', () => ({ useAuth: () => authState }));

describe('ProtectedRoute', () => {
  it('returns an authenticated user to their own role dashboard', () => {
    render(<MemoryRouter initialEntries={['/operator']}><Routes>
      <Route path="/operator" element={<ProtectedRoute requiredRole="OPERATOR"><h1>Operator only</h1></ProtectedRoute>} />
      <Route path="/citizen" element={<h1>Citizen service home</h1>} />
    </Routes></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Citizen service home/i })).toBeTruthy();
    expect(screen.queryByRole('heading', { name: /Operator only/i })).toBeNull();
  });

  it('redirects an unauthenticated user to sign in', () => {
    authState.isAuthenticated = false;
    authState.user = null;
    authState.hasRole = () => false;
    render(<MemoryRouter initialEntries={['/citizen']}><Routes>
      <Route path="/citizen" element={<ProtectedRoute requiredRole="CITIZEN"><h1>Citizen service home</h1></ProtectedRoute>} />
      <Route path="/login" element={<h1>Sign in</h1>} />
    </Routes></MemoryRouter>);
    expect(screen.getByRole('heading', { name: /Sign in/i })).toBeTruthy();
  });
});
