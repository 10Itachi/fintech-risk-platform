// DashboardPage.jsx
// Welcome screen — navigation is now in sidebar

import { useAuth } from '../../context/AuthContext';
import { Landmark, ShieldCheck, Zap, Globe } from 'lucide-react';

export default function DashboardPage() {
  const { tokenManager } = useAuth();
  const isAdmin = tokenManager.isAdmin();

  return (
    <div style={{
      padding: '48px',
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
    }}>

      {/* Welcome header */}
      <div style={{ marginBottom: '48px' }}>
        <p style={{
          color: '#6b7280',
          fontSize: '13px',
          letterSpacing: '0.1em',
          marginBottom: '8px',
          fontFamily: 'Cinzel, serif',
        }}>
          GRINGOTTS WIZARDING BANK
        </p>
        <h1 style={{
          fontFamily: 'Cinzel, serif',
          color: '#f59e0b',
          fontSize: '36px',
          fontWeight: 700,
          marginBottom: '12px',
          lineHeight: 1.2,
        }}>
          Welcome back,<br />{tokenManager.getUserName()}
        </h1>
        <p style={{ color: '#6b7280', fontSize: '15px' }}>
          {isAdmin
            ? 'You have full administrative access to the fraud intelligence platform.'
            : 'You have access to transaction submission and tracking.'}
        </p>
      </div>

      {/* Stats cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
        gap: '16px',
        maxWidth: '800px',
      }}>
        <StatCard
          icon={ShieldCheck}
          label="Platform Status"
          value="Operational"
          color="#10b981"
        />
        <StatCard
          icon={Zap}
          label="Fraud Detection"
          value="Active"
          color="#f59e0b"
        />
        <StatCard
          icon={Globe}
          label="Coverage"
          value="IN · US · SG · AE"
          color="#60a5fa"
        />
        <StatCard
          icon={Landmark}
          label="Architecture"
          value="Microservices"
          color="#a78bfa"
        />
      </div>

      {/* Footer */}
      <p style={{
        color: '#1f2937',
        fontSize: '12px',
        marginTop: '64px',
        fontFamily: 'Cinzel, serif',
        letterSpacing: '0.05em',
      }}>
        Gringotts Fraud Intelligence Platform • Secured by Keycloak • JWT Authentication
      </p>

    </div>
  );
}

function StatCard({ icon: Icon, label, value, color }) {
  return (
    <div style={{
      backgroundColor: 'rgba(17, 24, 39, 0.6)',
      backdropFilter: 'blur(12px)',
      border: '1px solid rgba(255,255,255,0.05)',
      borderRadius: '12px',
      padding: '20px',
      transition: 'border-color 0.2s',
    }}
      onMouseOver={(e) => e.currentTarget.style.borderColor = `${color}40`}
      onMouseOut={(e) => e.currentTarget.style.borderColor = 'rgba(255,255,255,0.05)'}
    >
      <Icon size={20} color={color} strokeWidth={1.5} style={{ marginBottom: '12px' }} />
      <p style={{ color: '#6b7280', fontSize: '11px', letterSpacing: '0.08em', margin: '0 0 4px 0' }}>
        {label.toUpperCase()}
      </p>
      <p style={{ color: 'white', fontSize: '15px', fontWeight: 600, margin: 0 }}>
        {value}
      </p>
    </div>
  );
}