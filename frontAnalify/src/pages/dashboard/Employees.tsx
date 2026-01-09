import React, { useState } from 'react';
import { Plus, Edit2, Trash2, Loader2 } from 'lucide-react';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { DataTable } from '@/components/shared/DataTable';
import { FilterPanel } from '@/components/shared/FilterPanel';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';
import { Employee, UserRole, FilterConfig, SearchConfig, EmployeeCreate, EmployeeUpdate } from '@/types';
import { employeesApi } from '@/services/api';

const filters: FilterConfig[] = [
  {
    field: 'role',
    label: 'Role',
    type: 'select',
    options: [
      { value: 'CAISSIER', label: 'Cashier' },
      { value: 'ADMIN_STORE', label: 'Store Admin' },
      { value: 'INVESTOR', label: 'Investor' },
      { value: 'ADMIN_GENERAL', label: 'General Admin' },
    ],
  },
  { field: 'salary', label: 'Salary Range', type: 'number-range' },
  { field: 'dateStarted', label: 'Start Date', type: 'date-range' },
];

const searchConfig: SearchConfig = {
  searchableFields: [
    { value: 'userName', label: 'Username' },
    { value: 'mail', label: 'Email' },
    { value: 'userId', label: 'User ID' },
  ],
};

const roleLabels: Record<UserRole, string> = {
  CAISSIER: 'Cashier',
  ADMIN_STORE: 'Store Admin',
  INVESTOR: 'Investor',
  ADMIN_GENERAL: 'General Admin',
  ADMIN_G: 'General Admin',
};

// Define EmployeeForm component outside to prevent re-creation on every render
const EmployeeFormComponent: React.FC<{
  formData: Partial<EmployeeCreate & EmployeeUpdate>;
  setFormData: React.Dispatch<React.SetStateAction<Partial<EmployeeCreate & EmployeeUpdate>>>;
  isCreate: boolean;
  isGeneralAdmin: boolean;
}> = ({ formData, setFormData, isCreate, isGeneralAdmin }) => (
  <div className="space-y-4">
    <div className="grid grid-cols-2 gap-4">
      <div className="space-y-2">
        <Label>Username</Label>
        <Input
          value={formData.userName || ''}
          onChange={(e) => setFormData(prev => ({ ...prev, userName: e.target.value }))}
        />
      </div>
      <div className="space-y-2">
        <Label>Email</Label>
        <Input
          type="email"
          value={formData.mail || ''}
          onChange={(e) => setFormData(prev => ({ ...prev, mail: e.target.value }))}
        />
      </div>
    </div>

    {/* Password field - required for new employees, optional for updates */}
    <div className="space-y-2">
      <Label>{isCreate ? 'Password' : 'New Password (leave empty to keep current)'}</Label>
      <Input
        type="password"
        placeholder={isCreate ? 'Enter password' : 'Leave empty to keep current password'}
        value={formData.password || ''}
        onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
      />
    </div>

    <div className="grid grid-cols-2 gap-4">
      <div className="space-y-2">
        <Label>Date of Birth</Label>
        <Input
          type="date"
          value={formData.dateOfBirth || ''}
          onChange={(e) => setFormData(prev => ({ ...prev, dateOfBirth: e.target.value }))}
        />
      </div>
      <div className="space-y-2">
        <Label>Salary</Label>
        <Input
          type="number"
          value={formData.salary || ''}
          onChange={(e) =>
            setFormData(prev => ({ ...prev, salary: parseFloat(e.target.value) || 0 }))
          }
        />
      </div>
    </div>

    <div className="grid grid-cols-2 gap-4">
      <div className="space-y-2">
        <Label>Role</Label>
        <Select
          value={formData.role}
          onValueChange={(value) => setFormData(prev => ({ ...prev, role: value as UserRole }))}
          disabled={!isGeneralAdmin && !isCreate}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select role" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="CAISSIER">Cashier</SelectItem>
            <SelectItem value="ADMIN_STORE">Store Admin</SelectItem>
            {isGeneralAdmin && (
              <>
                <SelectItem value="INVESTOR">Investor</SelectItem>
                <SelectItem value="ADMIN_GENERAL">General Admin</SelectItem>
              </>
            )}
          </SelectContent>
        </Select>
      </div>
      {isGeneralAdmin && (
        <div className="space-y-2">
          <Label>Store ID</Label>
          <Input
            type="number"
            value={formData.storeId || ''}
            onChange={(e) =>
              setFormData(prev => ({ ...prev, storeId: parseInt(e.target.value) || undefined }))
            }
          />
        </div>
      )}
    </div>
  </div>
);

