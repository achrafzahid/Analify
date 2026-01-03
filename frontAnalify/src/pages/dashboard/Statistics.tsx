import React, { useState } from 'react';
import {
  ShoppingCart,
  Package,
  Users,
  DollarSign,
  TrendingUp,
  Store,
  Briefcase,
  Loader2,
} from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { StatCard } from '@/components/shared/StatCard';
import { FilterPanel } from '@/components/shared/FilterPanel';
import { LineChartCard, BarChartCard, PieChartCard, AreaChartCard } from '@/components/charts';
import { useAuth } from '@/contexts/AuthContext';
import { FilterConfig, SearchConfig, DashboardStats } from '@/types';
import { statsApi } from '@/services/api';

// Sample data for charts
const ordersOverTimeData = [
  { month: 'Jan', orders: 120, revenue: 15000 },
  { month: 'Feb', orders: 145, revenue: 18500 },
  { month: 'Mar', orders: 162, revenue: 21000 },
  { month: 'Apr', orders: 138, revenue: 17200 },
  { month: 'May', orders: 189, revenue: 24500 },
  { month: 'Jun', orders: 201, revenue: 26800 },
];

const productSalesData = [
  { name: 'Office Supplies', value: 4500 },
  { name: 'Technology', value: 3200 },
  { name: 'Furniture', value: 2100 },
  { name: 'Storage', value: 1800 },
];

const stockLevelsData = [
  { category: 'Paper', inStock: 85, lowStock: 12, outOfStock: 3 },
  { category: 'Tech', inStock: 62, lowStock: 8, outOfStock: 5 },
  { category: 'Furniture', inStock: 45, lowStock: 15, outOfStock: 10 },
  { category: 'Storage', inStock: 72, lowStock: 18, outOfStock: 2 },
];

const employeePerformanceData = [
  { name: 'John', sales: 45, orders: 120 },
  { name: 'Sarah', sales: 52, orders: 145 },
  { name: 'Mike', sales: 38, orders: 98 },
  { name: 'Emma', sales: 61, orders: 167 },
  { name: 'Alex', sales: 49, orders: 132 },
];

const storePerformanceData = [
  { store: 'Gilbert', revenue: 125000, orders: 890 },
  { store: 'Phoenix', revenue: 189000, orders: 1230 },
  { store: 'Tucson', revenue: 98000, orders: 650 },
  { store: 'Scottsdale', revenue: 156000, orders: 1020 },
  { store: 'Mesa', revenue: 112000, orders: 780 },
];

const revenueTrendData = [
  { month: 'Jan', revenue: 520000, target: 500000 },
  { month: 'Feb', revenue: 580000, target: 550000 },
  { month: 'Mar', revenue: 620000, target: 600000 },
  { month: 'Apr', revenue: 590000, target: 620000 },
  { month: 'May', revenue: 680000, target: 650000 },
  { month: 'Jun', revenue: 750000, target: 700000 },
];

const orderStatusData = [
  { name: 'Completed', value: 42345 },
  { name: 'Processing', value: 2456 },
  { name: 'Shipped', value: 877 },
];

const investorDistributionData = [
  { name: 'Premier', value: 3200000 },
  { name: 'Acme', value: 2800000 },
  { name: 'TechCorp', value: 2100000 },
  { name: 'ComfortCo', value: 1900000 },
  { name: 'Others', value: 2400000 },
];

const stockTrendsData = [
  { week: 'W1', inStock: 89, lowStock: 8, outOfStock: 3 },
  { week: 'W2', inStock: 85, lowStock: 10, outOfStock: 5 },
  { week: 'W3', inStock: 88, lowStock: 9, outOfStock: 3 },
  { week: 'W4', inStock: 91, lowStock: 6, outOfStock: 3 },
];

const CHART_COLORS = {
  primary: 'hsl(217, 91%, 60%)',
  success: 'hsl(142, 76%, 36%)',
  warning: 'hsl(38, 92%, 50%)',
  info: 'hsl(199, 89%, 48%)',
  destructive: 'hsl(0, 84%, 60%)',
  purple: 'hsl(262, 83%, 58%)',
};

