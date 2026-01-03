import React, { useState, useMemo } from 'react';
import { Plus, Eye, Edit2, Trash2, ShoppingCart, Search, X, Loader2 } from 'lucide-react';
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
import { Order, OrderItem, FilterConfig, SearchConfig, Product } from '@/types';
import { ordersApi, productsApi, CreateOrderRequest } from '@/services/api';

// Mock products for selection (fallback)
const mockProducts: Product[] = [
  { productId: 368, productName: 'easy-staple paper', price: 11.952, categoryName: 'office supplies', subId: 1, subName: 'paper', investorId: 1644, investorName: 'premier', quantity: 100 },
  { productId: 190, productName: 'xerox 1957', price: 15.552, categoryName: 'office supplies', subId: 1, subName: 'paper', investorId: 1644, investorName: 'premier', quantity: 50 },
  { productId: 201, productName: 'fellowes staxonsteel drawer files', price: 234.99, categoryName: 'storage', subId: 2, subName: 'filing', investorId: 1200, investorName: 'acme', quantity: 25 },
  { productId: 205, productName: 'hp deskjet printer', price: 199.99, categoryName: 'technology', subId: 3, subName: 'printers', investorId: 1300, investorName: 'techcorp', quantity: 15 },
  { productId: 210, productName: 'logitech wireless mouse', price: 29.99, categoryName: 'technology', subId: 4, subName: 'accessories', investorId: 1300, investorName: 'techcorp', quantity: 200 },
  { productId: 215, productName: 'ergonomic office chair', price: 349.99, categoryName: 'furniture', subId: 5, subName: 'seating', investorId: 1400, investorName: 'comfortco', quantity: 30 },
];

// Mock orders with new format (fallback)
const mockOrders: Order[] = [
  {
    orderId: 111703,
    orderDate: '2024-07-02',
    shipDate: '2024-07-09',
    cashierId: 317,
    cashierName: 'amandafreeman',
    storeId: 203,
    storeName: 'Gilbert',
    totalAmount: 81.312,
    totalItems: 2,
    items: [
      { itemId: 2698, productId: 368, productName: 'easy-staple paper', categoryName: 'office supplies', price: 11.952, discount: 0.2, quantity: 3, lineTotal: 35.256 },
      { itemId: 2699, productId: 190, productName: 'xerox 1957', categoryName: 'office supplies', price: 15.552, discount: 0.2, quantity: 3, lineTotal: 46.056 },
    ],
  },
  {
    orderId: 111704,
    orderDate: '2024-07-03',
    shipDate: '2024-07-10',
    cashierId: 318,
    cashierName: 'johnsmith',
    storeId: 205,
    storeName: 'Phoenix',
    totalAmount: 234.99,
    totalItems: 1,
    items: [
      { itemId: 2700, productId: 201, productName: 'fellowes staxonsteel drawer files', categoryName: 'storage', price: 234.99, discount: 0, quantity: 1, lineTotal: 234.99 },
    ],
  },
  {
    orderId: 111705,
    orderDate: '2024-07-05',
    shipDate: '2024-07-12',
    cashierId: 317,
    cashierName: 'amandafreeman',
    storeId: 203,
    storeName: 'Gilbert',
    totalAmount: 549.98,
    totalItems: 2,
    items: [
      { itemId: 2701, productId: 205, productName: 'hp deskjet printer', categoryName: 'technology', price: 199.99, discount: 0, quantity: 1, lineTotal: 199.99 },
      { itemId: 2702, productId: 215, productName: 'ergonomic office chair', categoryName: 'furniture', price: 349.99, discount: 0, quantity: 1, lineTotal: 349.99 },
    ],
  },
];

// Mock stores, regions, states, cashiers for filters
const mockStores = [
  { storeId: 203, storeName: 'Gilbert' },
  { storeId: 205, storeName: 'Phoenix' },
  { storeId: 207, storeName: 'Tucson' },
];

const mockRegions = [
  { regionId: 1, regionName: 'West' },
  { regionId: 2, regionName: 'East' },
  { regionId: 3, regionName: 'Central' },
];

const mockStates = [
  { stateId: 1, stateName: 'Arizona' },
  { stateId: 2, stateName: 'California' },
  { stateId: 3, stateName: 'Texas' },
];

