// CreateTransactionPage.jsx
// USER role — submit a new transaction
// Accessible at /transactions/create

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTransaction } from '../../api/transactionApi';

const inputStyle = {
  backgroundColor: '#1f2937',
  border: '1px solid #374151',
  color: 'white',
  padding: '12px 16px',
  borderRadius: '8px',
  width: '100%',
  outline: 'none',
  fontSize: '14px',
  boxSizing: 'border-box',
  marginBottom: '16px',
};

const getStatusStyle = (status) => {
  switch (status) {
    case 'APPROVED': return { backgroundColor: '#064e3b', color: '#10b981' };
    case 'REVIEW':   return { backgroundColor: '#78350f', color: '#f59e0b' };
    case 'REJECTED': return { backgroundColor: '#7f1d1d', color: '#ef4444' };
    default:         return { backgroundColor: '#1f2937', color: '#9ca3af' };
  }
};

const EMPTY_FORM = {
  amount: '',
  transactionType: 'TRANSFER',
  sourceAccount: '',
  targetAccount: '',
  channel: 'UPI',
  country: 'IN',
};

const ACCOUNT_REGEX = /^ACC-[A-Z]{2,3}-[0-9]{6,12}$/;

export default function CreateTransactionPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY_FORM);
  const [accountError, setAccountError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setAccountError(null);
    setError(null);
    setSuccess(null);

    // Validate account format before hitting API
    if (!ACCOUNT_REGEX.test(form.sourceAccount) || !ACCOUNT_REGEX.test(form.targetAccount)) {
      setAccountError('Invalid account format. Example: ACC-EW-123456');
      return;
    }

    setIsLoading(true);

    // Auto-generate device ID and transaction time — never shown to user
   
    // Map channel to deviceId — matches backend's getDeviceId() logic exactly
      const channelDeviceMap = {
      UPI: 'DEV-ANDROID-MOBILE',
      CARD: 'DEV-WEB-ATM001',
      NET_BANKING: 'DEV-WEB-LAPTOP',
        };
      const deviceId = channelDeviceMap[form.channel] || 'DEV-WEB-SYSTEM';  
      const transactionTime = new Date().toISOString();
      const payload = { ...form, deviceId, transactionTime };

    try {
      const response = await createTransaction(payload);
      setSuccess(response);
      setForm(EMPTY_FORM);
    } catch {
      setError('Transaction failed. Please try again.');
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
      padding: '32px',
    }}>
      <div style={{
        backgroundColor: '#111827',
        border: '1px solid #1f2937',
        borderRadius: '16px',
        padding: '48px',
        width: '580px',
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
          💸 Create Transaction
        </h2>

        {/* Account validation error */}
        {accountError && (
          <p style={{
            color: '#ef4444',
            fontSize: '13px',
            marginBottom: '16px',
            marginTop: 0,
          }}>
            ⚠️ {accountError}
          </p>
        )}

        {/* API error */}
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

        {/* Success card */}
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
              ✅ TRANSACTION SUBMITTED
            </p>
            <DetailRow label="TRANSACTION ID" value={success.transactionId} mono />
            <DetailRow label="USER" value={success.userName} />
            <DetailRow label="AMOUNT" value={`₹${success.amount}`} />
            <DetailRow label="DATE" value={success.createdAt.split('T')[0]} />
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              paddingBottom: '4px',
            }}>
              <span style={{
                color: '#6b7280',
                fontSize: '11px',
                fontWeight: 700,
                letterSpacing: '0.08em',
              }}>
                STATUS
              </span>
              <span style={{
                ...getStatusStyle(success.transactionStatus),
                padding: '2px 10px',
                borderRadius: '999px',
                fontSize: '11px',
                fontWeight: 700,
              }}>
                {success.transactionStatus}
              </span>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column' }}>
          <input
            type="number"
            name="amount"
            min="1"
            placeholder="Amount (₹)"
            value={form.amount}
            onChange={handleChange}
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <input
            type="text"
            name="sourceAccount"
            placeholder="Source Account (ACC-EW-123456)"
            value={form.sourceAccount}
            onChange={handleChange}
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <input
            type="text"
            name="targetAccount"
            placeholder="Target Account (ACC-IN-000123)"
            value={form.targetAccount}
            onChange={handleChange}
            required
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <select
            name="transactionType"
            value={form.transactionType}
            onChange={handleChange}
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          >
            <option value="TRANSFER">TRANSFER</option>
            <option value="WITHDRAWAL">WITHDRAWAL</option>
            <option value="DEPOSIT">DEPOSIT</option>
          </select>
          <select
            name="channel"
            value={form.channel}
            onChange={handleChange}
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          >
            <option value="UPI">UPI</option>
            <option value="CARD">CARD</option>
            <option value="NET_BANKING">NET BANKING</option>
          </select>
          <select
            name="country"
            value={form.country}
            onChange={handleChange}
            style={inputStyle}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          >
            <option value="IN">IN — India</option>
            <option value="US">US — United States</option>
            <option value="SG">SG — Singapore</option>
            <option value="AE">AE — UAE</option>
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
              width: '100%',
              marginTop: '4px',
            }}
          >
            {isLoading ? 'Processing...' : 'Submit Transaction'}
          </button>
        </form>

        <p style={{
          color: '#374151',
          fontSize: '12px',
          textAlign: 'center',
          marginTop: '24px',
          marginBottom: 0,
        }}>
          USER access required
        </p>

      </div>
    </div>
  );
}

function DetailRow({ label, value, mono }) {
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
        color: 'white',
        fontSize: '13px',
        fontFamily: mono ? 'monospace' : 'inherit',
        //wordBreak: 'break-all',
        textAlign: 'nowrap',
        maxWidth: '260px',
      }}>
        {value}
      </span>
    </div>
  );
}