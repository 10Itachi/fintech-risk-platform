// AppLayout.jsx
// Main layout shell — sidebar + content area
// Wraps all protected pages
// Role-aware navigation with Lucide icons

import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import MagicBackground from './MagicBackground';
import {
  LayoutDashboard,
  UserPlus,
  ArrowLeftRight,
  ListOrdered,
  ShieldAlert,
  BrainCircuit,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Landmark,
} from 'lucide-react';

const ADMIN_NAV = [
  { label: 'Dashboard',        path: '/',                    icon: LayoutDashboard },
  { label: 'Create User',      path: '/admin/create-user',   icon: UserPlus },
  { label: 'All Transactions', path: '/transactions/all',    icon: ListOrdered },
  { label: 'Risk Decision',    path: '/risk/decision',       icon: ShieldAlert },
  { label: 'AI Fraud Summary', path: '/risk/ai-summary',     icon: BrainCircuit },
];

const USER_NAV = [
  { label: 'Dashboard',          path: '/',                     icon: LayoutDashboard },
  { label: 'Create Transaction', path: '/transactions/create',  icon: ArrowLeftRight },
];

export default function AppLayout({ children }) {
  const { tokenManager, handleLogout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const isAdmin = tokenManager.isAdmin();
  const navItems = isAdmin ? ADMIN_NAV : USER_NAV;

  return (
    <div style={{ display: 'flex', minHeight: '100vh', position: 'relative' }}>

      {/* Hogwarts themed background with gold particles */}
      <MagicBackground />

      {/* Sidebar — sits above background layers (z-index 4) */}
      <aside style={{
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        width: collapsed ? '72px' : '240px',
        backgroundColor: 'rgba(10, 15, 30, 0.75)',
        backdropFilter: 'blur(16px)',
        borderRight: '1px solid rgba(245, 158, 11, 0.15)',
        display: 'flex',
        flexDirection: 'column',
        transition: 'width 0.3s ease',
        zIndex: 10,
        overflow: 'hidden',
      }}>

        {/* Logo area */}
        <div style={{
          padding: collapsed ? '24px 0' : '24px 20px',
          borderBottom: '1px solid rgba(245, 158, 11, 0.1)',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          justifyContent: collapsed ? 'center' : 'flex-start',
        }}>
          <Landmark size={28} color="#f59e0b" strokeWidth={1.5} />
          {!collapsed && (
            <div>
              <p style={{
                fontFamily: 'Cinzel, serif',
                color: '#f59e0b',
                fontSize: '16px',
                fontWeight: 700,
                margin: 0,
                letterSpacing: '0.05em',
                whiteSpace: 'nowrap',
              }}>
                Gringotts
              </p>
              <p style={{
                color: '#6b7280',
                fontSize: '10px',
                margin: 0,
                letterSpacing: '0.08em',
                whiteSpace: 'nowrap',
              }}>
                FRAUD INTELLIGENCE
              </p>
            </div>
          )}
        </div>

        {/* User info */}
        {!collapsed && (
          <div style={{
            padding: '16px 20px',
            borderBottom: '1px solid rgba(245, 158, 11, 0.1)',
          }}>
            <p style={{
              color: '#9ca3af',
              fontSize: '11px',
              margin: '0 0 4px 0',
              letterSpacing: '0.05em',
            }}>
              SIGNED IN AS
            </p>
            <p style={{
              color: 'white',
              fontSize: '13px',
              fontWeight: 600,
              margin: '0 0 6px 0',
            }}>
              {tokenManager.getUserName()}
            </p>
            <span style={{
              backgroundColor: isAdmin ? '#78350f' : '#1e3a5f',
              color: isAdmin ? '#f59e0b' : '#60a5fa',
              fontSize: '10px',
              fontWeight: 700,
              padding: '2px 8px',
              borderRadius: '999px',
              letterSpacing: '0.08em',
            }}>
              {isAdmin ? 'ADMINISTRATOR' : 'USER'}
            </span>
          </div>
        )}

        {/* Nav items */}
        <nav style={{ flex: 1, padding: '12px 0', overflowY: 'auto' }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = location.pathname === item.path;
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                title={collapsed ? item.label : ''}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  width: '100%',
                  padding: collapsed ? '12px 0' : '12px 20px',
                  justifyContent: collapsed ? 'center' : 'flex-start',
                  backgroundColor: active
                    ? 'rgba(245, 158, 11, 0.12)'
                    : 'transparent',
                  border: 'none',
                  borderLeft: active
                    ? '2px solid #f59e0b'
                    : '2px solid transparent',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  marginBottom: '2px',
                }}
                onMouseOver={(e) => {
                  if (!active) e.currentTarget.style.backgroundColor = 'rgba(245, 158, 11, 0.06)';
                }}
                onMouseOut={(e) => {
                  if (!active) e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <Icon
                  size={18}
                  color={active ? '#f59e0b' : '#6b7280'}
                  strokeWidth={1.5}
                />
                {!collapsed && (
                  <span style={{
                    color: active ? '#f59e0b' : '#9ca3af',
                    fontSize: '13px',
                    fontWeight: active ? 600 : 400,
                    whiteSpace: 'nowrap',
                  }}>
                    {item.label}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Logout + Collapse */}
        <div style={{
          borderTop: '1px solid rgba(245, 158, 11, 0.1)',
          padding: '12px 0',
        }}>

          <button
            onClick={handleLogout}
            title={collapsed ? 'Logout' : ''}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              width: '100%',
              padding: collapsed ? '12px 0' : '12px 20px',
              justifyContent: collapsed ? 'center' : 'flex-start',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              marginBottom: '4px',
              transition: 'background-color 0.2s',
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.08)'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            <LogOut size={18} color="#6b7280" strokeWidth={1.5} />
            {!collapsed && (
              <span style={{ color: '#6b7280', fontSize: '13px' }}>
                Logout
              </span>
            )}
          </button>

          <button
            onClick={() => setCollapsed(!collapsed)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              width: '100%',
              padding: collapsed ? '12px 0' : '12px 20px',
              justifyContent: collapsed ? 'center' : 'flex-start',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              transition: 'background-color 0.2s',
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(245, 158, 11, 0.05)'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            {collapsed
              ? <ChevronRight size={18} color="#374151" strokeWidth={1.5} />
              : <ChevronLeft size={18} color="#374151" strokeWidth={1.5} />
            }
            {!collapsed && (
              <span style={{ color: '#374151', fontSize: '12px' }}>
                Collapse
              </span>
            )}
          </button>

        </div>
      </aside>

      {/* Main content — above all background layers */}
      <main style={{
        marginLeft: collapsed ? '72px' : '240px',
        flex: 1,
        minHeight: '100vh',
        transition: 'margin-left 0.3s ease',
        position: 'relative',
        zIndex: 5,
      }}>
        {children}
      </main>

    </div>
  );
}