const mockCashiers = [
  { cashierId: 317, cashierName: 'amandafreeman' },
  { cashierId: 318, cashierName: 'johnsmith' },
  { cashierId: 319, cashierName: 'sarawilliams' },
];

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

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [newItems, setNewItems] = useState<NewOrderItem[]>([{ product: null, quantity: 1 }]);

  // Fetch orders from API - filter by store for ADMIN_STORE
  const { data: orders = [], isLoading: ordersLoading } = useQuery({
    queryKey: ['orders', isStoreAdmin ? user?.storeId : null],
    queryFn: async () => {
      try {
        // For ADMIN_STORE, filter by their store
        const filters = isStoreAdmin && user?.storeId 
          ? { filterStoreId: user.storeId } 
          : {};
        const data = await ordersApi.getAll(filters);
        const orderList = data as Order[];
        // If API returns empty array, use mock data for demo purposes
        if (!orderList || orderList.length === 0) {
          console.warn('API returned empty orders list, using mock data');
          if (isStoreAdmin && user?.storeId) {
            return mockOrders.filter(o => o.storeId === user.storeId);
          }
          return mockOrders;
        }
        return orderList;
      } catch (err) {
        console.warn('Failed to fetch orders from API, using mock data:', err);
        // For mock data, filter by storeId if ADMIN_STORE
        if (isStoreAdmin && user?.storeId) {
          return mockOrders.filter(o => o.storeId === user.storeId);
        }
        return mockOrders;
      }
    },
  });

  // Fetch products for order creation
  const { data: products = mockProducts } = useQuery({
    queryKey: ['products'],
    queryFn: async () => {
      try {
        const data = await productsApi.getAll();
        return data as Product[];
      } catch (err) {
        console.warn('Failed to fetch products from API, using mock data:', err);
        return mockProducts;
      }
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

  const filters: FilterConfig[] = useMemo(() => {
    const baseFilters: FilterConfig[] = [
      { field: 'orderDate', label: 'Order Date Range', type: 'date-range' },
      { field: 'totalAmount', label: 'Total Range', type: 'number-range' },
    ];

    if (isGeneralAdmin) {
      return [
        ...baseFilters,
        {
          field: 'storeId',
          label: 'Store',
          type: 'select',
          options: mockStores.map(s => ({ value: String(s.storeId), label: s.storeName })),
        },
        {
          field: 'regionId',
          label: 'Region',
          type: 'select',
          options: mockRegions.map(r => ({ value: String(r.regionId), label: r.regionName })),
        },
        {
          field: 'stateId',
          label: 'State',
          type: 'select',
          options: mockStates.map(s => ({ value: String(s.stateId), label: s.stateName })),
        },
        {
          field: 'cashierId',
          label: 'Cashier',
          type: 'select',
          options: mockCashiers.map(c => ({ value: String(c.cashierId), label: c.cashierName })),
        },
      ];
    }

    return baseFilters;
  }, [isGeneralAdmin]);

  const searchConfig: SearchConfig = {
    searchableFields: [
      { value: 'orderId', label: 'Order ID' },
      { value: 'storeName', label: 'Store' },
      { value: 'cashierName', label: 'Cashier' },
      { value: 'productName', label: 'Product' },
    ],
  };

  const handleView = (order: Order) => {
    setSelectedOrder(order);
    setIsViewOpen(true);
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

  const columns = [
    { key: 'orderId', header: 'Order ID', sortable: true },
    { key: 'orderDate', header: 'Order Date', sortable: true },
    { key: 'shipDate', header: 'Ship Date', sortable: true },
    ...(isGeneralAdmin ? [
      { key: 'storeName', header: 'Store', sortable: true },
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
          <Button variant="ghost" size="icon">
            <Edit2 className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => handleDelete(order.orderId)}
          >
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="animate-fade-in">
      <div className="page-header flex items-center justify-between">
        <div>
          <h1 className="page-title">Manage Orders</h1>
          <p className="page-description">View, create, and manage customer orders</p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Create Order
        </Button>
      </div>

      <FilterPanel
        filters={filters}
        searchConfig={searchConfig}
        onFilterChange={(f) => console.log('Filters:', f)}
        onSearch={(q, f) => console.log('Search:', q, f)}
      />

      <DataTable columns={columns} data={orders} />

      {/* Create Order Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ShoppingCart className="h-5 w-5" />
              Create New Order
            </DialogTitle>
            <DialogDescription>
              Search and add products to create a new order
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 max-h-[400px] overflow-y-auto">
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
              {filteredProducts.map((product) => (
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
                    <span className="text-muted-foreground ml-2 text-xs">
                      {product.categoryName}
                    </span>
                  </div>
                  <span className="text-primary font-medium">${product.price.toFixed(2)}</span>
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
