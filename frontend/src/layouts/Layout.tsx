import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout({ children }: { children: ReactNode }) {
  const { isAuthenticated, logout, user } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">AquaConnect</div>
        <nav className="nav">
          {!isAuthenticated ? (
            <>
              <NavLink to="/login">Login</NavLink>
              <NavLink to="/register">Register</NavLink>
            </>
          ) : (
            <>
              {user?.roles.includes('CITIZEN') && <NavLink to="/citizen">Citizen</NavLink>}
              {user?.roles.includes('OPERATOR') && <NavLink to="/operator">Operator</NavLink>}
              {user?.roles.includes('OPERATIONS_MANAGER') && <NavLink to="/manager">Manager</NavLink>}
              {user?.roles.includes('FIELD_ENGINEER') && <NavLink to="/engineer">Field work</NavLink>}
              {user?.roles.includes('ADMIN') && <NavLink to="/admin">Admin</NavLink>}
              <button type="button" onClick={logout}>Logout</button>
            </>
          )}
        </nav>
      </header>
      <main className="page-shell">{children}</main>
    </div>
  );
}