const Employees: React.FC = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(null);
  const [formData, setFormData] = useState<Partial<EmployeeCreate & EmployeeUpdate>>({});
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('');

  const isGeneralAdmin = user?.role === 'ADMIN_GENERAL' || user?.role === 'ADMIN_G';
  const isStoreAdmin = user?.role === 'ADMIN_STORE';
  const hasAccess = isGeneralAdmin || isStoreAdmin;

  // Check access
  if (!hasAccess) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Access Denied: You don't have permission to view employees</p>
      </div>
    );
  }

  // Fetch all employees from API
  const { data: allEmployees = [], isLoading, error } = useQuery({
    queryKey: ['employees', isStoreAdmin ? user?.storeId : 'all'],
    queryFn: async () => {
      // If ADMIN_STORE, use getStoreEmployees to get only their store's employees
      if (isStoreAdmin && user?.storeId) {
        const data = await employeesApi.getStoreEmployees(user.storeId);
        return data as Employee[];
      }
      // Otherwise get all employees (for ADMIN_GENERAL/ADMIN_G)
      const data = await employeesApi.getAll();
      return data as Employee[];
    },
  });

  // Apply client-side filtering
  const employees = allEmployees.filter(emp => {
    // Apply role filter
    if (activeFilters.role && emp.role !== activeFilters.role) return false;
    
    // Apply salary range filter (FilterPanel sends salary_min and salary_max)
    if (activeFilters.salary_min && emp.salary != null && emp.salary < Number(activeFilters.salary_min)) return false;
    if (activeFilters.salary_max && emp.salary != null && emp.salary > Number(activeFilters.salary_max)) return false;
    
    // Apply date range filter (FilterPanel sends dateStarted_from and dateStarted_to)
    if (activeFilters.dateStarted_from && emp.dateStarted < activeFilters.dateStarted_from) return false;
    if (activeFilters.dateStarted_to && emp.dateStarted > activeFilters.dateStarted_to) return false;
    
    // Apply search filter
    if (searchQuery && searchField) {
      const value = (emp as any)[searchField];
      if (!value || !String(value).toLowerCase().includes(searchQuery.toLowerCase())) return false;
    }
    
    return true;
  });

  // Create employee mutation
  const createMutation = useMutation({
    mutationFn: (data: EmployeeCreate) => employeesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      toast({
        title: 'Employee Created',
        description: 'New employee has been added successfully.',
      });
      setIsCreateOpen(false);
      setFormData({});
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to create employee',
        variant: 'destructive',
      });
    },
  });

  // Update employee mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: EmployeeUpdate }) => 
      employeesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      toast({
        title: 'Employee Updated',
        description: 'Employee information has been updated.',
      });
      setIsEditOpen(false);
      setSelectedEmployee(null);
      setFormData({});
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to update employee',
        variant: 'destructive',
      });
    },
  });

  // Delete employee mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => employeesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      toast({
        title: 'Employee Deleted',
        description: 'Employee has been removed.',
      });
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to delete employee',
        variant: 'destructive',
      });
    },
  });

  const handleCreate = () => {
    setFormData({});
    setIsCreateOpen(true);
  };

  const handleEdit = (employee: Employee) => {
    setSelectedEmployee(employee);
    setFormData(employee);
    setIsEditOpen(true);
  };

  const handleDelete = (userId: number) => {
    if (confirm('Are you sure you want to delete this employee?')) {
      deleteMutation.mutate(userId);
    }
  };

  const handleSave = () => {
    if (selectedEmployee) {
      // Update existing employee
      const updateData: EmployeeUpdate = {
        userName: formData.userName,
        mail: formData.mail,
        password: formData.password,
        dateOfBirth: formData.dateOfBirth,
        salary: formData.salary,
        storeId: formData.storeId,
      };
      updateMutation.mutate({ id: selectedEmployee.userId, data: updateData });
    } else {
      // Create new employee
      const createData: EmployeeCreate = {
        userName: formData.userName || '',
        mail: formData.mail || '',
        password: formData.password || '',
        dateOfBirth: formData.dateOfBirth || '',
        role: (formData.role as UserRole) || 'CAISSIER',
        storeId: formData.storeId,
        salary: formData.salary,
        dateStarted: formData.dateStarted || new Date().toISOString().split('T')[0],
      };
      createMutation.mutate(createData);
    }
  };

  const columns = [
    { key: 'userId', header: 'ID', sortable: true },
    { key: 'userName', header: 'Username', sortable: true },
    { key: 'mail', header: 'Email', sortable: true },
    {
      key: 'role',
      header: 'Role',
      render: (emp: Employee) => (
        <Badge variant="secondary">{roleLabels[emp.role]}</Badge>
      ),
    },
    {
      key: 'salary',
      header: 'Salary',
      sortable: true,
      render: (emp: Employee) => emp.salary ? `$${emp.salary.toLocaleString()}` : 'N/A',
    },
    { key: 'dateStarted', header: 'Start Date', sortable: true },
    {
      key: 'actions',
      header: 'Actions',
      render: (emp: Employee) => (
        <div className="flex gap-2">
          <Button variant="ghost" size="icon" onClick={() => handleEdit(emp)}>
            <Edit2 className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => handleDelete(emp.userId)}
          >
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2">Loading employees...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-destructive">Failed to load employees: {(error as Error).message}</p>
        <Button onClick={() => queryClient.invalidateQueries({ queryKey: ['employees'] })}>
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div className="page-header flex items-center justify-between">
        <div>
          <h1 className="page-title">
            {isGeneralAdmin ? 'Manage Employees (Global)' : 'Manage Employees'}
          </h1>
          <p className="page-description">
            {isGeneralAdmin
              ? 'Manage all employees across all stores'
              : 'Manage employees in your store'}
          </p>
        </div>
        <Button onClick={handleCreate}>
          <Plus className="h-4 w-4 mr-2" />
          Add Employee
        </Button>
      </div>

      <FilterPanel
        filters={filters}
        searchConfig={searchConfig}
        onFilterChange={(f) => setActiveFilters(f)}
        onSearch={(q, f) => { setSearchQuery(q); setSearchField(f); }}
      />

      <DataTable columns={columns} data={employees} />

      {/* Create Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add New Employee</DialogTitle>
            <DialogDescription>
              Enter the details for the new employee
            </DialogDescription>
          </DialogHeader>
          <EmployeeFormComponent 
            formData={formData}
            setFormData={setFormData}
            isCreate={true}
            isGeneralAdmin={isGeneralAdmin}
          />
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSave} disabled={createMutation.isPending}>
              {createMutation.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Creating...
                </>
              ) : (
                'Create Employee'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Employee</DialogTitle>
            <DialogDescription>Update employee information</DialogDescription>
          </DialogHeader>
          <div className="space-y-2 mb-4">
            <Label className="text-muted-foreground">Employee ID</Label>
            <Input value={selectedEmployee?.userId} disabled className="bg-muted" />
          </div>
          <EmployeeFormComponent 
            formData={formData}
            setFormData={setFormData}
            isCreate={false}
            isGeneralAdmin={isGeneralAdmin}
          />
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
    </div>
  );
};

export default Employees;
