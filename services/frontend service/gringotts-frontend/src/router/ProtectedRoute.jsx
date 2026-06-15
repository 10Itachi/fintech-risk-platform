import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, tokenManager } = useAuth();

  // If the user is not authenticated, redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // If a required role is specified and the user does not have it, redirect to login
  if (requiredRole && !tokenManager.hasRole(requiredRole)) {
    return <Navigate to="/login" replace />;
  }

  // Otherwise, render the protected children components
  return children;
}