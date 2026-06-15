// CreateUserPage.jsx
// ADMIN only — creates a new user via User Service
// Accessible at /admin/create-user

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createUser } from '../../api/userApi';

const inputStyle = {
  backgroundColor: '#1f2937',
  border: '1px solid #374151',
  color: 'white',
  padding: '12px 16px',
  borderRadius: '8px',
  marginBottom: '16px',
  outline: 'none',
  fontSize: '14px',
  width: '100%',
  boxSizing: 'border-box',
};

export default function CreateUserPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    userName: '',
    password: '',
    email: '',
    phoneNumber: '',
    role: 'USER',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null); // holds response object, not a string

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const result = await createUser(form);
      setSuccess(result); // store full response object
      setForm({ userName: '', password: '', email: '', phoneNumber: '', role: 'USER' });
    } catch {
      setError('Failed to create user. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{
      backgroundColor: '#0a0f1e',
      minHeight: '100vh',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      fontFamily: "'Segoe UI', sans-serif",
    }}>
      <div style={{
        backgroundColor: '#111827',
        border: '1px solid #1f2937',
        borderRadius: '16px',
        padding: '48px',
        width: '480px',
        boxSizing: 'border-box',
      }}>

        {/* Back button */}
        <button
          onClick={() => navigate('/')}
          style={{
            backgroundColor: 'transparent',
            border: 'none',
            color: '#6b7280',
            cursor: 'pointer',
            fontSize: '14px',
            padding: 0,
            marginBottom: '24px',
            display: 'block',
          }}
        >
          ← Dashboard
        </button>

        <h2 style={{
          color: '#f59e0b',
          fontSize: '22px',
          fontWeight: 700,
          marginBottom: '24px',
          marginTop: 0,
        }}>
          👤 Create User
        </h2>

        {/* Error message */}
        {error && (
          <p style={{
            color: '#ef4444',
            fontSize: '14px',
            textAlign: 'center',
            marginBottom: '16px',
            marginTop: 0,
          }}>
            {error}
          </p>
        )}

        {/* Success card — shows created user details */}
        {success && (
          <div style={{
            backgroundColor: '#064e3b',
            border: '1px solid #10b981',
            borderRadius: '12px',
            padding: '16px 20px',
            marginBottom: '20px',
          }}>
            <p style={{
              color: '#10b981',
              fontSize: '12px',
              fontWeight: 700,
              margin: '0 0 12px 0',
              letterSpacing: '0.08em',
            }}>
              ✅ USER CREATED SUCCESSFULLY
            </p>
            <DetailRow label="USER ID" value={success.userId} />
            <DetailRow label="USERNAME" value={success.userName} />
            <DetailRow label="STATUS" value={success.isActive} highlight />
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column' }}>
          <input
            type="text"
            name="userName"
            value={form.userName}
            onChange={handleChange}
            placeholder="Username"
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Email"
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <input
            type="tel"
            name="phoneNumber"
            value={form.phoneNumber}
            onChange={handleChange}
            placeholder="Phone Number"
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Password"
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />

          {/* Role dropdown */}
          <select
            name="role"
            value={form.role}
            onChange={handleChange}
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          >
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>

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
              marginTop: '4px',
            }}
          >
            {isLoading ? 'Creating...' : 'Create User'}
          </button>
        </form>

        <p style={{
          color: '#374151',
          fontSize: '12px',
          textAlign: 'center',
          marginTop: '24px',
          marginBottom: 0,
        }}>
          ADMIN access required
        </p>

      </div>
    </div>
  );
}

// Reusable row for displaying user detail
function DetailRow({ label, value, highlight }) {
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      borderBottom: '1px solid #065f46',
      paddingBottom: '8px',
      marginBottom: '8px',
    }}>
      <span style={{
        color: '#6b7280',
        fontSize: '11px',
        fontWeight: 700,
        letterSpacing: '0.08em',
      }}>
        {label}
      </span>
      <span style={{
        color: highlight ? '#10b981' : 'white',
        fontSize: '13px',
        fontWeight: highlight ? 700 : 400,
        backgroundColor: highlight ? '#065f46' : 'transparent',
        padding: highlight ? '2px 10px' : '0',
        borderRadius: highlight ? '999px' : '0',
        fontFamily: label === 'USER ID' ? 'monospace' : 'inherit',
      }}>
        {value}
      </span>
    </div>
  );
}