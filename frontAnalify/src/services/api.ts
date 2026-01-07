// API Service Layer
// Connected to Spring Boot backend at /api

const API_BASE_URL = '/api';

const API_URLS = {
  // Auth
  LOGIN: `${API_BASE_URL}/auth/login`,
  
  // User Profile (using employees endpoint with current user id)
  GET_USER_PROFILE: `${API_BASE_URL}/employees`,
  UPDATE_USER_PROFILE: `${API_BASE_URL}/employees`,
  
  // Orders
  GET_ORDERS: `${API_BASE_URL}/orders`,
  GET_ORDER: `${API_BASE_URL}/orders`,
  CREATE_ORDER: `${API_BASE_URL}/orders`,
  UPDATE_ORDER_SHIP_DATE: `${API_BASE_URL}/orders`,
  DELETE_ORDER: `${API_BASE_URL}/orders`,
  
  // Products
  GET_PRODUCTS: `${API_BASE_URL}/products`,
  GET_PRODUCT: `${API_BASE_URL}/products`,
  CREATE_PRODUCT: `${API_BASE_URL}/products`,
  UPDATE_PRODUCT: `${API_BASE_URL}/products`,
  DELETE_PRODUCT: `${API_BASE_URL}/products`,
  UPDATE_STOCK: `${API_BASE_URL}/products`,
  SEARCH_PRODUCTS: `${API_BASE_URL}/products/search`,
  LOW_STOCK_ALERTS: `${API_BASE_URL}/products/alerts/low-stock`,
  
  // Employees
  GET_EMPLOYEES: `${API_BASE_URL}/employees/getall`,
  GET_EMPLOYEE: `${API_BASE_URL}/employees`,
  GET_STORE_EMPLOYEES: `${API_BASE_URL}/employees/store`,
  CREATE_EMPLOYEE: `${API_BASE_URL}/employees/add`,
  UPDATE_EMPLOYEE: `${API_BASE_URL}/employees`,
  DELETE_EMPLOYEE: `${API_BASE_URL}/employees`,
  ASSIGN_ROLE: `${API_BASE_URL}/employees`,
  
  // Statistics/Analytics
  GET_DASHBOARD_STATS: `${API_BASE_URL}/analytics/dashboard`,
  GET_ENHANCED_DASHBOARD: `${API_BASE_URL}/analytics/dashboard/enhanced`,
  GET_PREDICTIONS: `${API_BASE_URL}/analytics/predictions`,
  DEEP_SEARCH: `${API_BASE_URL}/analytics/deep-search`,
  
  // Bidding System
  // Navigation
  GET_CATEGORIES: `${API_BASE_URL}/bidding/categories`,
  GET_CATEGORY: `${API_BASE_URL}/bidding/categories`,
  GET_RANGS_BY_CATEGORY: `${API_BASE_URL}/bidding/categories`,
  GET_FACES_BY_RANG: `${API_BASE_URL}/bidding/rangs`,
  GET_SECTIONS_BY_FACE: `${API_BASE_URL}/bidding/faces`,
  GET_SECTION: `${API_BASE_URL}/bidding/sections`,
  
  // Bidding Operations
  PLACE_BID: `${API_BASE_URL}/bidding/bids`,
  CANCEL_BID: `${API_BASE_URL}/bidding/bids`,
  
  // My Bids
  GET_MY_BIDS: `${API_BASE_URL}/bidding/my-bids`,
  GET_MY_CURRENT_WINNING_BIDS: `${API_BASE_URL}/bidding/my-current-winning-bids`,
  GET_MY_WINNING_BIDS: `${API_BASE_URL}/bidding/my-winning-bids`,
  GET_MY_POSSESSIONS: `${API_BASE_URL}/bidding/my-possessions`,
  
  // Investor Bids (Admin)
  GET_INVESTOR_BIDS: `${API_BASE_URL}/bidding/investors`,
  GET_INVESTOR_CURRENT_WINNING_BIDS: `${API_BASE_URL}/bidding/investors`,
  GET_INVESTOR_WINNING_BIDS: `${API_BASE_URL}/bidding/investors`,
  
  // Section Information
  GET_BID_HISTORY: `${API_BASE_URL}/bidding/sections`,
  GET_CURRENT_WINNER: `${API_BASE_URL}/bidding/sections`,
  
  // All Bids (Admin)
  GET_ALL_BIDS: `${API_BASE_URL}/bidding/bids`,
  GET_BIDS_BETWEEN: `${API_BASE_URL}/bidding/bids/between`,
  
  // Section Management (Admin)
  CLOSE_SECTION: `${API_BASE_URL}/bidding/sections`,
  
  // Season/Period Info
  GET_SEASON_INFO: `${API_BASE_URL}/bidding/season/current`,
};

