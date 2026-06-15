// AISummaryPage.jsx
// ADMIN only — AI generated fraud investigation summary
// Accessible at /risk/ai-summary

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAISummary } from '../../api/riskApi';

const getStatusStyle = (status) => {
  switch (status) {
    case 'APPROVED':  return { backgroundColor: '#064e3b', color: '#10b981' };
    case 'DECLINED':
    case 'REJECTED':  return { backgroundColor: '#7f1d1d', color: '#ef4444' };
    case 'REVIEW':    return { backgroundColor: '#78350f', color: '#f59e0b' };
    default:          return { backgroundColor: '#1f2937', color: '#9ca3af' };
  }
};

// Converts \n separated text into clean paragraphs and sections
// Handles markdown-style double \n as paragraph breaks
function formatSummary(text) {
  if (!text) return [];
  return text
    .split(/\n+/)
    .map(line => line.trim())
    .filter(line => line.length > 0);
}

export default function AISummaryPage() {
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
      const response = await getAISummary(transactionId.trim());
      setData(response);
    } catch (err) {
      console.error("UI Request Failed: ", err.response?.data || err.message);
      setError(err.response?.data?.message || 'Something went wrong while generating the AI Summary.');
    } finally {
      setIsLoading(false);
    }
  };

  const paragraphs = data ? formatSummary(data.summary) : [];

  return (
    <div style={{
      backgroundColor: '#0a0f1e',
      minHeight: '100vh',
      fontFamily: "'Segoe UI', sans-serif",
    }}>
      <div style={{ maxWidth: '720px', margin: '0 auto', padding: '32px' }}>

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
          marginBottom: '4px',
          marginTop: 0,
        }}>
          🤖 AI Fraud Investigation
        </h1>
        <p style={{
          color: '#6b7280',
          fontSize: '13px',
          marginBottom: '24px',
          marginTop: 0,
        }}>
          Powered by Generative AI — summaries may take a few seconds to load
        </p>

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
            {isLoading ? 'Analysing...' : 'Analyse'}
          </button>
        </form>

        {/* Error */}
        {error && (
          <p style={{
            color: '#ef4444',
            textAlign: 'center',
            marginBottom: '24px',
            marginTop: 0,
            fontSize: '14px',
          }}>
            {error}
          </p>
        )}

        {/* Loading state — AI takes time */}
        {isLoading && (
          <div style={{
            backgroundColor: '#111827',
            border: '1px solid #1f2937',
            borderRadius: '16px',
            padding: '40px',
            textAlign: 'center',
          }}>
            <div style={{ fontSize: '32px', marginBottom: '16px' }}>🤖</div>
            <p style={{ color: '#f59e0b', fontWeight: 600, fontSize: '15px', margin: '0 0 8px 0' }}>
              Generating Investigation Summary...
            </p>
            <p style={{ color: '#6b7280', fontSize: '13px', margin: 0 }}>
              AI is analysing transaction patterns, risk signals, and fraud indicators.
              This may take a few seconds.
            </p>
          </div>
        )}

        {/* Result card */}
        {!isLoading && data && (
          <div style={{
            backgroundColor: '#111827',
            border: '1px solid #1f2937',
            borderRadius: '16px',
            padding: '32px',
          }}>

            {/* Header — transaction ID + status badge */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'flex-start',
              marginBottom: '16px',
              gap: '16px',
            }}>
              <div>
                <p style={{
                  color: '#6b7280',
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  margin: '0 0 6px 0',
                }}>
                  TRANSACTION ID
                </p>
                <p style={{
                  color: '#9ca3af',
                  fontSize: '13px',
                  fontFamily: 'monospace',
                  margin: 0,
                }}>
                  {data.transactionId}
                </p>
              </div>
              <span style={{
                ...getStatusStyle(data.status),
                padding: '4px 14px',
                borderRadius: '999px',
                fontSize: '12px',
                fontWeight: 700,
                whiteSpace: 'nowrap',
                flexShrink: 0,
              }}>
                {data.status}
              </span>
            </div>

            {/* Divider */}
            <div style={{ borderTop: '1px solid #1f2937', marginBottom: '24px' }} />

            {/* AI Summary label */}
            <p style={{
              color: '#6b7280',
              fontSize: '11px',
              fontWeight: 700,
              letterSpacing: '0.08em',
              margin: '0 0 16px 0',
            }}>
              🤖 AI INVESTIGATION SUMMARY
            </p>

            {/* Rendered summary paragraphs — clean, no \n visible */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {paragraphs.map((para, index) => {
                // Detect section headers ending with : like "Key Risk Indicators:"
                const isHeader = para.endsWith(':');
                return (
                  <p
                    key={index}
                    style={{
                      margin: 0,
                      color: isHeader ? '#f59e0b' : '#d1d5db',
                      fontSize: isHeader ? '12px' : '14px',
                      fontWeight: isHeader ? 700 : 400,
                      letterSpacing: isHeader ? '0.05em' : 'normal',
                      lineHeight: '1.7',
                      paddingLeft: isHeader ? 0 : '12px',
                      borderLeft: isHeader ? 'none' : '2px solid #1f2937',
                    }}
                  >
                    {para}
                  </p>
                );
              })}
            </div>

            {/* Footer */}
            <div style={{
              marginTop: '28px',
              paddingTop: '16px',
              borderTop: '1px solid #1f2937',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}>
              <p style={{
                color: '#374151',
                fontSize: '11px',
                margin: 0,
              }}>
                Generated by Gringotts AI • For investigative use only
              </p>
              <p style={{
                color: '#374151',
                fontSize: '11px',
                margin: 0,
              }}>
                ADMIN access required
              </p>
            </div>

          </div>
        )}
      </div>
    </div>
  );
}