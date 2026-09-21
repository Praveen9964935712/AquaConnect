import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

interface ProtectedRouteProps {
  requiredRole?: 'CITIZEN' | 'OPERATOR' | 'OPERATIONS_MANAGER' | 'FIELD_ENGINEER' | 'ADMIN';
  children: JSX.Element;
}

export default function ProtectedRoute({ requiredRole, children }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
