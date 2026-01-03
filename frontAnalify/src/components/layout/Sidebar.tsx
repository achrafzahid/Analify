import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { LogOut, Store } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthContext';
import { UserRole } from '@/types';
import { LucideIcon } from 'lucide-react';

interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
}

interface SidebarProps {
  navItems: NavItem[];
}

const roleLabels: Record<UserRole, string> = {
  CAISSIER: 'Cashier',
  ADMIN_STORE: 'Store Admin',
  INVESTOR: 'Investor',
  ADMIN_GENERAL: 'General Admin',
  ADMIN_G: 'General Admin',
};

export const Sidebar: React.FC<SidebarProps> = ({ navItems }) => {
  const { user, logout } = useAuth();
  const location = useLocation();

  if (!user) return null;

  return (
    <aside className="sidebar-nav">
      {/* Logo/Brand */}
      <div className="p-6 border-b border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-sidebar-primary rounded-lg">
            <Store className="h-6 w-6 text-sidebar-primary-foreground" />
          </div>
          <div>
            <h1 className="font-semibold text-sidebar-foreground">Analify</h1>
            <p className="text-xs text-sidebar-muted">Analytics Dashboard</p>
          </div>
        </div>
      </div>

      {/* User Info */}
      <div className="p-4 border-b border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-full bg-sidebar-accent flex items-center justify-center">
            <span className="text-sm font-medium text-sidebar-accent-foreground">
              {user.userName.charAt(0).toUpperCase()}
            </span>
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-sidebar-foreground truncate">
              {user.userName}
            </p>
            <p className="text-xs text-sidebar-muted">{roleLabels[user.role]}</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          const Icon = item.icon;

          return (
            <NavLink key={item.path} to={item.path}>
              <Button
                variant={isActive ? 'sidebar-active' : 'sidebar'}
                size="sidebar"
                className="w-full"
              >
                <Icon className="h-5 w-5 mr-3" />
                {item.label}
              </Button>
            </NavLink>
          );
        })}
      </nav>

      {/* Logout */}
      <div className="p-4 border-t border-sidebar-border">
        <Button
          variant="sidebar"
          size="sidebar"
          onClick={logout}
          className="w-full text-destructive hover:text-destructive hover:bg-destructive/10"
        >
          <LogOut className="h-5 w-5 mr-3" />
          Sign Out
        </Button>
      </div>
    </aside>
  );
};
