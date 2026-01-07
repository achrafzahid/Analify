import React, { useState, useMemo } from 'react';
import { Edit2, Package, RefreshCw, Loader2 } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
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
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';
import { Product, FilterConfig, SearchConfig } from '@/types';
import { productsApi, UpdateProductRequest, UpdateStockRequest } from '@/services/api';

const MAX_STOCK = 500;

const searchConfig: SearchConfig = {
  searchableFields: [
    { value: 'productName', label: 'Product Name' },
    { value: 'productId', label: 'Product ID' },
    { value: 'categoryName', label: 'Category' },
  ],
};

const Products: React.FC = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isStockDialogOpen, setIsStockDialogOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState<Partial<Product>>({});
  const [stockQuantity, setStockQuantity] = useState<number>(0);
  const [suggestedStock, setSuggestedStock] = useState<number>(0);
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
        <p className="text-destructive">Access Denied: You don't have permission to view products</p>
      </div>
    );
  }

  // Fetch all products from API
  const { data: allProducts = [], isLoading, error } = useQuery({
    queryKey: ['products'],
    queryFn: async () => {
      const data = await productsApi.getAll();
      return data as Product[];
    },
  });

  // Extract unique categories and subcategories from data
  const { uniqueCategories, uniqueSubcategories } = useMemo(() => {
    const categories = new Set<string>();
    const subcategories = new Set<string>();
    
    allProducts.forEach(product => {
      if (product.categoryName) {
        categories.add(product.categoryName);
      }
      if (product.subName) {
        subcategories.add(product.subName);
      }
    });
    
    return {
      uniqueCategories: Array.from(categories).sort(),
      uniqueSubcategories: Array.from(subcategories).sort(),
    };
  }, [allProducts]);

  // Generate dynamic filters based on actual data
  const filters: FilterConfig[] = useMemo(() => [
    {
      field: 'categoryName',
      label: 'Category',
      type: 'select',
      options: uniqueCategories.map(cat => ({
        value: cat.toLowerCase(),
        label: cat.charAt(0).toUpperCase() + cat.slice(1),
      })),
    },
    {
      field: 'subName',
      label: 'Sub-category',
      type: 'select',
      options: uniqueSubcategories.map(sub => ({
        value: sub.toLowerCase(),
        label: sub.charAt(0).toUpperCase() + sub.slice(1),
      })),
    },
    { field: 'price', label: 'Price Range', type: 'number-range' },
    { field: 'quantity', label: 'Quantity Range', type: 'number-range' },
  ], [uniqueCategories, uniqueSubcategories]);

  // Apply client-side filtering
  const products = allProducts.filter(product => {
    // Apply category filter
    if (activeFilters.categoryName && product.categoryName.toLowerCase() !== activeFilters.categoryName.toLowerCase()) return false;
    
    // Apply subcategory filter
    if (activeFilters.subName && product.subName.toLowerCase() !== activeFilters.subName.toLowerCase()) return false;
    
    // Apply price range filter (FilterPanel sends price_min and price_max)
    if (activeFilters.price_min && product.price < Number(activeFilters.price_min)) return false;
    if (activeFilters.price_max && product.price > Number(activeFilters.price_max)) return false;
    
    // Apply quantity range filter (FilterPanel sends quantity_min and quantity_max)
    if (activeFilters.quantity_min && product.quantity < Number(activeFilters.quantity_min)) return false;
    if (activeFilters.quantity_max && product.quantity > Number(activeFilters.quantity_max)) return false;
    
    // Apply search filter
    if (searchQuery && searchField) {
      const value = (product as any)[searchField];
      if (!value || !String(value).toLowerCase().includes(searchQuery.toLowerCase())) return false;
    }
    
    return true;
  });

  // Update product mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateProductRequest }) => 
      productsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      toast({
        title: 'Product Updated',
        description: 'Product information has been updated.',
      });
      setIsEditOpen(false);
      setSelectedProduct(null);
      setFormData({});
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to update product',
        variant: 'destructive',
      });
    },
  });

  // Update stock mutation
  const updateStockMutation = useMutation({
    mutationFn: ({ productId, data }: { productId: number; data: UpdateStockRequest }) =>
      productsApi.updateStock(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      toast({
        title: 'Stock Updated',
        description: 'Product stock has been updated.',
      });
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to update stock',
        variant: 'destructive',
      });
    },
  });

  const handleEdit = (product: Product) => {
    setSelectedProduct(product);
    setFormData(product);
    setIsEditOpen(true);
  };

  const handleFillStock = async (product: Product) => {
    if (!user?.storeId) {
      toast({
        title: 'Error',
        description: 'No store ID available. Please log in again.',
        variant: 'destructive',
      });
      return;
    }
    
    setSelectedProduct(product);
    setIsStockDialogOpen(true);
    
    // Fetch suggested stock quantity
    try {
      const suggested = await productsApi.getSuggestedStock(product.productId, user.storeId);
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
    if (!user?.storeId || !selectedProduct) return;
    
    updateStockMutation.mutate({
      productId: selectedProduct.productId,
      data: {
        storeId: user.storeId,
        quantity: stockQuantity,
      },
    }, {
      onSuccess: () => {
        setIsStockDialogOpen(false);
        setSelectedProduct(null);
      },
    });
  };

  const handleSave = () => {
    if (selectedProduct) {
      const updateData: UpdateProductRequest = {
        productName: formData.productName,
        price: formData.price,
      };
      updateMutation.mutate({ id: selectedProduct.productId, data: updateData });
    }
  };

  const getStockBadge = (quantity: number) => {
    if (quantity < 50) {
      return <Badge variant="destructive">Low Stock</Badge>;
    } else if (quantity < 100) {
      return <Badge variant="secondary">Medium</Badge>;
    }
    return <Badge variant="default">In Stock</Badge>;
  };

  const columns = [
    { key: 'productId', header: 'ID', sortable: true },
    { key: 'productName', header: 'Product Name', sortable: true },
    {
      key: 'price',
      header: 'Price',
      sortable: true,
      render: (product: Product) => `$${product.price.toFixed(2)}`,
    },
    { key: 'categoryName', header: 'Category' },
    { key: 'subName', header: 'Sub-category' },
    {
      key: 'quantity',
      header: 'Quantity',
      sortable: true,
      render: (product: Product) => (
        <div className="flex items-center gap-2">
          <span>{product.quantity}</span>
          {getStockBadge(product.quantity)}
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (product: Product) => (
        <div className="flex gap-2">
          <Button variant="ghost" size="icon" onClick={() => handleEdit(product)}>
            <Edit2 className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => handleFillStock(product)}
            disabled={updateStockMutation.isPending}
            title="Refill Stock"
          >
            <RefreshCw className={`h-4 w-4 ${updateStockMutation.isPending ? 'animate-spin' : ''}`} />
          </Button>
        </div>
      ),
    },
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2">Loading products...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Failed to load products: {(error as Error).message}</p>
        <Button onClick={() => queryClient.invalidateQueries({ queryKey: ['products'] })}>
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h1 className="page-title">My Products</h1>
        <p className="page-description">Manage your product inventory</p>
      </div>

      <FilterPanel
        filters={filters}
        searchConfig={searchConfig}
        onFilterChange={(f) => setActiveFilters(f)}
        onSearch={(q, f) => { setSearchQuery(q); setSearchField(f); }}
      />

      <DataTable columns={columns} data={products} />

      {/* Edit Dialog */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Package className="h-5 w-5" />
              Edit Product
            </DialogTitle>
            <DialogDescription>Update product information</DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-muted-foreground">Product ID</Label>
              <Input value={selectedProduct?.productId} disabled className="bg-muted" />
            </div>

            <div className="space-y-2">
              <Label>Product Name</Label>
              <Input
                value={formData.productName || ''}
                onChange={(e) =>
                  setFormData({ ...formData, productName: e.target.value })
                }
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Price</Label>
                <Input
                  type="number"
                  step={0.01}
                  value={formData.price || ''}
                  onChange={(e) =>
                    setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })
                  }
                />
              </div>
              <div className="space-y-2">
                <Label>Quantity</Label>
                <Input
                  type="number"
                  value={formData.quantity || ''}
                  onChange={(e) =>
                    setFormData({ ...formData, quantity: parseInt(e.target.value) || 0 })
                  }
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label className="text-muted-foreground">Category</Label>
                <Input value={formData.categoryName} disabled className="bg-muted" />
              </div>
              <div className="space-y-2">
                <Label className="text-muted-foreground">Sub-category</Label>
                <Input value={formData.subName} disabled className="bg-muted" />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSave} disabled={updateMutation.isPending}>
              {updateMutation.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Saving...
                </>
              ) : (
                'Save Changes'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Stock Refill Dialog */}
      <Dialog open={isStockDialogOpen} onOpenChange={setIsStockDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <RefreshCw className="h-5 w-5" />
              Refill Stock
            </DialogTitle>
            <DialogDescription>
              Add stock for {selectedProduct?.productName}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-muted-foreground">Current Stock</Label>
              <Input 
                value={selectedProduct?.quantity || 0} 
                disabled 
                className="bg-muted" 
              />
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
                {(selectedProduct?.quantity || 0) + stockQuantity} units
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => {
                setIsStockDialogOpen(false);
                setSelectedProduct(null);
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

export default Products;
