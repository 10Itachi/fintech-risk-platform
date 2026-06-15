// LoginPage.jsx
// Public page — no auth required
// Collects credentials, calls Keycloak, redirects on success

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import MagicBackground from '../../components/common/MagicBackground';

const inputStyle = {
  backgroundColor: 'rgba(17, 24, 39, 0.6)',
  border: '1px solid rgba(55, 65, 81, 0.8)',
  color: 'white',
  padding: '12px 16px',
  borderRadius: '8px',
  marginBottom: '16px',
  outline: 'none',
  fontSize: '14px',
  width: '100%',
  boxSizing: 'border-box',
};

export default function LoginPage() {
  const { isAuthenticated, handleLogin, isLoading, error } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  useEffect(() => {
    if (isAuthenticated) navigate('/');
  }, [isAuthenticated, navigate]);

  const handleSubmit = (e) => {
    e.preventDefault();
    handleLogin(username, password);
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-start',  // card on left
      height: '100vh',
      fontFamily: "'Segoe UI', sans-serif",
      position: 'relative',
    }}>
      <MagicBackground />

      {/* Left side — login card */}
      <div style={{
        position: 'relative',
        zIndex: 5,
        padding: '48px',
        height: '100vh',
        display: 'flex',
        alignItems: 'center',
        background: 'linear-gradient(90deg, rgba(10, 15, 30, 0.95) 60%, transparent 100%)',
        minWidth: '480px',
      }}>
        <form onSubmit={handleSubmit} style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: 'rgba(17, 24, 39, 0.5)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(245, 158, 11, 0.2)',
          borderRadius: '16px',
          padding: '48px',
          width: '380px',
          boxSizing: 'border-box',
        }}>

          {/* Branding */}
          <p style={{
            fontFamily: 'Cinzel, serif',
            color: '#6b7280',
            fontSize: '10px',
            letterSpacing: '0.15em',
            margin: '0 0 8px 0',
            textAlign: 'center',
          }}>
            FRAUD INTELLIGENCE SERVICE
          </p>
          <h1 style={{
            fontFamily: 'Cinzel, serif',
            fontSize: '26px',
            fontWeight: 700,
            color: '#f59e0b',
            textAlign: 'center',
            margin: '0 0 4px 0',
          }}>
            Gringotts 
          </h1>
          <p style={{
            fontSize: '13px',
            color: '#6b7280',
            textAlign: 'center',
            marginBottom: '32px',
            marginTop: 0,
          }}>
            Secure access portal
          </p>

          {/* Error message */}
          {error && (
            <p style={{
              fontSize: '14px',
              color: '#ef4444',
              textAlign: 'center',
              marginBottom: '16px',
              marginTop: 0,
            }}>
              {error}
            </p>
          )}

          {/* Username input */}
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = 'rgba(55, 65, 81, 0.8)'}
            style={inputStyle}
            required
            autoComplete="username"
          />

          {/* Password input */}
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = 'rgba(55, 65, 81, 0.8)'}
            style={inputStyle}
            required
            autoComplete="current-password"
          />

          {/* Submit button */}
          <button
            type="submit"
            disabled={isLoading}
            style={{
              backgroundColor: isLoading ? '#92400e' : '#f59e0b',
              color: '#0a0f1e',
              fontWeight: 700,
              padding: '14px',
              borderRadius: '8px',
              border: 'none',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              fontSize: '15px',
              marginTop: '8px',
              transition: 'background-color 0.2s',
              fontFamily: 'Cinzel, serif',
              letterSpacing: '0.05em',
            }}
          >
            {isLoading ? 'Authenticating...' : 'Access Vault'}
          </button>

          {/* Security footer */}
          <p style={{
            fontSize: '11px',
            color: '#374151',
            textAlign: 'center',
            marginTop: '24px',
            marginBottom: 0,
            letterSpacing: '0.05em',
          }}>
            Secured by Keycloak • JWT Authentication
          </p>

        </form>
      </div>

      {/* Right side — tagline over the castle image */}
      <div style={{
        position: 'relative',
        zIndex: 5,
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'flex-end',
        paddingBottom: '64px',
        pointerEvents: 'none',
      }}>
        <p style={{
          fontFamily: 'Cinzel, serif',
          color: 'rgba(245, 158, 11, 0.4)',
          fontSize: '13px',
          letterSpacing: '0.2em',
          margin: 0,
        }}>
          WHERE YOUR MONEY IS NEVER SAFE
        </p>
      </div>

    </div>
  );
}