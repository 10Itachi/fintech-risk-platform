// AllTransactionsPage.jsx
// ADMIN only — paginated + sortable view of all transactions

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllTransactions } from '../../api/transactionApi';

const getStatusStyle = (status) => {
  switch (status) {
    case 'APPROVED': return { backgroundColor: '#064e3b', color: '#10b981' };
    case 'REVIEW':   return { backgroundColor: '#78350f', color: '#f59e0b' };
    case 'REJECTED': return { backgroundColor: '#7f1d1d', color: '#ef4444' };
    default:         return { backgroundColor: '#1f2937', color: '#9ca3af' };
  }
};

// Column definitions — minWidth controls each column's minimum size
const COLUMNS = [
  { label: 'TRANSACTION ID', field: 'transactionId', sortable: false, minWidth: '320px' },
  { label: 'USER',           field: 'userName',       sortable: false, minWidth: '100px' },
  { label: 'AMOUNT',         field: 'amount',         sortable: true,  minWidth: '110px' },
  { label: 'TYPE',           field: 'transactionType',sortable: false, minWidth: '110px' },
  { label: 'CHANNEL',        field: 'channel',        sortable: false, minWidth: '100px' },
  { label: 'STATUS',         field: 'transactionStatus', sortable: true, minWidth: '110px' },
  { label: 'DATE',           field: 'createdAt',      sortable: true,  minWidth: '120px' },
];

export default function AllTransactionsPage() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [sortField, setSortField] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [copiedId, setCopiedId] = useState(null);

  const fetchTransactions = async (page, field, dir) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await getAllTransactions(page, 10, `${field},${dir}`);
      setData(response);
    } catch {
      setError('Failed to load transactions.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions(currentPage, sortField, sortDir);
  }, [currentPage, sortField, sortDir]);

  const handleSort = (field) => {
    if (sortField === field) {
      // Toggle direction if same field clicked
      setSortDir(sortDir === 'desc' ? 'asc' : 'desc');
    } else {
      setSortField(field);
      setSortDir('desc');
    }
    setCurrentPage(0); // reset to first page on sort change
  };

  const handleCopyId = (id) => {
    navigator.clipboard.writeText(id);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 1500);
  };

  const thStyle = (field, sortable) => ({
    padding: '12px 16px',
    color: sortable ? '#9ca3af' : '#6b7280',
    fontSize: '11px',
    letterSpacing: '0.08em',
    textAlign: 'left',
    fontWeight: 700,
    borderBottom: '1px solid #1f2937',
    cursor: sortable ? 'pointer' : 'default',
    userSelect: 'none',
    whiteSpace: 'nowrap',
    backgroundColor: sortField === field ? '#1a2332' : 'transparent',
  });

  const tdStyle = {
    padding: '12px 16px',
    borderBottom: '1px solid #1f2937',
    color: 'white',
    fontSize: '13px',
    whiteSpace: 'nowrap',
  };

  return (
    <div style={{
      backgroundColor: '#0a0f1e',
      minHeight: '100vh',
      fontFamily: "'Segoe UI', sans-serif",
      padding: '32px',
    }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>

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
          marginBottom: '8px',
          marginTop: 0,
        }}>
          📋 All Transactions
        </h1>

        {/* Summary line */}
        {data && (
          <p style={{ color: '#6b7280', fontSize: '13px', marginBottom: '24px', marginTop: 0 }}>
            Total: {data.totalElements} transactions | Page {data.number + 1} of {data.totalPages} | Sorted by {sortField} ({sortDir})
          </p>
        )}

        {/* Loading */}
        {isLoading && (
          <div style={{ textAlign: 'center', color: '#6b7280', marginTop: '48px' }}>
            Loading transactions...
          </div>
        )}

        {/* Error */}
        {error && (
          <div style={{ textAlign: 'center', color: '#ef4444', marginTop: '48px' }}>
            {error}
          </div>
        )}

        {/* Empty */}
        {!isLoading && data && data.empty && (
          <div style={{ textAlign: 'center', color: '#6b7280', marginTop: '48px' }}>
            No transactions found.
          </div>
        )}

        {/* Table */}
        {!isLoading && data && !data.empty && (
          <>
            <div style={{ overflowX: 'auto', borderRadius: '12px', border: '1px solid #1f2937' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#111827' }}>
                <thead>
                  <tr>
                    {COLUMNS.map((col) => (
                      <th
                        key={col.field}
                        style={{ ...thStyle(col.field, col.sortable), minWidth: col.minWidth }}
                        onClick={() => col.sortable && handleSort(col.field)}
                      >
                        {col.label}
                        {col.sortable && (
                          <span style={{ marginLeft: '6px', color: sortField === col.field ? '#f59e0b' : '#374151' }}>
                            {sortField === col.field
                              ? sortDir === 'desc' ? '▼' : '▲'
                              : '⇅'}
                          </span>
                        )}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((txn) => (
                    <tr
                      key={txn.transactionId}
                      onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#1a2332'}
                      onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      {/* Full transaction ID with copy on click */}
                      <td style={{ ...tdStyle, fontFamily: 'monospace', color: '#9ca3af', minWidth: '320px' }}>
                        <span
                          onClick={() => handleCopyId(txn.transactionId)}
                          title="Click to copy"
                          style={{ cursor: 'pointer' }}
                        >
                          {txn.transactionId}
                        </span>
                        {copiedId === txn.transactionId && (
                          <span style={{
                            marginLeft: '8px',
                            color: '#10b981',
                            fontSize: '11px',
                            fontFamily: 'sans-serif',
                          }}>
                            ✓ Copied
                          </span>
                        )}
                      </td>
                      <td style={tdStyle}>{txn.userName}</td>
                      <td style={{ ...tdStyle, color: '#f59e0b', fontWeight: 600 }}>
                        ₹{txn.amount.toFixed(2)}
                      </td>
                      <td style={tdStyle}>{txn.transactionType}</td>
                      <td style={tdStyle}>{txn.channel}</td>
                      <td style={tdStyle}>
                        <span style={{
                          ...getStatusStyle(txn.transactionStatus),
                          padding: '2px 10px',
                          borderRadius: '999px',
                          fontSize: '11px',
                          fontWeight: 700,
                        }}>
                          {txn.transactionStatus}
                        </span>
                      </td>
                      <td style={{ ...tdStyle, color: '#9ca3af' }}>
                        {txn.createdAt.split('T')[0]}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginTop: '24px',
            }}>
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={data.first}
                style={{
                  backgroundColor: '#1f2937',
                  border: '1px solid #374151',
                  color: 'white',
                  padding: '8px 20px',
                  borderRadius: '8px',
                  cursor: data.first ? 'not-allowed' : 'pointer',
                  opacity: data.first ? 0.4 : 1,
                  fontSize: '13px',
                }}
              >
                ← Previous
              </button>

              <span style={{ color: '#9ca3af', fontSize: '13px' }}>
                Page {data.number + 1} of {data.totalPages}
              </span>

              <button
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={data.last}
                style={{
                  backgroundColor: '#1f2937',
                  border: '1px solid #374151',
                  color: 'white',
                  padding: '8px 20px',
                  borderRadius: '8px',
                  cursor: data.last ? 'not-allowed' : 'pointer',
                  opacity: data.last ? 0.4 : 1,
                  fontSize: '13px',
                }}
              >
                Next →
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}