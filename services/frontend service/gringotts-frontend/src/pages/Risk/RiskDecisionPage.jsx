// RiskDecisionPage.jsx
// ADMIN only — lookup fraud risk decision by transaction ID
// Accessible at /risk/decision

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRiskDecision } from '../../api/riskApi';

const getStatusStyle = (status) => {
  switch (status) {
    case 'APPROVED': return { backgroundColor: '#064e3b', color: '#10b981' };
    case 'REVIEW':   return { backgroundColor: '#78350f', color: '#f59e0b' };
    case 'REJECTED': return { backgroundColor: '#7f1d1d', color: '#ef4444' };
    default:         return { backgroundColor: '#1f2937', color: '#9ca3af' };
  }
};

const getMlColor = (probability) => {
  const pct = probability * 100;
  if (pct < 40) return '#10b981';
  if (pct < 70) return '#f59e0b';
  return '#ef4444';
};

const labelStyle = {
  color: '#6b7280',
  fontSize: '11px',
  fontWeight: 700,
  letterSpacing: '0.08em',
  display: 'block',
  marginBottom: '4px',
};

const valueStyle = {
  color: '#9ca3af',
  fontSize: '14px',
};

export default function RiskDecisionPage() {
  const navigate = useNavigate();
  const [transactionId, setTransactionId] = useState('');
  const [data, setData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!transactionId.trim()) {
      setError('Please enter a transaction ID');
      return;
    }
    setIsLoading(true);
    setError(null);
    setData(null);
    try {
      const response = await getRiskDecision(transactionId.trim());
      setData(response);
    } catch {
      setError('No risk decision found for this transaction ID.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{
      backgroundColor: '#0a0f1e',
      minHeight: '100vh',
      fontFamily: "'Segoe UI', sans-serif",
    }}>
      <div style={{ maxWidth: '680px', margin: '0 auto', padding: '32px' }}>

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

        {/* Title */}
        <h1 style={{
          color: '#f59e0b',
          fontSize: '22px',
          fontWeight: 700,
          marginBottom: '24px',
          marginTop: 0,
        }}>
          🔍 Risk Decision Lookup
        </h1>

        {/* Search bar */}
        <form
          onSubmit={handleSearch}
          style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}
        >
          <input
            type="text"
            value={transactionId}
            onChange={(e) => setTransactionId(e.target.value)}
            placeholder="Enter Transaction ID"
            style={{
              flex: 1,
              backgroundColor: '#1f2937',
              border: '1px solid #374151',
              color: 'white',
              padding: '12px 16px',
              borderRadius: '8px',
              outline: 'none',
              fontSize: '14px',
            }}
            onFocus={(e) => e.target.style.borderColor = '#f59e0b'}
            onBlur={(e) => e.target.style.borderColor = '#374151'}
          />
          <button
            type="submit"
            disabled={isLoading}
            style={{
              backgroundColor: isLoading ? '#92400e' : '#f59e0b',
              color: '#0a0f1e',
              fontWeight: 700,
              padding: '12px 24px',
              borderRadius: '8px',
              border: 'none',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              fontSize: '14px',
              whiteSpace: 'nowrap',
            }}
          >
            {isLoading ? 'Searching...' : 'Search'}
          </button>
        </form>

        {/* Error */}
        {error && (
          <p style={{ color: '#ef4444', textAlign: 'center', marginBottom: '24px', marginTop: 0 }}>
            {error}
          </p>
        )}

        {/* Result card */}
        {data && (
          <div style={{
            backgroundColor: '#111827',
            border: '1px solid #1f2937',
            borderRadius: '16px',
            padding: '32px',
          }}>

            {/* Header row — label + status badge */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '16px',
            }}>
              <span style={{ color: '#6b7280', fontSize: '11px', letterSpacing: '0.08em', fontWeight: 700 }}>
                🔍 RISK DECISION
              </span>
              <span style={{
                ...getStatusStyle(data.finalStatus),
                padding: '4px 14px',
                borderRadius: '999px',
                fontSize: '12px',
                fontWeight: 700,
              }}>
                {data.finalStatus}
              </span>
            </div>

            {/* Divider */}
            <div style={{ borderTop: '1px solid #1f2937', marginBottom: '24px' }} />

            {/* Data grid */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>

              {/* Transaction ID — full width */}
              <div style={{ gridColumn: 'span 2' }}>
                <span style={labelStyle}>TRANSACTION ID</span>
                <span style={{ ...valueStyle, fontFamily: 'monospace', fontSize: '13px' }}>
                  {data.transactionId}
                </span>
              </div>

              {/* ML Probability — color coded by risk level */}
              <div>
                <span style={labelStyle}>ML FRAUD PROBABILITY</span>
                <span style={{
                  ...valueStyle,
                  color: data.mlProbability !== null ? getMlColor(data.mlProbability) : '#9ca3af',
                  fontWeight: 700,
                  fontSize: '16px',
                }}>
                  {data.mlProbability !== null
                    ? (data.mlProbability * 100).toFixed(2) + '%'
                    : 'N/A'}
                </span>
              </div>

              {/* Soft Risk Score */}
              <div>
                <span style={labelStyle}>SOFT RISK SCORE</span>
                <span style={valueStyle}>
                  {data.softRiskScore !== null ? data.softRiskScore : 'N/A'}
                </span>
              </div>

              {/* Model Name */}
              <div>
                <span style={labelStyle}>MODEL NAME</span>
                <span style={{ ...valueStyle, fontFamily: 'monospace', fontSize: '12px' }}>
                  {data.modelName}
                </span>
              </div>

              {/* Model Version */}
              <div>
                <span style={labelStyle}>MODEL VERSION</span>
                <span style={{ ...valueStyle, fontFamily: 'monospace', fontSize: '12px' }}>
                  {data.modelVersion}
                </span>
              </div>

              {/* Evaluated At */}
              <div>
                <span style={labelStyle}>EVALUATED AT</span>
                <span style={valueStyle}>{data.evaluatedAt.split('T')[0]}</span>
              </div>

            </div>

            {/* Reason Codes */}
            <div style={{ marginTop: '24px', paddingTop: '20px', borderTop: '1px solid #1f2937' }}>
              <span style={labelStyle}>REASON CODES</span>
              {data.reasonCodes.length === 0 ? (
                <p style={{ color: '#6b7280', fontSize: '13px', marginTop: '8px', marginBottom: 0 }}>
                  No reason codes flagged.
                </p>
              ) : (
                <div style={{ marginTop: '8px', display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {data.reasonCodes.map((code, index) => (
                    <span key={index} style={{
                      backgroundColor: '#1f2937',
                      color: '#f59e0b',
                      padding: '4px 12px',
                      borderRadius: '999px',
                      fontSize: '12px',
                      fontWeight: 600,
                    }}>
                      {code}
                    </span>
                  ))}
                </div>
              )}
            </div>

          </div>
        )}
      </div>
    </div>
  );
}