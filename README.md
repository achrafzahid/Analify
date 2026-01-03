# Analify - Multi-Store Analytics & Bidding Platform

![Project Status](https://img.shields.io/badge/status-active-success.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [User Roles & Permissions](#user-roles--permissions)
- [Getting Started](#getting-started)
- [System Architecture](#system-architecture)
- [API Documentation](#api-documentation)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

**Analify** is a comprehensive enterprise-level platform designed for multi-store retail management with integrated analytics and a unique **monthly bidding system** for product sections. The system provides role-based access control, real-time analytics, inventory management, order processing, and an innovative bidding mechanism for investors to acquire product display sections.

### Business Context

The platform serves a retail chain with multiple stores across different regions and states. It enables:
- **Store Managers** to manage their store operations and employees
- **Cashiers** to process customer orders
- **Investors** to bid on product display sections and manage inventory
- **General Admins** to oversee the entire operation with comprehensive analytics

## ✨ Key Features

### 📊 Analytics & Reporting
- Real-time dashboard with key performance indicators (KPIs)
- Revenue tracking and trend analysis
- Sales distribution by region, state, and store
- Product performance analytics
- Predictive analytics for future trends
- Customizable filters based on user role

### 🏪 Multi-Store Management
- Hierarchical organization (Region → State → Store)
- Store-specific employee management
- Cross-store inventory visibility
- Low stock alerts with store-level tracking

### 📦 Order Processing
- Complete order lifecycle management
- Multi-item orders with discount support
- Role-based order creation and editing
- Order tracking with ship date management
- Historical order analytics

### 👥 Employee Management
- Role-based access control
- Employee profile management
- Salary tracking
- Store assignment and transfer
- Comprehensive employee directory (Admin only)

### 💰 Monthly Bidding System
The platform features a unique **monthly bidding cycle** for product display sections:

#### How It Works:
1. **Monthly Cycle**: Each calendar month is a new bidding period
2. **Price Increases**: On the 1st of every month, all section prices increase by 2%
3. **Bidding Window**: Investors can place bids throughout the entire month
4. **Section Hierarchy**: Categories → Rangs → Faces → Sections
5. **Winning Bids**: Highest bid at month-end wins the section

#### Bidding Features:
- Browse available sections by category, rang, and face
- Place competitive bids on open sections
- Track current winning status in real-time
- View bid history and competing bids
- Manage won sections and possessions
- Monthly period information dashboard

#### Bid Statuses:
- **PENDING**: Currently the highest bid (winning)
- **OUTBID**: Another investor bid higher
- **WINNER**: Won the bid after section closed

### 📱 Product Management
- Product catalog with categories and subcategories
- Multi-store inventory tracking
- Stock level management
- Price updates
- Product search and filtering
- Low stock alerts

## 🛠 Technology Stack

### Frontend
- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite
- **UI Library**: shadcn/ui (Radix UI components)
- **Styling**: Tailwind CSS
- **State Management**: React Query (TanStack Query v5)
- **Routing**: React Router v6
- **Form Handling**: React Hook Form + Zod validation
- **Charts**: Recharts
- **Icons**: Lucide React

### Backend
- **Framework**: Spring Boot 3.4.13
- **Language**: Java 21
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT
- **Validation**: Jakarta Validation
- **Mapping**: MapStruct
- **Password Encryption**: BCrypt

### Development Tools
- **API Testing**: Postman/Insomnia recommended
- **Database Tool**: pgAdmin 4 or DBeaver
- **Version Control**: Git

## 📁 Project Structure

```
analifyProject/
├── frontAnalify/          # React + TypeScript Frontend
│   ├── src/
│   │   ├── components/    # Reusable UI components
│   │   ├── pages/         # Page components
│   │   ├── services/      # API service layer
│   │   ├── contexts/      # React Context providers
│   │   ├── hooks/         # Custom React hooks
│   │   ├── lib/           # Utility functions
│   │   └── types/         # TypeScript type definitions
│   └── README.md          # Frontend documentation
│
├── backAnalify/           # Spring Boot Backend
│   └── src/
│       ├── main/
│       │   ├── java/com/analyfy/analify/
│       │   │   ├── Controller/     # REST API endpoints
│       │   │   ├── Service/        # Business logic
│       │   │   ├── Repository/     # Database access
│       │   │   ├── Entity/         # JPA entities
│       │   │   ├── DTO/            # Data transfer objects
│       │   │   ├── Mapper/         # Entity-DTO mappers
│       │   │   ├── Security/       # JWT & auth
│       │   │   ├── Enum/           # Enumerations
│       │   │   └── Exception/      # Custom exceptions
│       │   └── resources/
│       │       └── application.properties
│       └── README.md      # Backend documentation
│
├── BIDDING_SYSTEM_COMPLETE_FEATURES.md    # Bidding system docs
└── README.md              # This file
```

## 👤 User Roles & Permissions

### 1. CAISSIER (Cashier)
**Permissions:**
- ✅ Create and delete orders
- ✅ View personal profile
- ❌ Cannot access employee management
- ❌ Cannot access analytics
- ❌ Cannot manage products

**Use Case:** Point-of-sale employees processing customer transactions

---

### 2. ADMIN_STORE (Store Manager)
**Permissions:**
- ✅ View and edit orders from their store
- ✅ View and manage employees in their store
- ✅ View store-specific analytics (filtered by store)
- ✅ See which cashier validated each order
- ❌ Cannot create new orders (only cashiers can)
- ❌ Cannot access other stores' data

**Use Case:** Store managers overseeing daily operations

---

### 3. INVESTOR
**Permissions:**
- ✅ Full bidding system access
  - Browse all sections (categories, rangs, faces)
  - Place and cancel bids
  - View bid history and current winning status
  - Manage won sections/possessions
- ✅ Manage products (view, create, edit, delete)
- ✅ View low stock alerts across all stores
- ✅ View analytics (filtered by their products)
- ❌ Cannot access employee management
- ❌ Cannot process orders

**Use Case:** Product suppliers bidding on display sections

---

### 4. ADMIN_GENERAL / ADMIN_G (General Administrator)
**Permissions:**
- ✅ Full access to all features
- ✅ View all employees (including other admins and investors)
- ✅ Manage all orders across all stores
- ✅ View comprehensive analytics (region, state, store filters)
- ✅ Access all bidding system data
- ✅ Manage all products
- ✅ System-wide oversight

**Use Case:** C-level executives and system administrators

---

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:
- **Node.js** (v18 or higher) and npm
- **Java Development Kit** (JDK 21)
- **Maven** (3.6 or higher)
- **PostgreSQL** (14 or higher)
- **Git**

### Database Setup

1. **Install PostgreSQL** (if not already installed)

2. **Create Database**:
```sql
CREATE DATABASE analify;
```

3. **Configure Database Connection**:
   - Navigate to `backAnalify/src/main/resources/application.properties`
   - Update the following properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/analify
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

### Backend Setup

1. **Navigate to backend directory**:
```bash
cd backAnalify
```

2. **Build the project**:
```bash
./mvnw clean install
```
*On Windows, use `mvnw.cmd` instead of `./mvnw`*

3. **Run the application**:
```bash
./mvnw spring-boot:run
```

The backend will start on **http://localhost:8081**

4. **Verify backend is running**:
   - Open browser: http://localhost:8081/api/bidding/categories
   - You should see a JSON response

### Frontend Setup

1. **Open a new terminal and navigate to frontend directory**:
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

The frontend will start on **http://localhost:5173**

4. **Access the application**:
   - Open browser: http://localhost:5173
   - You'll be redirected to the login page

### Default Login Credentials

*Note: Ask your database administrator for test credentials, or create users via the employee management system*

Example credentials structure:
```
Email: user@example.com
Password: [your-password]
```

## 🏗 System Architecture

### Frontend Architecture

```
┌─────────────────────────────────────────┐
│          React Application              │
│  ┌───────────────────────────────────┐  │
│  │   Components (shadcn/ui)          │  │
│  │   - Charts, Tables, Forms, etc.   │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │   Pages (Routes)                  │  │
│  │   - Dashboard, Orders, Products   │  │
│  │   - Bidding, Analytics, etc.      │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │   Services (API Layer)            │  │
│  │   - HTTP requests via fetch       │  │
│  │   - JWT token management          │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │   State Management                │  │
│  │   - React Query (caching)         │  │
│  │   - Context API (auth)            │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Backend Architecture

```
┌─────────────────────────────────────────────┐
│          Spring Boot Application            │
│  ┌───────────────────────────────────────┐  │
│  │   REST Controllers                    │  │
│  │   - @RestController endpoints         │  │
│  │   - Request/Response DTOs             │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │   Service Layer                       │  │
│  │   - Business logic                    │  │
│  │   - Transaction management            │  │
│  │   - Role-based authorization          │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │   Repository Layer                    │  │
│  │   - JPA Repositories                  │  │
│  │   - Custom queries                    │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │   Database (PostgreSQL)               │  │
│  │   - JPA Entity mapping                │  │
│  │   - Hibernate ORM                     │  │
│  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────┐  │
│  │   Security Layer                      │  │
│  │   - JWT authentication                │  │
│  │   - Request interceptors              │  │
│  │   - Password encryption               │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Data Flow

```
User Action (Frontend)
    ↓
API Service Layer (TypeScript)
    ↓
HTTP Request + JWT Token
    ↓
Spring Security Filter (JWT Validation)
    ↓
REST Controller (@GetMapping, @PostMapping, etc.)
    ↓
Service Layer (Business Logic + Authorization)
    ↓
Repository Layer (JPA/Hibernate)
    ↓
PostgreSQL Database
    ↓
Response DTO
    ↓
React Query Cache
    ↓
UI Component Render
```

## 📚 API Documentation

### Base URL
```
http://localhost:8081/api
```

### Authentication
All API requests (except `/auth/login`) require JWT authentication:
```
Authorization: Bearer <jwt_token>
```

### Main Endpoints

#### Authentication
- `POST /auth/login` - User login (returns JWT token)

#### Orders
- `GET /orders` - Get all orders (with optional filters)
- `GET /orders/{id}` - Get specific order
- `POST /orders` - Create new order
- `PATCH /orders/{id}/ship-date` - Update ship date
- `DELETE /orders/{id}` - Delete order

#### Products
- `GET /products` - Get all products (with filters)
- `GET /products/{id}` - Get specific product
- `POST /products` - Create new product
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product
- `PATCH /products/{id}/stock` - Update stock quantity
- `GET /products/alerts/low-stock` - Get low stock alerts

#### Employees
- `GET /employees/getall` - Get all employees (Admin only)
- `GET /employees/{id}` - Get employee profile
- `GET /employees/store/{storeId}` - Get store employees
- `POST /employees/add` - Create employee
- `PUT /employees/{id}` - Update employee
- `DELETE /employees/{id}` - Delete employee
- `PUT /employees/{id}/assign-role` - Assign role to employee

#### Analytics
- `GET /analytics/dashboard` - Get dashboard statistics
- `GET /analytics/predictions` - Get predictive analytics
- `POST /analytics/deep-search` - Advanced search

#### Bidding System
- `GET /bidding/categories` - Get all categories
- `GET /bidding/categories/{id}/rangs` - Get rangs by category
- `GET /bidding/rangs/{id}/faces` - Get faces by rang
- `GET /bidding/faces/{id}/sections` - Get sections by face
- `GET /bidding/sections/{id}` - Get section details
- `POST /bidding/bids` - Place a bid
- `DELETE /bidding/bids/{id}` - Cancel a bid
- `GET /bidding/my-bids` - Get my bids
- `GET /bidding/my-current-winning-bids` - Get bids I'm winning
- `GET /bidding/my-winning-bids` - Get bids I won
- `GET /bidding/my-possessions` - Get sections I possess
- `GET /bidding/season/current` - Get current period info

For complete API documentation, see:
- [`backAnalify/README.md`](backAnalify/README.md) - Detailed backend API docs
- [`BIDDING_SYSTEM_COMPLETE_FEATURES.md`](BIDDING_SYSTEM_COMPLETE_FEATURES.md) - Bidding system specification

## 🎨 Screenshots

### Dashboard Analytics
*Role-based dashboard showing KPIs, charts, and trends*

### Bidding System
*Browse sections, place bids, track winning status*

### Order Management
*Create, edit, and track orders with multi-item support*

### Employee Management
*Comprehensive employee directory with role assignment*

## 🔧 Configuration

### Environment Variables

#### Frontend (Optional)
Create `.env` file in `frontAnalify/`:
```env
VITE_API_BASE_URL=http://localhost:8081/api
```

#### Backend
Edit `backAnalify/src/main/resources/application.properties`:
```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/analify
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT (configure in JwtService.java)
jwt.secret=your-secret-key
jwt.expiration=86400000
```

## 📝 Development Guidelines

### Code Style
- **Frontend**: ESLint configuration with TypeScript rules
- **Backend**: Java coding conventions with Lombok for boilerplate reduction

### Branching Strategy
- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

### Commit Messages
Follow conventional commits:
```
feat: Add new bidding dashboard
fix: Resolve order date validation issue
docs: Update API documentation
refactor: Improve employee service logic
```

## 🧪 Testing

### Frontend Testing
```bash
cd frontAnalify
npm run lint        # Run linter
npm run build       # Test production build
```

### Backend Testing
```bash
cd backAnalify
./mvnw test         # Run unit tests
./mvnw verify       # Run integration tests
```

## 📦 Production Build

### Frontend
```bash
cd frontAnalify
npm run build       # Creates dist/ folder
```

### Backend
```bash
cd backAnalify
./mvnw clean package    # Creates target/*.jar file
java -jar target/analify-0.0.1-SNAPSHOT.jar
```

## 🐛 Troubleshooting

### Common Issues

**Backend won't start:**
- Verify PostgreSQL is running
- Check database credentials in `application.properties`
- Ensure port 8081 is not in use

**Frontend can't connect to backend:**
- Verify backend is running on http://localhost:8081
- Check browser console for CORS errors
- Verify JWT token is being sent with requests

**Database errors:**
- Run `spring.jpa.hibernate.ddl-auto=create` once to recreate schema
- Check PostgreSQL logs for connection issues

**Authentication issues:**
- Clear browser localStorage: `localStorage.clear()`
- Verify JWT token is valid and not expired
- Check user exists in database

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

**Development Team**
- Full Stack Development
- UI/UX Design
- Database Architecture
- Business Logic Implementation

## 📞 Contact & Support

For questions, issues, or support:
- **Issues**: Open an issue on GitHub
- **Documentation**: See individual README files in `frontAnalify/` and `backAnalify/`
- **API Docs**: See `BIDDING_SYSTEM_COMPLETE_FEATURES.md`

## 🎯 Future Enhancements

- [ ] Email notifications for bid status changes
- [ ] Mobile-responsive progressive web app (PWA)
- [ ] Advanced analytics with AI predictions
- [ ] Export functionality for reports
- [ ] Real-time notifications with WebSocket
- [ ] Multi-language support (i18n)
- [ ] Automated testing suite
- [ ] Docker containerization
- [ ] CI/CD pipeline setup

## 📊 Project Status

- ✅ Core functionality complete
- ✅ Role-based access control implemented
- ✅ Monthly bidding system operational
- ✅ Analytics dashboard functional
- ✅ Multi-store management working
- 🔄 Ongoing refinements and optimizations

---

**Built with ❤️ using React, Spring Boot, and PostgreSQL**
