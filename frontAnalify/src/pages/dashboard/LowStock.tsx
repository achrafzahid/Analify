import React, { useState, useMemo } from 'react';
import { AlertTriangle, Package, Store, RefreshCw, Loader2 } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { DataTable } from '@/components/shared/DataTable';
import { FilterPanel } from '@/components/shared/FilterPanel';
import { StatCard } from '@/components/shared/StatCard';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';
import { LowStockItem, FilterConfig, SearchConfig } from '@/types';
import { productsApi, UpdateStockRequest } from '@/services/api';

const searchConfig: SearchConfig = {
  searchableFields: [
    { value: 'productName', label: 'Product Name' },
    { value: 'storeCity', label: 'Store City' },
  ],
};

const LowStock: React.FC = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('');
  const [isStockDialogOpen, setIsStockDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<LowStockItem | null>(null);
  const [stockQuantity, setStockQuantity] = useState<number>(0);
  const [suggestedStock, setSuggestedStock] = useState<number>(0);
  const [selectedStoreId, setSelectedStoreId] = useState<number | undefined>(undefined);

  const isInvestor = user?.role === 'INVESTOR';
  const isGeneralAdmin = user?.role === 'ADMIN_GENERAL' || user?.role === 'ADMIN_G';
  const isStoreAdmin = user?.role === 'ADMIN_STORE';
  const hasAccess = isInvestor || isGeneralAdmin || isStoreAdmin;

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
    queryKey: ['lowStockAlerts', selectedStoreId],
    queryFn: async () => {
      const data = await productsApi.getLowStockAlerts(selectedStoreId);
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

  // Extract unique stores for ADMIN_G selector
  const availableStores = useMemo(() => {
    const storeMap = new Map<number, string>();
    allItems.forEach(item => {
      storeMap.set(item.storeId, item.storeCity);
    });
    return Array.from(storeMap.entries()).map(([id, city]) => ({ id, city }));
  }, [allItems]);

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

  // Update stock mutation
  const updateStockMutation = useMutation({
    mutationFn: ({ productId, data }: { productId: number; data: UpdateStockRequest }) =>
      productsApi.updateStock(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lowStockAlerts'] });
      toast({
        title: 'Stock Updated',
        description: 'Product stock has been successfully refilled.',
      });
      setIsStockDialogOpen(false);
      setSelectedItem(null);
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to update stock',
        variant: 'destructive',
      });
    },
  });

  const handleRequestRestock = async (item: LowStockItem) => {
    setSelectedItem(item);
    setIsStockDialogOpen(true);
    
    // Fetch suggested stock quantity
    try {
      const suggested = await productsApi.getSuggestedStock(item.productId, item.storeId);
      setSuggestedStock(suggested);
      setStockQuantity(suggested);
    } catch (error) {
      toast({
        title: 'Warning',
        description: 'Could not fetch suggested stock. Using default value.',
        variant: 'default',
      });
      setSuggestedStock(50);
      setStockQuantity(50);
    }
  };

  const handleConfirmStockRefill = () => {
    if (!selectedItem) return;
    
    updateStockMutation.mutate({
      productId: selectedItem.productId,
      data: {
        storeId: selectedItem.storeId,
        quantity: stockQuantity,
      },
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

      {/* Store Selector for ADMIN_G */}
      {isGeneralAdmin && (
        <div className="mb-6 bg-muted/50 rounded-lg p-4">
          <div className="flex items-center gap-4">
            <Label htmlFor="store-select" className="whitespace-nowrap">
              Filter by Store:
            </Label>
            <Select
              value={selectedStoreId?.toString() || 'all'}
              onValueChange={(value) => setSelectedStoreId(value === 'all' ? undefined : Number(value))}
            >
              <SelectTrigger id="store-select" className="w-[300px]">
                <SelectValue placeholder="All Stores" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Stores</SelectItem>
                {availableStores.map((store) => (
                  <SelectItem key={store.id} value={store.id.toString()}>
                    Store {store.id} - {store.city}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      )}

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

      {/* Stock Refill Dialog */}
      <Dialog open={isStockDialogOpen} onOpenChange={setIsStockDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <RefreshCw className="h-5 w-5" />
              Refill Stock
            </DialogTitle>
            <DialogDescription>
              Add stock for {selectedItem?.productName} at {selectedItem?.storeCity}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label className="text-muted-foreground">Store</Label>
                <Input 
                  value={selectedItem?.storeCity || ''} 
                  disabled 
                  className="bg-muted" 
                />
              </div>
              <div className="space-y-2">
                <Label className="text-muted-foreground">Current Stock</Label>
                <Input 
                  value={selectedItem?.quantity || 0} 
                  disabled 
                  className="bg-muted" 
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label>Quantity to Add</Label>
              <Input
                type="number"
                min="1"
                value={stockQuantity}
                onChange={(e) => setStockQuantity(parseInt(e.target.value) || 0)}
                placeholder="Enter quantity"
              />
              <p className="text-sm text-muted-foreground">
                Suggested: {suggestedStock} units
                {suggestedStock !== stockQuantity && (
                  <Button
                    variant="link"
                    size="sm"
                    className="ml-2 h-auto p-0"
                    onClick={() => setStockQuantity(suggestedStock)}
                  >
                    Use suggested
                  </Button>
                )}
              </p>
            </div>

            <div className="rounded-lg bg-muted p-3">
              <p className="text-sm font-medium">New Total Stock</p>
              <p className="text-2xl font-bold">
                {(selectedItem?.quantity || 0) + stockQuantity} units
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => {
                setIsStockDialogOpen(false);
                setSelectedItem(null);
              }}
            >
              Cancel
            </Button>
            <Button 
              onClick={handleConfirmStockRefill} 
              disabled={updateStockMutation.isPending || stockQuantity <= 0}
            >
              {updateStockMutation.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Updating...
                </>
              ) : (
                'Confirm Refill'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default LowStock;
