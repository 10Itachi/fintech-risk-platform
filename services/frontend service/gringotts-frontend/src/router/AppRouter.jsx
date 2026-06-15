import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/Login/LoginPage';
import DashboardPage from '../pages/Dashboard/DashboardPage';
import CreateUserPage from '../pages/Admin/CreateUserPage';
import CreateTransactionPage from '../pages/Transactions/CreateTransactionPage';
import AllTransactionsPage from '../pages/Transactions/AllTransactionsPage';
import RiskDecisionPage from '../pages/Risk/RiskDecisionPage';
import AISummaryPage from '../pages/Risk/AISummaryPage';
import { AuthProvider } from '../context/AuthContext';
import AppLayout from '../components/common/AppLayout';

export default function AppRouter() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>

          {/* Public — no layout, no auth */}
          <Route path="/login" element={<LoginPage />} />

          {/* Dashboard — authenticated, any role */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <DashboardPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          {/* ADMIN only */}
          <Route
            path="/admin/create-user"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AppLayout>
                  <CreateUserPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/transactions/all"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AppLayout>
                  <AllTransactionsPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/risk/decision"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AppLayout>
                  <RiskDecisionPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/risk/ai-summary"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AppLayout>
                  <AISummaryPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          {/* USER only */}
          <Route
            path="/transactions/create"
            element={
              <ProtectedRoute requiredRole="USER">
                <AppLayout>
                  <CreateTransactionPage />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          {/* Catch all */}
          <Route path="*" element={<Navigate to="/login" replace />} />

        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}