// Get auth token from localStorage
const getAuthToken = (): string | null => {
  return localStorage.getItem('auth_token');
};

// Generic API request function
const apiRequest = async <T>(
  url: string,
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' = 'GET',
  body?: unknown,
  skipAuth: boolean = false
): Promise<T> => {
  const token = getAuthToken();
  
  if (!token && !skipAuth) {
    throw new Error('No authentication token found');
  }

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config: RequestInit = {
    method,
    headers,
  };

  if (body) {
    config.body = JSON.stringify(body);
  }

  const response = await fetch(url, config);

  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
      throw new Error('Unauthorized');
    }
    if (response.status === 403) {
      throw new Error('Forbidden: You do not have access to this resource');
    }
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || `API Error: ${response.status}`);
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
};

// Helper to build query string from filters
const buildQueryString = (params: Record<string, unknown> | object): string => {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.append(key, String(value));
    }
  });
  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : '';
};

// Auth API
export const authApi = {
  login: (email: string, password: string) =>
    apiRequest<{ token: string; user: unknown }>(
      API_URLS.LOGIN,
      'POST',
      { email, password },
      true // Skip auth for login
    ),
};

// User/Profile API
export const userApi = {
  getProfile: (userId: number) =>
    apiRequest(`${API_URLS.GET_USER_PROFILE}/${userId}`),
  updateProfile: (userId: number, data: unknown) =>
    apiRequest(`${API_URLS.UPDATE_USER_PROFILE}/${userId}`, 'PUT', data),
};

// Orders API
export interface OrderFilters {
  filterStoreId?: number;
  filterRegionId?: number;
  filterStateId?: number;
  filterCaissierId?: number;
  filterProductId?: number;
}

export interface CreateOrderRequest {
  cashierId: number;
  items: Array<{
    productId: number;
    quantity: number;
    discount?: number;
  }>;
}

export const ordersApi = {
  getAll: (filters?: OrderFilters) =>
    apiRequest(`${API_URLS.GET_ORDERS}${buildQueryString(filters || {})}`),
  getOne: (id: number) =>
    apiRequest(`${API_URLS.GET_ORDER}/${id}`),
  create: (data: CreateOrderRequest) =>
    apiRequest(API_URLS.CREATE_ORDER, 'POST', data),
  updateShipDate: (id: number, shipDate: string) =>
    apiRequest(`${API_URLS.UPDATE_ORDER_SHIP_DATE}/${id}/ship-date?shipDate=${shipDate}`, 'PATCH'),
  delete: (id: number) =>
    apiRequest(`${API_URLS.DELETE_ORDER}/${id}`, 'DELETE'),
  getTotal: (id: number) =>
    apiRequest<number>(`${API_URLS.GET_ORDER}/${id}/total`),
};

// Products API
export interface ProductFilters {
  filterStoreId?: number;
  filterStateId?: number;
  filterRegionId?: number;
}

export interface CreateProductRequest {
  productName: string;
  price: number;
  subcategoryId: number;
  investorId: number;
}

