// AuthContext.jsx
// Central authentication state for the entire app
// Any component can read login status, roles, and username from here

import { createContext, useState, useContext } from 'react';
import { login, logout } from '../api/authApi';
import { tokenManager } from '../auth/tokenManager';

// The context object — think of it as a shared store
export const AuthContext = createContext();

// AuthProvider wraps your entire app
// Every page inside it can access auth state
export const AuthProvider = ({ children }) => {

  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // Called when user submits the login form
  const handleLogin = async (username, password) => {
    setIsLoading(true);
    setError(null);
    try {
      await login(username, password);
      setIsAuthenticated(true);
    } catch {
      // Generic message — never expose raw server error to UI
      setError('Invalid username or password');
    } finally {
      setIsLoading(false);
    }
  };

  // Called when user clicks logout
  const handleLogout = () => {
    logout();
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      isLoading,
      error,
      handleLogin,
      handleLogout,
      tokenManager
    }}>
      {children}
    </AuthContext.Provider>
  );
};

// useAuth — shortcut hook so any component can do:
// const { isAuthenticated, handleLogin } = useAuth();
export const useAuth = () => useContext(AuthContext);