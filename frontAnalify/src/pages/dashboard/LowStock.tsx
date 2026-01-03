import React, { useState } from 'react';
import { AlertTriangle, Package, Store, RefreshCw, Loader2 } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/shared/DataTable';
import { FilterPanel } from '@/components/shared/FilterPanel';
import { StatCard } from '@/components/shared/StatCard';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';
import { LowStockItem, FilterConfig, SearchConfig } from '@/types';
import { productsApi } from '@/services/api';

const searchConfig: SearchConfig = {
  searchableFields: [
    { value: 'productName', label: 'Product Name' },
    { value: 'storeCity', label: 'Store City' },
  ],
};

const LowStock: React.FC = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('');

  const isInvestor = user?.role === 'INVESTOR';
  const isGeneralAdmin = user?.role === 'ADMIN_GENERAL' || user?.role === 'ADMIN_G';
  const hasAccess = isInvestor || isGeneralAdmin;

  // Check access
  if (!hasAccess) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Access Denied: You don't have permission to view low stock alerts</p>
      </div>
    );
  }

  // Fetch all low stock alerts from API
  const { data: allItems = [], isLoading, error } = useQuery({
    queryKey: ['lowStockAlerts'],
    queryFn: async () => {
      const data = await productsApi.getLowStockAlerts();
      return data as LowStockItem[];
    },
  });

  // Apply client-side filtering
  const items = allItems.filter(item => {
    // Apply store city filter
    if (activeFilters.storeCity && item.storeCity !== activeFilters.storeCity) return false;
    
    // Apply quantity range filter (FilterPanel sends quantity_min and quantity_max)
    if (activeFilters.quantity_min && item.quantity < Number(activeFilters.quantity_min)) return false;
    if (activeFilters.quantity_max && item.quantity > Number(activeFilters.quantity_max)) return false;
    
    // Apply search filter
    if (searchQuery && searchField) {
      const value = (item as any)[searchField];
      if (!value || !String(value).toLowerCase().includes(searchQuery.toLowerCase())) return false;
    }
    
    return true;
  });

  const criticalItems = items.filter(item => item.quantity <= 5);
  const warningItems = items.filter(item => item.quantity > 5 && item.quantity <= 10);
  const uniqueStores = new Set(items.map(item => item.storeId)).size;

  // Extract unique cities for filter options
  const uniqueCities = Array.from(new Set(allItems.map(item => item.storeCity)))
    .map(city => ({ value: city, label: city }));

  // Filter configuration with dynamic options
  const filters: FilterConfig[] = [
    {
      field: 'storeCity',
      label: 'Store City',
      type: 'select',
      options: uniqueCities,
    },
    { field: 'quantity', label: 'Quantity Range', type: 'number-range' },
  ];

  const handleRequestRestock = (item: LowStockItem) => {
    toast({
      title: 'Restock Requested',
      description: `Restock request sent for "${item.productName}" at ${item.storeCity}.`,
    });
  };

  const handleBulkRestock = () => {
    toast({
      title: 'Bulk Restock Requested',
      description: `Restock requests sent for all ${criticalItems.length} critical items.`,
    });
  };

  const columns = [
    { key: 'storeId', header: 'Store ID', sortable: true },
    { key: 'storeCity', header: 'Store City', sortable: true },
    { key: 'productName', header: 'Product Name', sortable: true },
    {
      key: 'quantity',
      header: 'Quantity',
      sortable: true,
      render: (item: LowStockItem) => (
        <span
          className={`font-medium ${
            item.quantity <= 5
              ? 'text-destructive'
              : item.quantity <= 10
              ? 'text-warning'
              : 'text-foreground'
          }`}
        >
          {item.quantity}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (item: LowStockItem) => (
        <span
          className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${
            item.quantity <= 5
              ? 'bg-destructive/10 text-destructive'
              : 'bg-warning/10 text-warning'
          }`}
        >
          <AlertTriangle className="h-3 w-3" />
          {item.quantity <= 5 ? 'Critical' : 'Warning'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (item: LowStockItem) => (
        <Button
          variant="outline"
          size="sm"
          onClick={() => handleRequestRestock(item)}
          className="gap-1"
        >
          <RefreshCw className="h-3 w-3" />
          Request Restock
        </Button>
      ),
    },
  ];

  return (
    <div className="animate-fade-in">
      <div className="page-header flex items-center justify-between">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <AlertTriangle className="h-6 w-6 text-warning" />
            Low Stock Alerts
          </h1>
          <p className="page-description">
            Monitor and manage products with low inventory levels across stores
          </p>
        </div>
        <Button onClick={handleBulkRestock} disabled={criticalItems.length === 0}>
          <RefreshCw className="h-4 w-4 mr-2" />
          Restock All Critical ({criticalItems.length})
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <span className="ml-2">Loading low stock alerts...</span>
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center h-64 gap-4">
          <p className="text-destructive">Failed to load low stock alerts: {(error as Error).message}</p>
          <Button onClick={() => window.location.reload()}>
            Retry
          </Button>
        </div>
      ) : (
        <>
          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <StatCard
              title="Total Low Stock Items"
              value={String(items.length)}
              icon={Package}
              description="Across all stores"
            />
            <StatCard
              title="Critical (≤5 units)"
              value={String(criticalItems.length)}
              icon={AlertTriangle}
              trend={{ value: criticalItems.length, isPositive: false }}
              description="Immediate attention needed"
            />
            <StatCard
              title="Warning (6-10 units)"
              value={String(warningItems.length)}
              icon={Package}
              description="Monitor closely"
            />
            <StatCard
              title="Affected Stores"
              value={String(uniqueStores)}
              icon={Store}
              description="Stores with low stock"
            />
          </div>

          <FilterPanel
            filters={filters}
            searchConfig={searchConfig}
            onFilterChange={(f) => setActiveFilters(f)}
            onSearch={(q, f) => { setSearchQuery(q); setSearchField(f); }}
          />

          <DataTable columns={columns} data={items} defaultPageSize={10} />
        </>
      )}
    </div>
  );
};

export default LowStock;
