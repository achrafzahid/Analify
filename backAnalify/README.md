# Analify Backend - Spring Boot REST API

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Database Schema](#database-schema)
- [API Documentation](#api-documentation)
- [Security & Authentication](#security--authentication)
- [Business Logic](#business-logic)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [Testing](#testing)

## 🎯 Overview

The Analify backend is a robust Spring Boot REST API that powers a multi-store retail analytics and bidding platform. It provides secure, role-based access to:

- **Multi-store order processing**
- **Product and inventory management**
- **Employee management with hierarchical access control**
- **Real-time analytics and reporting**
- **Monthly bidding system for product sections**

### Key Capabilities

✅ **RESTful API** with comprehensive endpoints  
✅ **JWT-based authentication** with role-based authorization  
✅ **PostgreSQL database** with JPA/Hibernate ORM  
✅ **Automated monthly bidding cycles** with scheduled tasks  
✅ **Complex business logic** with transaction management  
✅ **DTO pattern** for clean API contracts  
✅ **MapStruct** for efficient entity-DTO mapping  

## 🛠 Technology Stack

### Core Framework
- **Spring Boot** 3.4.13
- **Java** 21
- **Maven** 3.6+

### Spring Modules
- **Spring Web** - REST API controllers
- **Spring Data JPA** - Database access layer
- **Spring Security** - Authentication & authorization
- **Spring Validation** - Request validation

### Database
- **PostgreSQL** 14+ - Primary database
- **Hibernate** - ORM implementation
- **HikariCP** - Connection pooling (default)

### Security & Authentication
- **Spring Security** - Security framework
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing

### Developer Tools
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Entity-DTO mapping
- **Spring Boot DevTools** - Hot reload during development

## 📁 Project Structure

```
backAnalify/
├── src/
│   ├── main/
│   │   ├── java/com/analyfy/analify/
│   │   │   ├── Controller/              # REST API Endpoints
│   │   │   │   ├── AuthController.java           # Login/Authentication
│   │   │   │   ├── OrderController.java          # Order CRUD operations
│   │   │   │   ├── ProductController.java        # Product management
│   │   │   │   ├── EmployeeController.java       # Employee management
│   │   │   │   ├── AnalyticsController.java      # Dashboard analytics
│   │   │   │   └── BiddingController.java        # Bidding system
│   │   │   │
│   │   │   ├── Service/                 # Business Logic Layer
│   │   │   │   ├── AuthService.java              # Authentication logic
│   │   │   │   ├── OrderService.java             # Order processing
│   │   │   │   ├── ProductService.java           # Product operations
│   │   │   │   ├── EmployeeService.java          # Employee operations
│   │   │   │   ├── AnalyticsService.java         # Analytics calculation
│   │   │   │   └── BiddingService.java           # Bidding logic
│   │   │   │
│   │   │   ├── Repository/              # Database Access Layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── StoreRepository.java
│   │   │   │   ├── BidRepository.java
│   │   │   │   └── SectionRepository.java
│   │   │   │
│   │   │   ├── Entity/                  # JPA Entities (Database Models)
│   │   │   │   ├── User.java                     # Abstract user class
│   │   │   │   ├── Caissier.java                 # Cashier entity
│   │   │   │   ├── AdminStore.java               # Store admin entity
│   │   │   │   ├── AdminG.java                   # General admin entity
│   │   │   │   ├── Investor.java                 # Investor entity
│   │   │   │   ├── Order.java                    # Order entity
│   │   │   │   ├── Product.java                  # Product entity
│   │   │   │   ├── Store.java                    # Store entity
│   │   │   │   ├── Bid.java                      # Bid entity
│   │   │   │   ├── Section.java                  # Section entity
│   │   │   │   └── ... (30+ entities)
│   │   │   │
│   │   │   ├── DTO/                     # Data Transfer Objects
│   │   │   │   ├── LoginRequestDTO.java
│   │   │   │   ├── LoginResponseDTO.java
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── EmployeeResponseDTO.java
│   │   │   │   ├── BidDTO.java
│   │   │   │   └── ... (40+ DTOs)
│   │   │   │
│   │   │   ├── Mapper/                  # Entity-DTO Mappers
│   │   │   │   ├── UserMapper.java               # MapStruct mapper
│   │   │   │   ├── OrderMapper.java
│   │   │   │   ├── ProductMapper.java
│   │   │   │   └── BiddingMapper.java
│   │   │   │
│   │   │   ├── Security/                # Security Configuration
│   │   │   │   ├── JwtService.java               # JWT generation/validation
│   │   │   │   ├── SecurityConfig.java           # Security filter chain
│   │   │   │   └── JwtRequestFilter.java         # JWT authentication filter
│   │   │   │
│   │   │   ├── Enum/                    # Enumerations
│   │   │   │   ├── UserRole.java                 # User roles
│   │   │   │   ├── BidStatus.java                # Bid statuses
│   │   │   │   └── SectionStatus.java            # Section statuses
│   │   │   │
│   │   │   ├── Exception/               # Custom Exceptions
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── AccessDeniedException.java
│   │   │   │   ├── BusinessValidationException.java
│   │   │   │   └── GlobalExceptionHandler.java   # Exception handler
│   │   │   │
│   │   │   └── AnalifyApplication.java  # Main Application Class
│   │   │
│   │   └── resources/
│   │       ├── application.properties    # Configuration file
│   │       └── data.sql                  # (Optional) Initial data
│   │
│   └── test/
│       └── java/com/analyfy/analify/
│           └── (Test classes)
│
├── pom.xml                              # Maven dependencies
├── mvnw                                 # Maven wrapper (Unix)
├── mvnw.cmd                             # Maven wrapper (Windows)
└── README.md                            # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)** 21 or higher
- **Maven** 3.6+ (or use included Maven wrapper)
- **PostgreSQL** 14 or higher
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### Database Setup

1. **Install PostgreSQL** (if not installed)

2. **Create Database**:
```sql
CREATE DATABASE analify;
```

3. **Create User** (optional, recommended for security):
```sql
CREATE USER analify_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE analify TO analify_user;
```

### Configuration

1. **Open** `src/main/resources/application.properties`

2. **Update Database Connection**:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/analify
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
```

3. **Configure Server Port** (default is 8081):
```properties
server.port=8081
```

### Build & Run

#### Using Maven Wrapper (Recommended)

**On Linux/Mac**:
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

**On Windows**:
```cmd
# Build the project
mvnw.cmd clean install

# Run the application
mvnw.cmd spring-boot:run
```

#### Using Maven (if installed globally)

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

### Verify Installation

1. **Check if server is running**:
   - Console should show: `Started AnalifyApplication in X seconds`
   - Default URL: http://localhost:8081

2. **Test an endpoint**:
```bash
curl http://localhost:8081/api/bidding/categories
```

You should receive a JSON response (may be empty initially).

## 🗄 Database Schema

### Entity Relationship Overview

```
Region (1) ──→ (N) State
State (1) ──→ (N) City
City (1) ──→ (N) Store

Store (1) ──→ (N) Caissier
Store (1) ──→ (1) AdminStore

Investor (1) ──→ (N) Product
Product (1) ──→ (N) Subcategory
Subcategory (1) ──→ (N) Category

Caissier (1) ──→ (N) Order
Order (1) ──→ (N) OrderItem
Product (1) ──→ (N) OrderItem

Store (1) ──→ (N) Stock
Product (1) ──→ (N) Stock

# Bidding System
Category (1) ──→ (N) Rang
Rang (1) ──→ (N) Face
Face (1) ──→ (N) Section
Section (1) ──→ (N) Bid
Investor (1) ──→ (N) Bid
```

### Key Tables

#### Users (Inheritance Strategy: JOINED)
- `users` - Base user table
- `caissier` - Cashier-specific fields
- `admin_store` - Store admin-specific fields
- `admin_g` - General admin-specific fields
- `investor` - Investor-specific fields

#### Core Business
- `store` - Physical store locations
- `product` - Product catalog
- `orders` - Order headers
- `order_item` - Order line items
- `stock` - Product inventory by store

#### Bidding System
- `category` - Top-level bidding categories
- `rang` - Second-level classification
- `face` - Third-level classification
- `section` - Biddable sections
- `bid` - Bid records

### Schema Auto-Generation

The application uses Hibernate's DDL auto-generation:

```properties
# In application.properties
spring.jpa.hibernate.ddl-auto=update
```

**Options**:
- `update` - Update schema on startup (recommended for development)
- `create` - Drop and recreate schema (⚠️ destroys data)
- `create-drop` - Create on startup, drop on shutdown
- `validate` - Validate schema matches entities
- `none` - No schema management

## 📚 API Documentation

### Base URL
```
http://localhost:8081/api
```

### Authentication Required

All endpoints (except `/auth/login`) require JWT token:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 🔐 Authentication API

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 1,
    "userName": "John Doe",
    "mail": "user@example.com",
    "role": "INVESTOR"
  }
}
```

---

### 📦 Orders API

#### Get All Orders (with filters)
```http
GET /api/orders?filterStoreId=5&filterRegionId=2
Authorization: Bearer {token}
```

**Query Parameters**:
- `filterStoreId` - Filter by store
- `filterRegionId` - Filter by region
- `filterStateId` - Filter by state
- `filterCaissierId` - Filter by cashier
- `filterProductId` - Filter by product

#### Create Order
```http
POST /api/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "cashierId": 3,
  "items": [
    {
      "productId": 15,
      "quantity": 2,
      "discount": 0.1
    }
  ]
}
```

#### Update Ship Date
```http
PATCH /api/orders/123/ship-date?shipDate=2026-01-15
Authorization: Bearer {token}
```

#### Delete Order
```http
DELETE /api/orders/123
Authorization: Bearer {token}
```

---

### 🏪 Products API

#### Get All Products
```http
GET /api/products?filterStoreId=5
Authorization: Bearer {token}
```

#### Create Product
```http
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "productName": "Laptop Dell XPS",
  "price": 1299.99,
  "subcategoryId": 8,
  "investorId": 4
}
```

#### Update Product
```http
PUT /api/products/25
Authorization: Bearer {token}
Content-Type: application/json

