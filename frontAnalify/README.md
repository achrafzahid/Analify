# Analify Frontend - React Application

![React](https://img.shields.io/badge/React-18-blue.svg)
![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)
![Vite](https://img.shields.io/badge/Vite-6.0-purple.svg)

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Features & Pages](#features--pages)
- [Components Library](#components-library)
- [State Management](#state-management)
- [API Integration](#api-integration)
- [Authentication & Authorization](#authentication--authorization)
- [Routing](#routing)
- [Styling](#styling)
- [Development](#development)
- [Build & Deployment](#build--deployment)

## 🎯 Overview

The Analify frontend is a modern, responsive React application built with TypeScript and Vite. It provides an intuitive interface for:

- **Multi-store analytics dashboards** with real-time charts
- **Order processing** with multi-item support
- **Product and inventory management**
- **Employee directory and management**
- **Monthly bidding system** for product sections
- **Role-based access control** with personalized experiences

### Key Features

✅ **Modern UI** with shadcn/ui component library  
✅ **Type-safe** with TypeScript  
✅ **Fast development** with Vite HMR  
✅ **Data fetching** with React Query (TanStack Query)  
✅ **Form validation** with React Hook Form + Zod  
✅ **Responsive design** with Tailwind CSS  
✅ **Interactive charts** with Recharts  
✅ **JWT authentication** with automatic token management  

## 🛠 Technology Stack

### Core
- **React** 18.3.1 - UI library
- **TypeScript** 5.6+ - Type safety
- **Vite** 6.0+ - Build tool & dev server

### UI & Styling
- **Tailwind CSS** 3.4.17 - Utility-first CSS
- **shadcn/ui** - High-quality component library (built on Radix UI)
- **Radix UI** - Accessible component primitives
- **Lucide React** - Beautiful icon library
- **class-variance-authority** - Variant management
- **clsx** - Conditional class names

### State & Data
- **TanStack Query** (React Query) 5.83.0 - Server state management
- **React Router** 7.1.3 - Client-side routing
- **React Hook Form** 7.54.3 - Form management
- **Zod** 3.24.1 - Schema validation

### Charts & Visualization
- **Recharts** 2.15.1 - Chart library
- **date-fns** 3.6.0 - Date utilities

### Other
- **cmdk** - Command menu
- **embla-carousel-react** - Carousel component
- **input-otp** - OTP input component
- **next-themes** - Theme management

## 📁 Project Structure

```
frontAnalify/
├── public/                          # Static assets
│   └── robots.txt
│
├── src/
│   ├── components/                  # React components
│   │   ├── charts/                  # Chart components
│   │   │   ├── AreaChartCard.tsx           # Area chart wrapper
│   │   │   ├── BarChartCard.tsx            # Bar chart wrapper
│   │   │   ├── LineChartCard.tsx           # Line chart wrapper
│   │   │   ├── PieChartCard.tsx            # Pie chart wrapper
│   │   │   └── index.ts
│   │   │
│   │   ├── layout/                  # Layout components
│   │   │   ├── DashboardLayout.tsx         # Main dashboard layout
│   │   │   └── Sidebar.tsx                 # Navigation sidebar
│   │   │
│   │   ├── shared/                  # Shared components
│   │   │   ├── ChartPlaceholder.tsx        # Loading placeholder
│   │   │   ├── DataTable.tsx               # Generic data table
│   │   │   ├── FilterPanel.tsx             # Filter UI component
│   │   │   ├── ProfileForm.tsx             # User profile form
│   │   │   └── StatCard.tsx                # KPI card component
│   │   │
│   │   ├── ui/                      # shadcn/ui components (60+ components)
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── form.tsx
│   │   │   ├── input.tsx
│   │   │   ├── select.tsx
│   │   │   ├── table.tsx
│   │   │   └── ... (50+ more)
│   │   │
│   │   └── NavLink.tsx              # Navigation link component
│   │
│   ├── contexts/                    # React Context
│   │   └── AuthContext.tsx                 # Authentication state
│   │
│   ├── hooks/                       # Custom hooks
│   │   ├── use-mobile.tsx                  # Mobile detection
│   │   └── use-toast.ts                    # Toast notifications
│   │
│   ├── lib/                         # Utilities
│   │   └── utils.ts                        # Helper functions
│   │
│   ├── pages/                       # Page components
│   │   ├── dashboard/               # Dashboard pages
│   │   │   ├── BiddingBrowse.tsx           # Browse bidding categories
│   │   │   ├── BiddingCategory.tsx         # Category details
│   │   │   ├── BiddingDashboard.tsx        # Bidding overview
│   │   │   ├── BiddingSection.tsx          # Section details & bidding
│   │   │   ├── Employees.tsx               # Employee management
│   │   │   ├── LowStock.tsx                # Low stock alerts
│   │   │   ├── MyBids.tsx                  # My bids page
│   │   │   ├── Orders.tsx                  # Order management
│   │   │   ├── Products.tsx                # Product management
│   │   │   ├── Profile.tsx                 # User profile
│   │   │   └── Statistics.tsx              # Analytics dashboard
│   │   │
│   │   ├── Index.tsx                # Dashboard home (redirects)
│   │   ├── Landing.tsx              # Landing page
│   │   ├── Login.tsx                # Login page
│   │   └── NotFound.tsx             # 404 page
│   │
│   ├── services/                    # API services
│   │   └── api.ts                          # All API calls
│   │
│   ├── types/                       # TypeScript types
│   │   └── index.ts                        # Type definitions
│   │
│   ├── App.css                      # Global styles
│   ├── App.tsx                      # Root component
│   ├── index.css                    # Tailwind imports
│   ├── main.tsx                     # Entry point
│   └── vite-env.d.ts                # Vite type definitions
│
├── index.html                       # HTML entry point
├── package.json                     # Dependencies
├── tsconfig.json                    # TypeScript config
├── tailwind.config.ts               # Tailwind config
├── vite.config.ts                   # Vite config
├── components.json                  # shadcn/ui config
└── README.md                        # This file
```

## 🚀 Getting Started

### Prerequisites

- **Node.js** 18+ (LTS recommended)
- **npm** 9+ (comes with Node.js)
- **Backend API** running on http://localhost:8081

### Installation

1. **Navigate to frontend directory**:
```bash
cd frontAnalify
```

2. **Install dependencies**:
```bash
npm install
```

3. **Start development server**:
```bash
npm run dev
```

The application will start on **http://localhost:5173**

4. **Open in browser**:
```
http://localhost:5173
```

### Available Scripts

```bash
# Start development server with hot reload
npm run dev

# Build for production
npm run build

# Build for development (no minification)
npm run build:dev

# Preview production build locally
npm run preview

# Run ESLint
npm run lint
```

## 📱 Features & Pages

### Public Pages

#### Landing Page (`/`)
- Project overview
- Feature highlights
- Call-to-action

#### Login Page (`/login`)
- Email/password authentication
- JWT token generation
- Automatic redirect to dashboard

---

### Dashboard Pages (Protected)

#### 🏠 Dashboard Home (`/dashboard`)
- Redirects to role-appropriate page
- Quick navigation

#### 📊 Statistics (`/dashboard/statistics`)
**Access**: All roles (filtered by role)

**Features**:
- Key performance indicators (KPIs)
  - Total revenue
  - Total orders
  - Average order value
  - Low stock count
- Interactive charts:
  - Revenue over time (area chart)
  - Sales by region (bar chart)
  - Category distribution (pie chart)
  - Top products/stores/investors
- **Role-based filters**:
  - **ADMIN_GENERAL**: Date, region, state, store
  - **INVESTOR**: Date, region, state
  - **ADMIN_STORE**: Date only

**Components Used**:
- `StatCard` - KPI displays
- `AreaChartCard`, `BarChartCard`, `PieChartCard` - Charts
- `FilterPanel` - Dynamic filtering

---

#### 📦 Orders (`/dashboard/orders`)
**Access**: CAISSIER, ADMIN_STORE, ADMIN_GENERAL

**Features**:
- **List all orders** with filters (date, store, product)
- **View order details** (items, pricing, cashier)
- **Create new order** (CAISSIER only)
  - Multi-item support
  - Discount per item
  - Real-time total calculation
- **Edit order** (ADMIN_STORE, ADMIN_GENERAL)
  - Update ship date
- **Delete order** (CAISSIER, ADMIN_GENERAL)
- **Cashier visibility** (ADMIN_STORE, ADMIN_GENERAL see who processed order)

**Role Permissions**:
- `CAISSIER`: Create, view own orders, delete own orders
- `ADMIN_STORE`: View store orders, edit ship dates, see cashier names
- `ADMIN_GENERAL`: Full access to all orders

---

#### 🏪 Products (`/dashboard/products`)
**Access**: INVESTOR, ADMIN_GENERAL

**Features**:
- Product catalog with search and filters
  - Filter by category, subcategory
  - Price range filter
  - Quantity range filter
- Create new products
- Edit product details (name, price)
- Delete products
- Update stock levels (per store)
- Low stock indicators

**API Calls**:
- `productsApi.getAll(filters)`
- `productsApi.create(data)`
- `productsApi.update(id, data)`
- `productsApi.updateStock(id, data)`
- `productsApi.delete(id)`

---

#### 📉 Low Stock Alerts (`/dashboard/low-stock`)
**Access**: INVESTOR, ADMIN_GENERAL

**Features**:
- Real-time low stock alerts across all stores
- Filter by store city
- Filter by quantity thresholds
- Product details with current stock levels
- Store location information

**Data Source**: `productsApi.getLowStockAlerts()`

---

#### 👥 Employees (`/dashboard/employees`)
**Access**: ADMIN_STORE (own store), ADMIN_GENERAL (all)

**Features**:
- **Employee directory** with search and filters
  - Search by username, email, user ID
  - Filter by role, salary range, start date
- **Create employees**
  - Assign role (CAISSIER, ADMIN_STORE, INVESTOR, ADMIN_GENERAL)
  - Assign to store (for CAISSIER, ADMIN_STORE)
  - Set salary and start date
- **Edit employee profiles**
- **Delete employees**
- **Role assignment** (promote/demote)

**Role-Based Data**:
- `ADMIN_STORE`: Only employees from their store
- `ADMIN_GENERAL`: All employees (including other admins, investors)

---

#### 💰 Bidding System

##### Bidding Overview (`/dashboard/bidding-overview`)
**Access**: INVESTOR, ADMIN_GENERAL

**Features**:
- Monthly period information
- Quick stats:
  - Total bids placed
  - Currently winning bids
  - Possessed sections
  - Days until period closes
- Current winning bids preview
- Period details (start/end dates, bidding window)
- Quick action buttons

---

##### Browse Sections (`/dashboard/bidding`)
**Access**: INVESTOR, ADMIN_GENERAL

**Features**:
- Browse bidding hierarchy:
  - Categories
  - Rangs (by category)
  - Faces (by rang)
  - Sections (by face)
- Section cards with:
  - Current price
  - Status (OPEN/CLOSED)
  - Current winner
  - Bid button

---

##### Section Details (`/dashboard/bidding/section/:id`)
**Access**: INVESTOR, ADMIN_GENERAL

**Features**:
- Full section information
- Current price and base price
- Status and deadline
- Place bid form
  - Validates bid > current price
  - Real-time feedback
- Bid history table
  - All bids on this section
  - Bid amounts, dates, statuses
  - Current winner highlighted
- Monthly contract description

---

##### My Bids (`/dashboard/my-bids`)
**Access**: INVESTOR, ADMIN_GENERAL

**Features**:
- **Three tabs**:
  1. **All Bids** - Every bid you've placed
  2. **Currently Winning** - Bids with PENDING status
  3. **Final Wins** - Bids with WINNER status (won sections)
- Bid cards showing:
  - Section name
  - Bid amount
  - Bid time
  - Status badge (PENDING, OUTBID, WINNER)
- **Cancel bid** functionality (for OUTBID bids)
- View section details from bid card

---

#### 👤 Profile (`/dashboard/profile`)
**Access**: All roles

**Features**:
- View personal information
- Edit profile details (username, email, date of birth)
- Change password
- View role and permissions

**Form Validation**: React Hook Form + Zod schema

---

## 🎨 Components Library

### Chart Components

All chart components use **Recharts** library with consistent styling:

#### `AreaChartCard`
```tsx
<AreaChartCard
  title="Revenue Over Time"
  data={revenueData}
  dataKey="value"
  xAxisKey="date"
/>
```

#### `BarChartCard`
```tsx
<BarChartCard
  title="Sales by Region"
  data={salesData}
  dataKey="value"
  xAxisKey="name"
  color="#8884d8"
/>
```

#### `LineChartCard`
```tsx
<LineChartCard
  title="Monthly Trends"
  data={trendsData}
  dataKey="value"
  xAxisKey="month"
/>
```

#### `PieChartCard`
```tsx
<PieChartCard
  title="Category Distribution"
  data={categoryData}
  dataKey="value"
  nameKey="name"
/>
```

---

### Layout Components

#### `DashboardLayout`
Main layout wrapper with sidebar navigation:
```tsx
<DashboardLayout>
  <YourPageContent />
</DashboardLayout>
```

#### `Sidebar`
Navigation menu with role-based menu items:
- Automatic role detection
- Active state management
- Responsive design (collapsible on mobile)

---

### Shared Components

#### `StatCard`
KPI display card:
```tsx
<StatCard
  title="Total Revenue"
  value="$156,789"
  icon={DollarSign}
  description="+12.5% from last month"
/>
```

#### `DataTable`
Generic table component with sorting and pagination:
```tsx
<DataTable
  columns={columns}
  data={data}
  searchPlaceholder="Search..."
/>
```

#### `FilterPanel`
Dynamic filter UI:
```tsx
<FilterPanel
  filters={filterConfig}
  onFilterChange={handleFilterChange}
  onSearch={handleSearch}
  searchConfig={searchConfig}
/>
```

**Filter Types**:
- `select` - Dropdown selection
- `number-range` - Min/max inputs
- `date-range` - From/to date pickers

---

### UI Components (shadcn/ui)

Over **60 pre-built components** from shadcn/ui:

**Forms**:
- `Button`, `Input`, `Select`, `Checkbox`, `Radio`, `Switch`
- `Form`, `Label`, `Textarea`

**Data Display**:
- `Table`, `Card`, `Badge`, `Avatar`, `Separator`

**Overlays**:
- `Dialog`, `Sheet`, `Popover`, `Tooltip`, `Dropdown Menu`
- `Alert Dialog`, `Command`, `Context Menu`

**Navigation**:
- `Tabs`, `Accordion`, `Navigation Menu`, `Breadcrumb`

**Feedback**:
- `Toast`, `Alert`, `Progress`, `Skeleton`

**Charts**:
- `Chart` (Recharts wrapper with shadcn styling)

All components are:
- ✅ Fully accessible (ARIA compliant)
- ✅ Keyboard navigable
- ✅ Customizable with Tailwind
- ✅ TypeScript typed

---

## 🔄 State Management

### React Query (TanStack Query)

All server state is managed with **React Query**:

**Benefits**:
- Automatic caching
- Background refetching
- Optimistic updates
- Loading and error states
- Mutations with callbacks

**Example Usage**:
```tsx
// Fetch data
const { data, isLoading, error } = useQuery({
  queryKey: ['orders', filters],
  queryFn: () => ordersApi.getAll(filters),
});

// Mutate data
const mutation = useMutation({
  mutationFn: ordersApi.create,
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['orders'] });
    toast({ title: 'Order created successfully' });
  },
});
```

### Context API

#### `AuthContext`
Manages authentication state:
- Current user information
- JWT token
- Login/logout functions
- Auto-initialization from localStorage

**Usage**:
```tsx
const { user, token, login, logout } = useAuth();

if (user?.role === 'ADMIN_GENERAL') {
  // Show admin features
}
```

---

## 🔌 API Integration

### API Service Layer

All API calls are centralized in `src/services/api.ts`:

**Structure**:
```typescript
// Base configuration
const API_BASE_URL = '/api';

// Generic request function
const apiRequest = async <T>(
  url: string,
  method: 'GET' | 'POST' | 'PUT' | 'DELETE',
  body?: unknown
): Promise<T> => {
  const token = localStorage.getItem('auth_token');
  
  const response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }
  
  return response.json();
};

// API modules
export const ordersApi = { ... };
export const productsApi = { ... };
export const employeesApi = { ... };
export const biddingApi = { ... };
```

**Error Handling**:
- 401 Unauthorized → Redirect to login
- 403 Forbidden → Show error message
- 404 Not Found → Handle gracefully
- 500 Server Error → Show error toast

---

## 🔐 Authentication & Authorization

### JWT Flow

```
1. User enters credentials → Login page
2. POST /api/auth/login
3. Server returns JWT token + user info
4. Frontend stores token in localStorage
5. AuthContext initializes with user data
6. All API requests include: Authorization: Bearer {token}
7. Protected routes check authentication
8. Pages check user role for feature access
```

### Protected Routes

**Implementation** in `App.tsx`:
```tsx
<Route path="/dashboard" element={
  <ProtectedRoute>
    <DashboardLayout />
  </ProtectedRoute>
}>
  <Route path="orders" element={<Orders />} />
  ...
</Route>
```

### Role-Based Access

**Access Control in Components**:
```tsx
const Orders = () => {
  const { user } = useAuth();
  
  const canCreate = user?.role === 'CAISSIER';
  const canEdit = ['ADMIN_STORE', 'ADMIN_GENERAL'].includes(user?.role);
  
  return (
    <>
      {canCreate && <Button onClick={createOrder}>Create</Button>}
      {canEdit && <Button onClick={editOrder}>Edit</Button>}
    </>
  );
};
```

---

## 🛣 Routing

### Route Structure

```
/                          → Landing page
/login                     → Login page
/dashboard                 → Dashboard home (redirects)
/dashboard/statistics      → Analytics dashboard
/dashboard/orders          → Order management
/dashboard/products        → Product management
/dashboard/low-stock       → Low stock alerts
/dashboard/employees       → Employee management
/dashboard/bidding-overview      → Bidding dashboard
/dashboard/bidding         → Browse sections
/dashboard/bidding/section/:id   → Section details
/dashboard/my-bids         → My bids
/dashboard/profile         → User profile
*                          → 404 Not Found
```

**Navigation**: React Router v7 with nested routes

---

## 🎨 Styling

### Tailwind CSS

Utility-first CSS framework with custom configuration:

**Config** (`tailwind.config.ts`):
```typescript
export default {
  darkMode: ["class"],
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        primary: "hsl(var(--primary))",
        // ... custom color palette
      },
    },
  },
};
```

**Usage**:
```tsx
<div className="flex items-center gap-4 p-6 rounded-lg bg-card">
  <h2 className="text-2xl font-bold">Title</h2>
</div>
```

### CSS Variables

Custom properties in `index.css`:
```css
:root {
  --background: 0 0% 100%;
  --foreground: 222.2 84% 4.9%;
  --primary: 222.2 47.4% 11.2%;
  --radius: 0.5rem;
  /* ... */
}
```

### Dark Mode Support

Theme toggle with `next-themes`:
```tsx
import { useTheme } from 'next-themes';

const { theme, setTheme } = useTheme();
```

---

## 🔧 Development

### Hot Module Replacement (HMR)

Vite provides instant updates during development:
- Save file → Changes appear immediately
- React Fast Refresh preserves component state
- TypeScript errors shown in browser overlay

### TypeScript

**Type Safety**:
- All components are typed
- API responses have interfaces
- Props are strictly typed
- Type inference reduces boilerplate

**Example**:
```typescript
interface OrderProps {
  orderId: number;
  onDelete: (id: number) => void;
}

const OrderCard: React.FC<OrderProps> = ({ orderId, onDelete }) => {
  // TypeScript knows types
};
```

### ESLint

Code quality checks:
```bash
npm run lint
```

**Rules**: React, TypeScript, and accessibility best practices

---

## 📦 Build & Deployment

### Production Build

```bash
npm run build
```

Output: `dist/` folder with optimized assets

**Optimizations**:
- Code splitting
- Tree shaking
- Minification
- Asset compression

### Preview Build

```bash
npm run preview
```

Test production build locally on http://localhost:4173

### Environment Variables

Create `.env` file:
```env
VITE_API_BASE_URL=http://localhost:8081/api
```

Access in code:
```typescript
const API_URL = import.meta.env.VITE_API_BASE_URL || '/api';
```

### Deployment Options

#### Static Hosting (Recommended)
- **Vercel**, **Netlify**, **GitHub Pages**
- Simply upload `dist/` folder

#### Docker
Create `Dockerfile`:
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 5173
CMD ["npm", "run", "preview"]
```

#### Nginx
Serve `dist/` folder:
```nginx
server {
  listen 80;
  root /var/www/analify/dist;
  
  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

---

## 🐛 Troubleshooting

**Node modules errors**:
```bash
rm -rf node_modules package-lock.json
npm install
```

**Build fails**:
```bash
npm run build -- --debug
```

**TypeScript errors**:
```bash
npx tsc --noEmit
```

**Port already in use**:
```bash
# Change port in vite.config.ts
server: { port: 5174 }
```

---

## 📞 Support & Documentation

- **Main README**: [`../README.md`](../README.md)
- **Backend README**: [`../backAnalify/README.md`](../backAnalify/README.md)
- **Bidding Docs**: [`../BIDDING_SYSTEM_COMPLETE_FEATURES.md`](../BIDDING_SYSTEM_COMPLETE_FEATURES.md)

---

**Built with React 18, TypeScript, and Vite**
- Edit files directly within the Codespace and commit and push your changes once you're done.

## What technologies are used for this project?

This project is built with:

- Vite
- TypeScript
- React
- shadcn-ui
- Tailwind CSS

## How can I deploy this project?

Simply open [Lovable](https://lovable.dev/projects/REPLACE_WITH_PROJECT_ID) and click on Share -> Publish.

## Can I connect a custom domain to my Lovable project?

Yes, you can!

To connect a domain, navigate to Project > Settings > Domains and click Connect Domain.

Read more here: [Setting up a custom domain](https://docs.lovable.dev/features/custom-domain#custom-domain)