const PIE_COLORS = [CHART_COLORS.primary, CHART_COLORS.success, CHART_COLORS.warning, CHART_COLORS.info, CHART_COLORS.purple];

const Statistics: React.FC = () => {
  const { user } = useAuth();
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('');

  const isStoreAdmin = user?.role === 'ADMIN_STORE';
  const isInvestor = user?.role === 'INVESTOR';
  const isGeneralAdmin = user?.role === 'ADMIN_GENERAL' || user?.role === 'ADMIN_G';
  const hasAccess = isStoreAdmin || isInvestor || isGeneralAdmin;

  // Check access
  if (!hasAccess) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Access Denied: You don't have permission to view statistics</p>
      </div>
    );
  }

  // Role-based filter configurations
  const getFilters = (): FilterConfig[] => {
    if (isGeneralAdmin) {
      // ADMIN_GENERAL gets all filters
      return [
        { field: 'dateRange', label: 'Date Range', type: 'date-range' },
        {
          field: 'region',
          label: 'Region',
          type: 'select',
          options: [
            { value: 'north', label: 'North' },
            { value: 'south', label: 'South' },
            { value: 'east', label: 'East' },
            { value: 'west', label: 'West' },
          ],
        },
        {
          field: 'state',
          label: 'State',
          type: 'select',
          options: [
            { value: 'california', label: 'California' },
            { value: 'texas', label: 'Texas' },
            { value: 'florida', label: 'Florida' },
            { value: 'new-york', label: 'New York' },
          ],
        },
        {
          field: 'storeId',
          label: 'Store',
          type: 'select',
          options: [], // Would be populated from API
        },
      ];
    } else if (isInvestor) {
      // INVESTOR gets date range, region, state filters
      return [
        { field: 'dateRange', label: 'Date Range', type: 'date-range' },
        {
          field: 'region',
          label: 'Region',
          type: 'select',
          options: [
            { value: 'north', label: 'North' },
            { value: 'south', label: 'South' },
            { value: 'east', label: 'East' },
            { value: 'west', label: 'West' },
          ],
        },
        {
          field: 'state',
          label: 'State',
          type: 'select',
          options: [
            { value: 'california', label: 'California' },
            { value: 'texas', label: 'Texas' },
            { value: 'florida', label: 'Florida' },
            { value: 'new-york', label: 'New York' },
          ],
        },
      ];
    } else {
      // ADMIN_STORE only gets date range
      return [
        { field: 'dateRange', label: 'Date Range', type: 'date-range' },
      ];
    }
  };

  const filters = getFilters();

  const searchConfig: SearchConfig = {
    searchableFields: [{ value: 'all', label: 'All' }],
  };

  const handleFilterChange = (newFilters: Record<string, string>) => {
    setActiveFilters(newFilters);
    // Here you would refetch stats with new filters
    // For now, just log them
    console.log('Statistics filters:', newFilters);
  };

  const handleSearch = (query: string, field: string) => {
    setSearchQuery(query);
    setSearchField(field);
  };

  // Fetch dashboard stats from API
  const { data: dashboardStats, isLoading, error } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: async () => {
      const data = await statsApi.getDashboard();
      return data;
    },
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2">Loading statistics...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Failed to load statistics: {(error as Error).message}</p>
        <Button onClick={() => window.location.reload()}>
          Retry
        </Button>
      </div>
    );
  }

  if (!dashboardStats) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-muted-foreground">No statistics data available</p>
      </div>
    );
  }

  const stats = dashboardStats;

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h1 className="page-title">
          {isGeneralAdmin ? 'Global Statistics' : 'Statistics'}
        </h1>
        <p className="page-description">
          {isGeneralAdmin
            ? 'Overview of all stores and operations'
            : isInvestor
            ? 'Track your product performance and trends'
            : 'Monitor store performance and metrics'}
        </p>
      </div>

      <FilterPanel
        filters={filters}
        searchConfig={searchConfig}
        onFilterChange={handleFilterChange}
        onSearch={handleSearch}
      />

      {/* Store Admin Statistics */}
      {isStoreAdmin && (
        <>
          {/* Order Statistics */}
          <section className="mb-8">
            <h2 className="text-lg font-semibold mb-4">Orders Statistics</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
              <StatCard
                title="Total Orders"
                value="1,234"
                icon={ShoppingCart}
                trend={{ value: 12.5, isPositive: true }}
                description="vs last month"
              />
              <StatCard
                title="Completed Orders"
                value="1,089"
                icon={ShoppingCart}
                trend={{ value: 8.2, isPositive: true }}
              />
              <StatCard
                title="Pending Orders"
                value="98"
                icon={ShoppingCart}
              />
              <StatCard
                title="Order Value"
                value="$45,678"
                icon={DollarSign}
                trend={{ value: 15.3, isPositive: true }}
              />
            </div>
            <LineChartCard
              title="Orders Over Time"
              data={ordersOverTimeData}
              dataKeys={[
                { key: 'orders', color: CHART_COLORS.primary, name: 'Orders' },
                { key: 'revenue', color: CHART_COLORS.success, name: 'Revenue ($)' },
              ]}
              xAxisKey="month"
            />
          </section>

          {/* Product Statistics */}
          <section className="mb-8">
            <h2 className="text-lg font-semibold mb-4">Products Statistics</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <StatCard title="Total Products" value="456" icon={Package} />
              <StatCard
                title="Low Stock Items"
                value="23"
                icon={Package}
                trend={{ value: 5, isPositive: false }}
              />
              <StatCard title="Top Selling" value="Electronics" icon={TrendingUp} />
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <PieChartCard
                title="Product Sales Distribution"
                data={productSalesData}
                colors={PIE_COLORS}
              />
              <BarChartCard
                title="Stock Levels by Category"
                data={stockLevelsData}
                dataKeys={[
                  { key: 'inStock', color: CHART_COLORS.success, name: 'In Stock' },
                  { key: 'lowStock', color: CHART_COLORS.warning, name: 'Low Stock' },
                  { key: 'outOfStock', color: CHART_COLORS.destructive, name: 'Out of Stock' },
                ]}
                xAxisKey="category"
                stacked
              />
            </div>
          </section>

          {/* Employee Statistics */}
          <section>
            <h2 className="text-lg font-semibold mb-4">Employees Statistics</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <StatCard title="Total Employees" value="24" icon={Users} />
              <StatCard
                title="Average Tenure"
                value="2.5 yrs"
                icon={Users}
              />
              <StatCard
                title="Payroll"
                value="$125,000"
                icon={DollarSign}
              />
            </div>
            <BarChartCard
              title="Employee Performance"
              data={employeePerformanceData}
              dataKeys={[
                { key: 'sales', color: CHART_COLORS.primary, name: 'Sales ($K)' },
                { key: 'orders', color: CHART_COLORS.info, name: 'Orders' },
              ]}
              xAxisKey="name"
            />
          </section>
        </>
      )}

      {/* Investor Statistics */}
      {isInvestor && (
        <>
          {/* Product Performance */}
          <section className="mb-8">
            <h2 className="text-lg font-semibold mb-4">Product Performance</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
              <StatCard
                title="Total Products"
                value="156"
                icon={Package}
                trend={{ value: 5, isPositive: true }}
              />
              <StatCard
                title="Total Sales"
                value="$234,567"
                icon={DollarSign}
                trend={{ value: 18.7, isPositive: true }}
              />
              <StatCard
                title="Units Sold"
                value="12,345"
                icon={ShoppingCart}
              />
              <StatCard
                title="Avg. Price"
                value="$19.00"
                icon={TrendingUp}
              />
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <BarChartCard
                title="Sales by Product Category"
                data={stockLevelsData}
                dataKeys={[
                  { key: 'inStock', color: CHART_COLORS.primary, name: 'Units Sold' },
                ]}
                xAxisKey="category"
              />
              <LineChartCard
                title="Revenue Trend"
                data={ordersOverTimeData}
                dataKeys={[
                  { key: 'revenue', color: CHART_COLORS.success, name: 'Revenue ($)' },
                ]}
                xAxisKey="month"
              />
            </div>
          </section>

          {/* Stock Trends */}
          <section>
            <h2 className="text-lg font-semibold mb-4">Stock Trends</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <StatCard title="In Stock" value="89%" icon={Package} />
              <StatCard
                title="Low Stock Alerts"
                value="12"
                icon={Package}
                trend={{ value: 3, isPositive: false }}
              />
              <StatCard title="Out of Stock" value="4" icon={Package} />
            </div>
            <AreaChartCard
              title="Stock Levels Over Time"
              data={stockTrendsData}
              dataKeys={[
                { key: 'inStock', color: CHART_COLORS.success, name: 'In Stock %' },
                { key: 'lowStock', color: CHART_COLORS.warning, name: 'Low Stock %' },
              ]}
              xAxisKey="week"
            />
          </section>
        </>
      )}

      {/* General Admin Statistics */}
      {isGeneralAdmin && (
        <>
          {/* Stores Overview */}
          <section className="mb-8">
            <h2 className="text-lg font-semibold mb-4">Stores Overview</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
              <StatCard
                title="Total Stores"
                value="48"
                icon={Store}
                trend={{ value: 4, isPositive: true }}
              />
              <StatCard
                title="Active Stores"
                value="45"
                icon={Store}
              />
              <StatCard
                title="Total Employees"
                value="1,234"
                icon={Users}
              />
              <StatCard
                title="Avg. Store Revenue"
                value="$156K"
                icon={DollarSign}
              />
            </div>
            <BarChartCard
              title="Store Performance Comparison"
              data={storePerformanceData}
              dataKeys={[
                { key: 'revenue', color: CHART_COLORS.primary, name: 'Revenue ($)' },
                { key: 'orders', color: CHART_COLORS.info, name: 'Orders' },
              ]}
              xAxisKey="store"
            />
          </section>

          {/* Revenue */}
          <section className="mb-8">
            <h2 className="text-lg font-semibold mb-4">Revenue</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <StatCard
                title="Total Revenue"
                value="$7.5M"
                icon={DollarSign}
                trend={{ value: 22.5, isPositive: true }}
              />
              <StatCard
                title="Monthly Revenue"
                value="$625K"
                icon={DollarSign}
                trend={{ value: 8.3, isPositive: true }}
              />
              <StatCard
                title="Growth Rate"
                value="15.2%"
                icon={TrendingUp}
              />
            </div>
            <LineChartCard
              title="Revenue vs Target"
              data={revenueTrendData}
              dataKeys={[
                { key: 'revenue', color: CHART_COLORS.success, name: 'Actual Revenue' },
                { key: 'target', color: CHART_COLORS.warning, name: 'Target' },
              ]}
              xAxisKey="month"
            />
          </section>

          {/* Orders Overview */}
          <section className="mb-8">
            <h2 className="text-lg font-semibold mb-4">Orders</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
              <StatCard title="Total Orders" value="45,678" icon={ShoppingCart} />
              <StatCard title="Completed" value="42,345" icon={ShoppingCart} />
              <StatCard title="Processing" value="2,456" icon={ShoppingCart} />
              <StatCard title="Shipped" value="877" icon={ShoppingCart} />
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <BarChartCard
                title="Orders by Store"
                data={storePerformanceData}
                dataKeys={[
                  { key: 'orders', color: CHART_COLORS.primary, name: 'Orders' },
                ]}
                xAxisKey="store"
              />
              <PieChartCard
                title="Order Status Distribution"
                data={orderStatusData}
                colors={[CHART_COLORS.success, CHART_COLORS.warning, CHART_COLORS.info]}
              />
            </div>
          </section>

          {/* Investors */}
          <section>
            <h2 className="text-lg font-semibold mb-4">Investors</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <StatCard title="Total Investors" value="156" icon={Briefcase} />
              <StatCard
                title="Active Products"
                value="2,345"
                icon={Package}
              />
              <StatCard
                title="Total Investment"
                value="$12.4M"
                icon={DollarSign}
              />
            </div>
            <PieChartCard
              title="Investment Distribution"
              data={investorDistributionData}
              colors={PIE_COLORS}
            />
          </section>
        </>
      )}
    </div>
  );
};

export default Statistics;