{
  "productName": "Laptop Dell XPS 15",
  "price": 1399.99
}
```

#### Update Stock
```http
PATCH /api/products/25/stock
Authorization: Bearer {token}
Content-Type: application/json

{
  "storeId": 3,
  "quantity": 50
}
```

#### Low Stock Alerts
```http
GET /api/products/alerts/low-stock
Authorization: Bearer {token}
```

---

### 👥 Employees API

#### Get All Employees (Admin only)
```http
GET /api/employees/getall
Authorization: Bearer {token}
```

#### Get Store Employees
```http
GET /api/employees/store/5
Authorization: Bearer {token}
```

#### Get Employee Profile
```http
GET /api/employees/12
Authorization: Bearer {token}
```

#### Create Employee
```http
POST /api/employees/add
Authorization: Bearer {token}
Content-Type: application/json

{
  "userName": "Jane Smith",
  "mail": "jane@example.com",
  "password": "securePassword123",
  "dateOfBirth": "1990-05-15",
  "role": "CAISSIER",
  "storeId": 5,
  "salary": 2500.00,
  "dateStarted": "2024-01-01"
}
```

#### Update Employee
```http
PUT /api/employees/12
Authorization: Bearer {token}
Content-Type: application/json

{
  "userName": "Jane Doe",
  "mail": "jane.doe@example.com",
  "salary": 2800.00
}
```

#### Assign Role
```http
PUT /api/employees/12/assign-role?newRole=ADMIN_STORE&storeId=5
Authorization: Bearer {token}
```

---

### 📊 Analytics API

#### Get Dashboard Statistics
```http
GET /api/analytics/dashboard?startDate=2024-01-01&endDate=2024-12-31&storeId=5
Authorization: Bearer {token}
```

**Query Parameters**:
- `startDate` - Filter start date
- `endDate` - Filter end date
- `storeId` - Filter by store
- `investorId` - Filter by investor
- `productId` - Filter by product

**Response**:
```json
{
  "totalRevenue": 156789.50,
  "totalStockValue": 89234.00,
  "totalOrders": 1245,
  "totalProductsSold": 3456,
  "averageOrderValue": 125.89,
  "lowStockCount": 12,
  "revenueOverTime": [...],
  "topProducts": [...],
  "salesByRegion": {...}
}
```

---

### 💰 Bidding API

#### Get Categories
```http
GET /api/bidding/categories
Authorization: Bearer {token}
```

#### Get Rangs by Category
```http
GET /api/bidding/categories/5/rangs
Authorization: Bearer {token}
```

#### Get Faces by Rang
```http
GET /api/bidding/rangs/8/faces
Authorization: Bearer {token}
```

#### Get Sections by Face
```http
GET /api/bidding/faces/12/sections
Authorization: Bearer {token}
```

#### Get Section Details
```http
GET /api/bidding/sections/45
Authorization: Bearer {token}
```

#### Place Bid
```http
POST /api/bidding/bids
Authorization: Bearer {token}
Content-Type: application/json