export interface UpdateProductRequest {
  productName?: string;
  price?: number;
}

export interface UpdateStockRequest {
  storeId: number;
  quantity: number;
}

export const productsApi = {
  getAll: (filters?: ProductFilters) =>
    apiRequest(`${API_URLS.GET_PRODUCTS}${buildQueryString(filters || {})}`),
  getOne: (id: number) =>
    apiRequest(`${API_URLS.GET_PRODUCT}/${id}`),
  create: (data: CreateProductRequest) =>
    apiRequest(API_URLS.CREATE_PRODUCT, 'POST', data),
  update: (id: number, data: UpdateProductRequest) =>
    apiRequest(`${API_URLS.UPDATE_PRODUCT}/${id}`, 'PUT', data),
  delete: (id: number) =>
    apiRequest(`${API_URLS.DELETE_PRODUCT}/${id}`, 'DELETE'),
  updateStock: (productId: number, data: UpdateStockRequest) =>
    apiRequest(`${API_URLS.UPDATE_STOCK}/${productId}/stock`, 'PUT', data),
  search: (query: string) =>
    apiRequest(`${API_URLS.SEARCH_PRODUCTS}?query=${encodeURIComponent(query)}`),
  getLowStockAlerts: () =>
    apiRequest(API_URLS.LOW_STOCK_ALERTS),
};

// Employees API
export interface EmployeeCreateDTO {
  userName: string;
  mail: string;
  password: string;
  dateOfBirth: string;
  role: string;
  storeId?: number;
  salary?: number;
  dateStarted?: string;
}

export interface EmployeeUpdateDTO {
  userName?: string;
  mail?: string;
  password?: string;
  dateOfBirth?: string;
  salary?: number;
  storeId?: number;
}

export const employeesApi = {
  getAll: () =>
    apiRequest(API_URLS.GET_EMPLOYEES),
  getOne: (id: number) =>
    apiRequest(`${API_URLS.GET_EMPLOYEE}/${id}`),
  getStoreEmployees: (storeId: number) =>
    apiRequest(`${API_URLS.GET_STORE_EMPLOYEES}/${storeId}`),
  create: (data: EmployeeCreateDTO) =>
    apiRequest(API_URLS.CREATE_EMPLOYEE, 'POST', data),
  update: (id: number, data: EmployeeUpdateDTO) =>
    apiRequest(`${API_URLS.UPDATE_EMPLOYEE}/${id}`, 'PUT', data),
  delete: (id: number) =>
    apiRequest(`${API_URLS.DELETE_EMPLOYEE}/${id}`, 'DELETE'),
  assignRole: (id: number, newRole: string, storeId?: number) =>
    apiRequest(
      `${API_URLS.ASSIGN_ROLE}/${id}/assign-role?newRole=${newRole}${storeId ? `&storeId=${storeId}` : ''}`,
      'PUT'
    ),
};

// Statistics/Analytics API
export interface StatisticsFilters {
  startDate?: string;
  endDate?: string;
  storeId?: number;
  investorId?: number;
  productId?: number;
}

export interface DashboardStats {
  totalRevenue: number;
  totalStockValue: number;
  totalOrders: number;
  totalProductsSold: number;
  averageOrderValue: number;
  lowStockCount: number;
  revenueOverTime: Array<{ date: string; value: number }>;
  ordersByDayOfWeek: Record<string, number>;
  ordersByMonth: Record<string, number>;
  categoryRevenueDistribution: Record<string, number>;
  categoryProductCount: Record<string, number>;
  salesByRegion: Record<string, number>;
  salesByState: Record<string, number>;
  topProducts: Array<{ name: string; value: number }>;
  topStores: Array<{ name: string; value: number }>;
  topInvestors: Array<{ name: string; value: number }>;
}

