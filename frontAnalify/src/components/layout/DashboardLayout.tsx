import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { useAuth } from '@/contexts/AuthContext';
import { UserRole } from '@/types';
import { LucideIcon, ShoppingCart, Users, BarChart3, User, Package, AlertTriangle, TrendingUp, Trophy, LayoutDashboard } from 'lucide-react';

interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
}

const roleNavItems: Record<UserRole, NavItem[]> = {
  CAISSIER: [
    { label: 'Manage Orders', path: '/dashboard/orders', icon: ShoppingCart },
    { label: 'Personal Data', path: '/dashboard/profile', icon: User },
  ],
  ADMIN_STORE: [
    { label: 'Store Orders', path: '/dashboard/orders', icon: ShoppingCart },
    { label: 'Manage Employees', path: '/dashboard/employees', icon: Users },
    { label: 'Low Stock Alerts', path: '/dashboard/low-stock', icon: AlertTriangle },
    { label: 'Statistics', path: '/dashboard/statistics', icon: BarChart3 },
    { label: 'Personal Data', path: '/dashboard/profile', icon: User },
  ],
  INVESTOR: [
    { label: 'Bidding Overview', path: '/dashboard/bidding-overview', icon: LayoutDashboard },
    { label: 'Browse Sections', path: '/dashboard/bidding', icon: TrendingUp },
    { label: 'My Bids', path: '/dashboard/my-bids', icon: Trophy },
    { label: 'My Products', path: '/dashboard/products', icon: Package },
    { label: 'Low Stock', path: '/dashboard/low-stock', icon: AlertTriangle },
    { label: 'Statistics', path: '/dashboard/statistics', icon: BarChart3 },
    { label: 'Personal Data', path: '/dashboard/profile', icon: User },
  ],
  ADMIN_GENERAL: [
    { label: 'Manage Orders', path: '/dashboard/orders', icon: ShoppingCart },
    { label: 'Manage Employees', path: '/dashboard/employees', icon: Users },
    { label: 'All Products', path: '/dashboard/products', icon: Package },
    { label: 'Low Stock Alerts', path: '/dashboard/low-stock', icon: AlertTriangle },
    { label: 'Global Statistics', path: '/dashboard/statistics', icon: BarChart3 },
    { label: 'Personal Data', path: '/dashboard/profile', icon: User },
  ],
  // Also handle ADMIN_G in case it comes through before normalization
  ADMIN_G: [
    { label: 'Manage Orders', path: '/dashboard/orders', icon: ShoppingCart },
    { label: 'Manage Employees', path: '/dashboard/employees', icon: Users },
    { label: 'All Products', path: '/dashboard/products', icon: Package },
    { label: 'Low Stock Alerts', path: '/dashboard/low-stock', icon: AlertTriangle },
    { label: 'Global Statistics', path: '/dashboard/statistics', icon: BarChart3 },
    { label: 'Personal Data', path: '/dashboard/profile', icon: User },
  ],
};

export const DashboardLayout: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="animate-pulse text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/" replace />;
  }

  const navItems = roleNavItems[user.role] || [];

  return (
    <div className="dashboard-container">
      <Sidebar navItems={navItems} />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};
