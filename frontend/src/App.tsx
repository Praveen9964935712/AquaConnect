import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import Layout from './layouts/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CitizenDashboardPage from './pages/CitizenDashboardPage';
import OperatorDashboardPage from './pages/OperatorDashboardPage';
import ManagerDashboardPage from './pages/ManagerDashboardPage';
import FieldEngineerDashboardPage from './pages/FieldEngineerDashboardPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import ProtectedRoute from './routes/ProtectedRoute';

export default function App() {
  return (
    <AuthProvider>
      <Layout>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route
            path="/citizen"
            element={<ProtectedRoute requiredRole="CITIZEN"><CitizenDashboardPage /></ProtectedRoute>}
          />
          <Route
            path="/operator"
            element={<ProtectedRoute requiredRole="OPERATOR"><OperatorDashboardPage /></ProtectedRoute>}
          />
          <Route path="/manager" element={<ProtectedRoute requiredRole="OPERATIONS_MANAGER"><ManagerDashboardPage /></ProtectedRoute>} />
          <Route path="/engineer" element={<ProtectedRoute requiredRole="FIELD_ENGINEER"><FieldEngineerDashboardPage /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute requiredRole="ADMIN"><AdminDashboardPage /></ProtectedRoute>} />
        </Routes>
      </Layout>
    </AuthProvider>
  );
}
