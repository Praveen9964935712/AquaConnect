import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { dashboardPath } from '../auth/dashboardPath';

interface ProtectedRouteProps {
  requiredRole?: 'CITIZEN' | 'OPERATOR' | 'OPERATIONS_MANAGER' | 'FIELD_ENGINEER' | 'ADMIN';
  children: JSX.Element;
}

export default function ProtectedRoute({ requiredRole, children }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole, user, sessionExpired } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={sessionExpired ? '/login?expired=1' : '/login'} replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to={user ? dashboardPath(user.roles) : '/login'} replace />;
  }

  return children;
}
