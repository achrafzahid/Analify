// User Types
// ADMIN_G is backend value, ADMIN_GENERAL is frontend display value
export type UserRole = 'CAISSIER' | 'ADMIN_STORE' | 'INVESTOR' | 'ADMIN_GENERAL' | 'ADMIN_G';

export interface User {
  userId: number;
  userName: string;
  mail: string;
  dateOfBirth: string;
  role: UserRole;
  storeId?: number;
  salary?: number;
  dateStarted?: string;
}

export interface JWTPayload {
  userId: number;
  role: UserRole;
  exp: number;
  iat: number;
}

// Order Item Types (matches OrderItemDTO from backend)
export interface OrderItem {
  itemId: number;
  productId: number;
  productName: string;
  categoryName: string;
  price: number;
  discount: number;
  quantity: number;
  lineTotal: number;
}

// Order Types (matches OrderDTO from backend)
export interface Order {
  orderId: number;
  orderDate: string;
  shipDate: string;
  cashierId: number;
  cashierName: string;
  storeId: number;
  storeName: string;
  totalAmount: number;
  totalItems: number;
  items: OrderItem[];
}

// Product Types (matches ProductDTO from backend)
export interface Product {
  productId: number;
  productName: string;
  price: number;
  categoryName: string;
  subId: number;
  subName: string;
  investorId: number;
  investorName: string;
  quantity: number;
}

// Low Stock Item (matches LowStockAlertDTO from backend)
export interface LowStockItem {
  storeId: number;
  storeCity: string;
  productName: string;
  quantity: number;
}

// Employee Types (matches EmployeeResponseDTO from backend)
export interface Employee {
  userId: number;
  userName: string;
  mail: string;
  dateOfBirth: string;
  role: UserRole;
  storeId?: number;
  salary?: number; // Optional as some employees may not have salary set
  dateStarted: string;
}

// Employee Create (matches EmployeeCreateDTO from backend)
export interface EmployeeCreate {
  userName: string;
  mail: string;
  password: string;
  dateOfBirth: string;
  role: UserRole;
  storeId?: number;
  salary?: number;
  dateStarted?: string;
}

// Employee Update (matches EmployeeUpdateDTO from backend)
export interface EmployeeUpdate {
  userName?: string;
  mail?: string;
  password?: string;
  dateOfBirth?: string;
  salary?: number;
  storeId?: number;
}

// Store Types (matches StoreDTO from backend)
export interface Store {
  storeId: number;
  cityId: number;
  cityName: string;
  regionName: string;
  managerId: number;
  managerName: string;
  employeeCount: number;
}

// Region Types (matches RegionDTO from backend)
export interface Region {
  regionId: number;
  name: string;
}

// State Types (matches StateDTO from backend)
export interface State {
  stateId: number;
  name: string;
  regionId: number;
  regionName: string;
}

// Cashier Types
export interface Cashier {
  cashierId: number;
  cashierName: string;
}

// Inventory DTO (matches InventoryDTO from backend)
export interface Inventory {
  inventoryId?: number;
  storeId: number;
  productId: number;
  quantity: number;
}

// Statistics Types (matches DashboardStatsDTO from backend)
export interface TimeSeriesPoint {
  date: string;
  value: number;
}

export interface RankingItem {
  name: string;
  value: number;
  additionalInfo?: string;
}

export interface DashboardStats {
  totalRevenue: number;
  totalStockValue: number;
  totalOrders: number;
  totalProductsSold: number;
  averageOrderValue: number;
  lowStockCount: number;
  revenueOverTime: TimeSeriesPoint[];
  ordersByDayOfWeek: Record<string, number>;
  ordersByMonth: Record<string, number>;
  categoryRevenueDistribution: Record<string, number>;
  categoryProductCount: Record<string, number>;
  salesByRegion: Record<string, number>;
  salesByState: Record<string, number>;
  topProducts: RankingItem[];
  topStores: RankingItem[];
  topInvestors: RankingItem[];
}

// Filter Types
export interface FilterConfig {
  field: string;
  label: string;
  type: 'text' | 'select' | 'date' | 'number-range' | 'date-range';
  options?: { value: string; label: string }[];
}

export interface SearchConfig {
  searchableFields: { value: string; label: string }[];
}

// Pagination Types
export interface PaginationState {
  page: number;
  pageSize: number;
  total: number;
}

// Bidding System Types
export interface CategoryDTO {
  categoryId: number;
  categoryName: string;
}

export interface RangDTO {
  rangId: number;
  rangName: string;
  description?: string;
  categoryId: number;
  categoryName: string;
}

export interface FaceDTO {
  faceId: number;
  faceName: string;
  description?: string;
  rangId: number;
  rangName: string;
}

export interface SectionDTO {
  sectionId: number;
  sectionName: string;
  basePrice: number;
  currentPrice: number;
  status: string;
  dateDelai: string;
  description?: string;
  faceId: number;
  faceName: string;
  winnerInvestorId?: number;
  winnerInvestorName?: string;
}

export interface BidDTO {
  bidId: number;
  amount: number;
  bidTime: string;
  status: 'PENDING' | 'OUTBID' | 'WINNER';
  sectionId: number;
  sectionName: string;
  investorId: number;
  investorName: string;
}

export interface CreateBidRequest {
  sectionId: number;
  amount: number;
}

export interface BidHistoryDTO {
  bidId: number;
  amount: number;
  bidTime: string;
  investorName: string;
  status: string;
  isWinner: boolean;
}

export interface SeasonConfigDTO {
  currentMonth: number;
  currentPeriod: number; // 1-12 (month of the year)
  periodStartDate: string;
  periodEndDate: string;
  biddingOpenDate: string;
  biddingCloseDate: string;
  isBiddingOpen: boolean;
  daysUntilClose: number;
}
