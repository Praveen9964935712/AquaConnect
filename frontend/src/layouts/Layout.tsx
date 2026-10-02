import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { dashboardPath } from '../auth/dashboardPath';

export default function Layout({ children }: { children: ReactNode }) {
  const { isAuthenticated, logout, user } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink className="brand" to={user ? dashboardPath(user.roles) : '/login'} aria-label="AquaConnect home">
          <span className="brand-mark" aria-hidden="true">~</span>
          <span>AquaConnect</span>
        </NavLink>
        <nav className="nav">
          {!isAuthenticated ? (
            <>
              <NavLink to="/login">Login</NavLink>
              <NavLink to="/register">Register</NavLink>
            </>
          ) : (
            <>
              {user?.roles.includes('CITIZEN') && <NavLink to="/citizen">My service</NavLink>}
              {user?.roles.includes('OPERATOR') && <NavLink to="/operator">Operations</NavLink>}
              {user?.roles.includes('OPERATIONS_MANAGER') && <NavLink to="/manager">Management</NavLink>}
              {user?.roles.includes('FIELD_ENGINEER') && <NavLink to="/engineer">Field work</NavLink>}
              {user?.roles.includes('ADMIN') && <NavLink to="/admin">Administration</NavLink>}
              {user?.roles.includes('OPERATOR') && <NavLink to="/ivr-simulator">IVR simulator</NavLink>}
              <span className="user-chip">{user?.email}</span>
              <button className="nav-logout" type="button" onClick={logout}>Sign out</button>
            </>
          )}
        </nav>
      </header>
      <main className="page-shell">{children}</main>
    </div>
  );
}