{
  "sectionId": 45,
  "amount": 5000.00
}
```

#### Cancel Bid
```http
DELETE /api/bidding/bids/123
Authorization: Bearer {token}
```

#### Get My Bids
```http
GET /api/bidding/my-bids
Authorization: Bearer {token}
```

#### Get My Current Winning Bids
```http
GET /api/bidding/my-current-winning-bids
Authorization: Bearer {token}
```

#### Get My Possessions
```http
GET /api/bidding/my-possessions
Authorization: Bearer {token}
```

#### Get Season Info
```http
GET /api/bidding/season/current
Authorization: Bearer {token}
```

**Response**:
```json
{
  "currentMonth": 1,
  "currentPeriod": 1,
  "isBiddingOpen": true,
  "daysUntilClose": 15,
  "periodStartDate": "2026-01-01",
  "periodEndDate": "2026-01-31",
  "biddingOpenDate": "2026-01-01",
  "biddingCloseDate": "2026-01-31"
}
```

---

## 🔒 Security & Authentication

### JWT Authentication Flow

```
1. User sends credentials → POST /api/auth/login
2. Server validates credentials
3. Server generates JWT token (contains userId, role)
4. Client stores token (localStorage)
5. Client sends token with every request: Authorization: Bearer {token}
6. JwtRequestFilter intercepts request
7. JwtService validates token
8. Request attributes populated: userId, role
9. Controller receives authenticated request
10. Service layer performs role-based authorization
```

### JWT Token Structure

**Payload**:
```json
{
  "userId": 123,
  "role": "INVESTOR",
  "iat": 1704067200,
  "exp": 1704153600
}
```

### Role-Based Authorization

Authorization is enforced in the **Service Layer**:

```java
public List<OrderDTO> getAllOrders(Long userId, UserRole role) {
    if (role == UserRole.ADMIN_STORE) {
        // Get store admin's store ID
        Long storeId = getStoreIdForAdmin(userId);
        // Return only orders from that store
        return orderRepository.findByStoreId(storeId);
    } else if (role == UserRole.ADMIN_G) {
        // Return all orders
        return orderRepository.findAll();
    } else {
        throw new AccessDeniedException("Insufficient permissions");
    }
}
```

### Security Configuration

**Security Filter Chain** (`SecurityConfig.java`):
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

### Password Encryption

Passwords are hashed using **BCrypt**:

```java
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    public void createUser(String password) {
        String hashedPassword = passwordEncoder.encode(password);
        // Store hashedPassword in database
    }
}
```

---

## 🧠 Business Logic

### Monthly Bidding Cycle

**Automated Scheduler** (`BiddingService.java`):

```java
@Scheduled(cron = "0 0 0 1 * *") // Runs at midnight on 1st of every month
public void increasePricesForNewMonth() {
    // 1. Get all sections
    List<Section> sections = sectionRepository.findAll();
    
    // 2. Increase prices by 2%
    sections.forEach(section -> {
        BigDecimal newPrice = section.getBasePrice()
            .multiply(BigDecimal.valueOf(1.02));
        section.setBasePrice(newPrice);
        section.setCurrentPrice(newPrice);
        section.setStatus(SectionStatus.OPEN);
        section.setWinnerInvestor(null);
    });
    
    // 3. Set new deadline
    LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(
        LocalDate.now().lengthOfMonth()
    );
    
    sections.forEach(section -> 
        section.setDeadline(lastDayOfMonth.atTime(23, 59, 59))
    );
    
    sectionRepository.saveAll(sections);
}
```

### Bid Processing Logic

**When investor places a bid**:

1. Validate bid amount > current price
2. Check section is OPEN
3. Update previous PENDING bid → OUTBID
4. Create new bid with status PENDING
5. Update section's current price
6. Save all changes in transaction

**When section closes**:

1. Find highest bid (status = PENDING)
2. Mark bid as WINNER
3. Assign investor to section
4. Set section status to CLOSED

### Order Total Calculation

```java
public BigDecimal calculateOrderTotal(Order order) {
    return order.getItems().stream()
        .map(item -> {
            BigDecimal price = item.getPrice();
            BigDecimal discount = item.getDiscount();
            int quantity = item.getQuantity();
            
            BigDecimal lineTotal = price
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.subtract(discount));
            
            return lineTotal;
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

---

## ⚠️ Error Handling

### Global Exception Handler

All exceptions are caught by `GlobalExceptionHandler.java`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Custom Exceptions

- **ResourceNotFoundException** - 404 Not Found
- **AccessDeniedException** - 403 Forbidden
- **BusinessValidationException** - 400 Bad Request
- **AuthenticationException** - 401 Unauthorized

---

## ⚙️ Configuration

### Application Properties Reference

```properties
# Application Name
spring.application.name=analify

# Server Configuration
server.port=8081

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/analify
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Logging Configuration
logging.level.root=INFO
logging.level.com.analyfy.analify=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# JWT Configuration (in code)
jwt.secret=your-secret-key-min-256-bits
jwt.expiration=86400000
```

---

## 🧪 Testing

### Run Unit Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

### Test Coverage

```bash
./mvnw clean test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

---

## 📦 Production Deployment

### Build JAR File

```bash
./mvnw clean package
```

Output: `target/analify-0.0.1-SNAPSHOT.jar`

### Run JAR

```bash
java -jar target/analify-0.0.1-SNAPSHOT.jar
```

### Environment Variables (Production)

```bash
export DB_URL=jdbc:postgresql://prod-db.example.com:5432/analify
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=very-secure-secret-key-for-production
export SERVER_PORT=8081

java -jar analify.jar
```

### Docker Deployment (Optional)

Create `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/analify-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t analify-backend .
docker run -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/analify \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=yourpassword \
  analify-backend
```

---

## 🔧 Troubleshooting

### Common Issues

**Port already in use**:
```bash
# Change port in application.properties
server.port=8082
```

**Database connection failed**:
- Verify PostgreSQL is running: `sudo systemctl status postgresql`
- Check credentials in `application.properties`
- Test connection: `psql -U postgres -d analify`

**Lombok not working**:
- Enable annotation processing in IDE
- IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
- Eclipse: Install Lombok plugin

**MapStruct errors**:
```bash
# Clean and rebuild
./mvnw clean install -U
```

**JWT token issues**:
- Check token expiration time
- Verify secret key is at least 256 bits
- Ensure token is sent in Authorization header

---

## 📊 Performance Optimization

### Database Indexing

Add indexes for frequently queried fields:
```sql
CREATE INDEX idx_order_cashier ON orders(cashier_id);
CREATE INDEX idx_product_investor ON product(investor_id);
CREATE INDEX idx_bid_section ON bid(section_id);
```

### Connection Pool Tuning

In `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

### Query Optimization

Use `@EntityGraph` to avoid N+1 queries:
```java
@EntityGraph(attributePaths = {"items", "items.product"})
@Query("SELECT o FROM Order o WHERE o.cashier.id = :cashierId")
List<Order> findByCashierId(@Param("cashierId") Long cashierId);
```

---

## 📝 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use Lombok for boilerplate reduction
- Keep controllers thin (delegate to services)
- Use DTOs for all API responses
- Document complex business logic

### Git Workflow
- Create feature branches: `feature/add-bidding-analytics`
- Write descriptive commit messages
- Run tests before committing
- Keep commits atomic and focused

---

## 📞 Support & Documentation

- **Main README**: [`../README.md`](../README.md)
- **Frontend README**: [`../frontAnalify/README.md`](../frontAnalify/README.md)
- **Bidding System**: [`../BIDDING_SYSTEM_COMPLETE_FEATURES.md`](../BIDDING_SYSTEM_COMPLETE_FEATURES.md)

---

**Built with Spring Boot 3.4.13 and Java 21**