export const statsApi = {
  getDashboard: (filters?: StatisticsFilters) =>
    apiRequest<DashboardStats>(`${API_URLS.GET_DASHBOARD_STATS}${buildQueryString(filters || {})}`),
  getEnhancedDashboard: (filters?: StatisticsFilters) =>
    apiRequest(`${API_URLS.GET_ENHANCED_DASHBOARD}${buildQueryString(filters || {})}`),
  getPredictions: (metric: string, filters?: StatisticsFilters) =>
    apiRequest(`${API_URLS.GET_PREDICTIONS}${buildQueryString({ metric, ...filters })}`),
  deepSearch: (query: string) =>
    apiRequest(API_URLS.DEEP_SEARCH, 'POST', { query }),
};

// Bidding API
export const biddingApi = {
  // Navigation
  getCategories: () =>
    apiRequest(API_URLS.GET_CATEGORIES),
  getCategory: (id: number) =>
    apiRequest(`${API_URLS.GET_CATEGORY}/${id}`),
  getRangsByCategory: (categoryId: number) =>
    apiRequest(`${API_URLS.GET_RANGS_BY_CATEGORY}/${categoryId}/rangs`),
  getFacesByRang: (rangId: number) =>
    apiRequest(`${API_URLS.GET_FACES_BY_RANG}/${rangId}/faces`),
  getSectionsByFace: (faceId: number) =>
    apiRequest(`${API_URLS.GET_SECTIONS_BY_FACE}/${faceId}/sections`),
  getSection: (sectionId: number) =>
    apiRequest(`${API_URLS.GET_SECTION}/${sectionId}`),
  
  // Bidding Operations
  placeBid: (data: { sectionId: number; amount: number }) =>
    apiRequest(API_URLS.PLACE_BID, 'POST', data),
  cancelBid: (bidId: number) =>
    apiRequest(`${API_URLS.CANCEL_BID}/${bidId}`, 'DELETE'),
  
  // My Bids
  getMyBids: () =>
    apiRequest(API_URLS.GET_MY_BIDS),
  getMyCurrentWinningBids: () =>
    apiRequest(API_URLS.GET_MY_CURRENT_WINNING_BIDS),
  getMyWinningBids: () =>
    apiRequest(API_URLS.GET_MY_WINNING_BIDS),
  getMyPossessions: () =>
    apiRequest(API_URLS.GET_MY_POSSESSIONS),
  
  // Other Investors' Bids (Admin)
  getInvestorBids: (investorId: number) =>
    apiRequest(`${API_URLS.GET_INVESTOR_BIDS}/${investorId}/bids`),
  getInvestorCurrentWinningBids: (investorId: number) =>
    apiRequest(`${API_URLS.GET_INVESTOR_CURRENT_WINNING_BIDS}/${investorId}/current-winning-bids`),
  getInvestorWinningBids: (investorId: number) =>
    apiRequest(`${API_URLS.GET_INVESTOR_WINNING_BIDS}/${investorId}/winning-bids`),
  
  // Section Info
  getBidHistory: (sectionId: number) =>
    apiRequest(`${API_URLS.GET_BID_HISTORY}/${sectionId}/bids`),
  getCurrentWinner: (sectionId: number) =>
    apiRequest(`${API_URLS.GET_CURRENT_WINNER}/${sectionId}/winner`),
  
  // All Bids (Admin only)
  getAllBids: () =>
    apiRequest(API_URLS.GET_ALL_BIDS),
  getBidsBetweenDates: (startDate: string, endDate: string) =>
    apiRequest(`${API_URLS.GET_BIDS_BETWEEN}?startDate=${startDate}&endDate=${endDate}`),
  
  // Section Management (Admin)
  closeSection: (sectionId: number) =>
    apiRequest(`${API_URLS.CLOSE_SECTION}/${sectionId}/close`, 'POST'),
  
  // Season Info
  getSeasonInfo: () =>
    apiRequest(API_URLS.GET_SEASON_INFO),
};

export { API_URLS, API_BASE_URL };
