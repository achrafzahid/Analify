import React, { useState } from 'react';
import { Plus, Eye, Trash2, Edit2, ShoppingCart, Search, X, Loader2 } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { DataTable } from '@/components/shared/DataTable';
import { FilterPanel } from '@/components/shared/FilterPanel';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';
import { Order, Product, FilterConfig, SearchConfig } from '@/types';
import { ordersApi, productsApi, CreateOrderRequest } from '@/services/api';

interface NewOrderItem {
  product: Product | null;
  quantity: number;
}

const Orders: React.FC = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const isGeneralAdmin = user?.role === 'ADMIN_GENERAL' || user?.role === 'ADMIN_G';
  const isStoreAdmin = user?.role === 'ADMIN_STORE';
  const isCaissier = user?.role === 'CAISSIER';
  const hasAccess = isGeneralAdmin || isStoreAdmin || isCaissier;

  // Check access
  if (!hasAccess) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Access Denied: You don't have permission to view orders</p>
      </div>
    );
  }

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [newItems, setNewItems] = useState<NewOrderItem[]>([{ product: null, quantity: 1 }]);
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('');
  const [editShipDate, setEditShipDate] = useState('');

  // Build API filter object from active filters
  const buildApiFilters = () => {
    const apiFilters: any = {};
    
    // Always filter by store for ADMIN_STORE
    if (isStoreAdmin && user?.storeId) {
      apiFilters.filterStoreId = user.storeId;
    }
    
    // Add user-selected filters
    if (activeFilters.storeId) apiFilters.filterStoreId = Number(activeFilters.storeId);
    if (activeFilters.regionId) apiFilters.filterRegionId = Number(activeFilters.regionId);
    if (activeFilters.stateId) apiFilters.filterStateId = Number(activeFilters.stateId);
    if (activeFilters.cashierId) apiFilters.filterCaissierId = Number(activeFilters.cashierId);
    
    return apiFilters;
  };

  // Fetch orders from API with filters
  const { data: allOrders = [], isLoading: ordersLoading, error: ordersError } = useQuery({
    queryKey: ['orders', activeFilters, isStoreAdmin ? user?.storeId : null],
    queryFn: async () => {
      const data = await ordersApi.getAll(buildApiFilters());
      return data as Order[];
    },
  });

  // Apply client-side search and date filtering
  const orders = allOrders.filter(order => {
    // Apply date range filter (FilterPanel sends orderDate_from and orderDate_to)\n    if (activeFilters.orderDate_from && order.orderDate < activeFilters.orderDate_from) return false;
    if (activeFilters.orderDate_to && order.orderDate > activeFilters.orderDate_to) return false;
    
    // Apply search filter
    if (searchQuery && searchField) {
      const value = (order as any)[searchField];
      if (!value || !String(value).toLowerCase().includes(searchQuery.toLowerCase())) return false;
    }
    
    return true;
  });

  // Fetch products for order creation
  const { data: products = [], isLoading: productsLoading } = useQuery({
    queryKey: ['products'],
    queryFn: async () => {
      const data = await productsApi.getAll();
      return data as Product[];
    },
  });

  // Create order mutation
  const createMutation = useMutation({
    mutationFn: (data: CreateOrderRequest) => ordersApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast({
        title: 'Order Created',
        description: 'Order has been created successfully.',
      });
      setIsCreateOpen(false);
      setNewItems([{ product: null, quantity: 1 }]);
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to create order',
        variant: 'destructive',
      });
    },
  });

  // Delete order mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => ordersApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast({
        title: 'Order Deleted',
        description: 'Order has been deleted.',
      });
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to delete order',
        variant: 'destructive',
      });
    },
  });

  // Update order mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, shipDate }: { id: number; shipDate: string }) => 
      ordersApi.updateShipDate(id, shipDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast({
        title: 'Order Updated',
        description: 'Ship date has been updated successfully.',
      });
      setIsEditOpen(false);
      setSelectedOrder(null);
      setEditShipDate('');
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to update order',
        variant: 'destructive',
      });
    },
  });

  const handleView = (order: Order) => {
    setSelectedOrder(order);
    setIsViewOpen(true);
  };

  const handleEdit = (order: Order) => {
    setSelectedOrder(order);
    setEditShipDate(order.shipDate || '');
    setIsEditOpen(true);
  };

  const handleUpdateOrder = () => {
    if (!selectedOrder || !editShipDate) {
      toast({
        title: 'Error',
        description: 'Please select a ship date',
        variant: 'destructive',
      });
      return;
    }
    updateMutation.mutate({ id: selectedOrder.orderId, shipDate: editShipDate });
  };

  const handleDelete = (orderId: number) => {
    if (confirm('Are you sure you want to delete this order?')) {
      deleteMutation.mutate(orderId);
    }
  };

  const addItem = () => {
    setNewItems([...newItems, { product: null, quantity: 1 }]);
  };

  const updateItem = (index: number, field: keyof NewOrderItem, value: Product | null | number) => {
    const updated = [...newItems];
    updated[index] = { ...updated[index], [field]: value };
    setNewItems(updated);
  };

  const removeItem = (index: number) => {
    if (newItems.length > 1) {
      setNewItems(newItems.filter((_, i) => i !== index));
    }
  };

  const calculateTotal = () => {
    return newItems.reduce((sum, item) => {
      if (item.product) {
        return sum + item.quantity * item.product.price;
      }
      return sum;
    }, 0);
  };

  const handleCreateOrder = () => {
    const validItems = newItems.filter(item => item.product !== null);
    if (validItems.length === 0) {
      toast({
        title: 'Error',
        description: 'Please add at least one product to the order.',
        variant: 'destructive',
      });
      return;
    }

    const orderRequest: CreateOrderRequest = {
      cashierId: user?.userId || 0,
      items: validItems.map(item => ({
        productId: item.product!.productId,
        quantity: item.quantity,
        discount: 0,
      })),
    };

    createMutation.mutate(orderRequest);
  };

  if (ordersLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2">Loading orders...</span>
      </div>
    );
  }

  if (ordersError) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Failed to load orders: {(ordersError as Error).message}</p>
        <Button onClick={() => queryClient.invalidateQueries({ queryKey: ['orders'] })}>
          Retry
        </Button>
      </div>
    );
  }

  const columns = [
    { key: 'orderId', header: 'Order ID', sortable: true },
    { key: 'orderDate', header: 'Order Date', sortable: true },
    { key: 'shipDate', header: 'Ship Date', sortable: true },
    ...(isGeneralAdmin ? [
      { key: 'storeName', header: 'Store', sortable: true },
      { key: 'cashierName', header: 'Cashier', sortable: true },
    ] : []),
    ...(isStoreAdmin ? [
      { key: 'cashierName', header: 'Cashier', sortable: true },
    ] : []),
    { key: 'totalItems', header: 'Items', sortable: true },
    {
      key: 'totalAmount',
      header: 'Total',
      sortable: true,
      render: (order: Order) => `$${order.totalAmount.toFixed(2)}`,
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (order: Order) => (
        <div className="flex gap-2">
          <Button variant="ghost" size="icon" onClick={() => handleView(order)}>
            <Eye className="h-4 w-4" />
          </Button>
          {(isStoreAdmin || isGeneralAdmin) && (
            <Button variant="ghost" size="icon" onClick={() => handleEdit(order)}>
              <Edit2 className="h-4 w-4" />
            </Button>
          )}
          {(isCaissier || isGeneralAdmin) && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => handleDelete(order.orderId)}
            >
              <Trash2 className="h-4 w-4 text-destructive" />
            </Button>
          )}
        </div>
      ),
    },
  ];

  // Extract unique stores and cashiers from all orders for filter options
  const uniqueStores = Array.from(new Set(allOrders.map(o => JSON.stringify({ value: String(o.storeId), label: o.storeName }))))
    .map(s => JSON.parse(s));
  const uniqueCashiers = Array.from(new Set(allOrders.map(o => JSON.stringify({ value: String(o.cashierId), label: o.cashierName }))))
    .map(s => JSON.parse(s));

  // Filter configuration - only show for ADMIN_GENERAL
  const filters: FilterConfig[] = isGeneralAdmin ? [
    {
      field: 'storeId',
      label: 'Store',
      type: 'select',
      options: uniqueStores,
    },
    {
      field: 'cashierId',
      label: 'Cashier',
      type: 'select',
      options: uniqueCashiers,
    },
    { field: 'orderDate', label: 'Order Date', type: 'date-range' },
  ] : [];

  const searchConfig: SearchConfig = {
    searchableFields: [
      { value: 'orderId', label: 'Order ID' },
      { value: 'storeName', label: 'Store Name' },
      { value: 'cashierName', label: 'Cashier Name' },
    ],
  };

  const handleFilterChange = (newFilters: Record<string, string>) => {
    setActiveFilters(newFilters);
  };

  const handleSearch = (query: string, field: string) => {
    setSearchQuery(query);
    setSearchField(field);
  };

  return (
    <div className="animate-fade-in">
      <div className="page-header flex items-center justify-between">
        <div>
          <h1 className="page-title">{isStoreAdmin ? 'Store Orders' : 'Manage Orders'}</h1>
          <p className="page-description">{isStoreAdmin ? 'View and manage store orders' : 'View and create customer orders'}</p>
        </div>
        {isCaissier && (
          <Button onClick={() => setIsCreateOpen(true)}>
            <Plus className="h-4 w-4 mr-2" />
            Create Order
          </Button>
        )}
      </div>

      {isGeneralAdmin && (
        <FilterPanel
          filters={filters}
          searchConfig={searchConfig}
          onFilterChange={handleFilterChange}
          onSearch={handleSearch}
        />
      )}

      <DataTable columns={columns} data={orders} />

      {/* Create Order Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ShoppingCart className="h-5 w-5" />
              Create New Order
            </DialogTitle>
            <DialogDescription>
              Search and add products to create a new order
            </DialogDescription>
          </DialogHeader>

          {productsLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
              <span className="ml-2">Loading products...</span>
            </div>
          ) : (
            <>
              <div className="space-y-4">
                {newItems.map((item, index) => (
                  <div key={index} className="flex gap-3 items-end p-3 bg-muted/30 rounded-lg">
                    <div className="flex-1 space-y-2">
                      <Label>Product</Label>
                      <ProductSearch
                        products={products}
                        selectedProduct={item.product}
                        onSelect={(product) => updateItem(index, 'product', product)}
                      />
                    </div>
                    <div className="w-24 space-y-2">
                      <Label>Quantity</Label>
                      <Input
                        type="number"
                        min={1}
                        value={item.quantity}
                        onChange={(e) =>
                          updateItem(index, 'quantity', parseInt(e.target.value) || 1)
                        }
                      />
                    </div>
                    <div className="w-28 space-y-2">
                      <Label>Line Total</Label>
                      <div className="h-9 flex items-center text-sm font-medium">
                        ${item.product ? (item.quantity * item.product.price).toFixed(2) : '0.00'}
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => removeItem(index)}
                      disabled={newItems.length === 1}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                ))}
              </div>

              <Button variant="outline" onClick={addItem} className="w-full">
                <Plus className="h-4 w-4 mr-2" />
                Add Product
              </Button>

              <div className="flex justify-between items-center pt-4 border-t">
                <div className="text-lg font-semibold">
                  Total: ${calculateTotal().toFixed(2)}
                </div>
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={handleCreateOrder} disabled={createMutation.isPending}>
                  {createMutation.isPending ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Creating...
                    </>
                  ) : (
                    'Validate Order'
                  )}
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* View Order Dialog */}
      <Dialog open={isViewOpen} onOpenChange={setIsViewOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Order #{selectedOrder?.orderId}</DialogTitle>
          </DialogHeader>
          {selectedOrder && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                  <Label className="text-muted-foreground">Order Date</Label>
                  <p className="font-medium">{selectedOrder.orderDate}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Ship Date</Label>
                  <p className="font-medium">{selectedOrder.shipDate}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Store</Label>
                  <p className="font-medium">{selectedOrder.storeName}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Cashier</Label>
                  <p className="font-medium">{selectedOrder.cashierName}</p>
                </div>
              </div>

              <div>
                <Label className="text-muted-foreground mb-2 block">Order Items</Label>
                <div className="rounded-lg border border-border overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-muted">
                      <tr>
                        <th className="px-3 py-2 text-left">Product</th>
                        <th className="px-3 py-2 text-left">Category</th>
                        <th className="px-3 py-2 text-right">Price</th>
                        <th className="px-3 py-2 text-right">Qty</th>
                        <th className="px-3 py-2 text-right">Discount</th>
                        <th className="px-3 py-2 text-right">Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedOrder.items.map((item) => (
                        <tr key={item.itemId} className="border-t border-border">
                          <td className="px-3 py-2">{item.productName}</td>
                          <td className="px-3 py-2">{item.categoryName}</td>
                          <td className="px-3 py-2 text-right">${item.price.toFixed(2)}</td>
                          <td className="px-3 py-2 text-right">{item.quantity}</td>
                          <td className="px-3 py-2 text-right">{(item.discount * 100).toFixed(0)}%</td>
                          <td className="px-3 py-2 text-right font-medium">${item.lineTotal.toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot className="bg-muted/50">
                      <tr>
                        <td colSpan={5} className="px-3 py-2 text-right font-semibold">Total</td>
                        <td className="px-3 py-2 text-right font-bold">${selectedOrder.totalAmount.toFixed(2)}</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button onClick={() => setIsViewOpen(false)}>Close</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Order Dialog */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Order</DialogTitle>
            <DialogDescription>
              Update the ship date for order #{selectedOrder?.orderId}
            </DialogDescription>
          </DialogHeader>
          {selectedOrder && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">Order ID</Label>
                  <p className="font-medium">#{selectedOrder.orderId}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Order Date</Label>
                  <p className="font-medium">{selectedOrder.orderDate}</p>
                </div>
              </div>
              <div>
                <Label htmlFor="shipDate">Ship Date</Label>
                <Input
                  id="shipDate"
                  type="date"
                  value={editShipDate}
                  onChange={(e) => setEditShipDate(e.target.value)}
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleUpdateOrder} disabled={updateMutation.isPending}>
              {updateMutation.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Updating...
                </>
              ) : (
                'Update Order'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

// Product Search Component
interface ProductSearchProps {
  products: Product[];
  selectedProduct: Product | null;
  onSelect: (product: Product | null) => void;
}

const ProductSearch: React.FC<ProductSearchProps> = ({ products, selectedProduct, onSelect }) => {
  const [open, setOpen] = useState(false);
  const [searchValue, setSearchValue] = useState('');

  const filteredProducts = products.filter((product) =>
    product.productName.toLowerCase().includes(searchValue.toLowerCase())
  );

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className="w-full justify-between h-9 font-normal"
        >
          {selectedProduct ? (
            <span className="truncate">{selectedProduct.productName}</span>
          ) : (
            <span className="text-muted-foreground">Search products...</span>
          )}
          {selectedProduct ? (
            <X
              className="h-4 w-4 shrink-0 opacity-50 hover:opacity-100"
              onClick={(e) => {
                e.stopPropagation();
                onSelect(null);
              }}
            />
          ) : (
            <Search className="h-4 w-4 shrink-0 opacity-50" />
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[400px] p-0" align="start">
        <Command>
          <CommandInput
            placeholder="Search by product name..."
            value={searchValue}
            onValueChange={setSearchValue}
          />
          <CommandList>
            <CommandEmpty>No products found.</CommandEmpty>
            <CommandGroup>
              {filteredProducts.slice(0, 50).map((product) => (
                <CommandItem
                  key={product.productId}
                  value={product.productName}
                  onSelect={() => {
                    onSelect(product);
                    setOpen(false);
                    setSearchValue('');
                  }}
                  className="flex justify-between"
                >
                  <div>
                    <span className="font-medium">{product.productName}</span>
                    <span className="text-xs text-muted-foreground ml-2">
                      {product.categoryName}
                    </span>
                  </div>
                  <span className="text-sm font-medium">${product.price.toFixed(2)}</span>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
};

export default Orders;
