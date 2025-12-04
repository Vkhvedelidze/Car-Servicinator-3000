# 🚗 Car Servicinator 3000

A comprehensive car service management system built with JavaFX and Supabase. This application streamlines the interaction between clients, mechanics, and administrators in a car service environment.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue?style=flat-square)
![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-green?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square)

---

## 📋 Table of Contents

- [Features](#-features)
- [Technologies Used](#-technologies-used)
- [Architecture & Design Patterns](#-architecture--design-patterns)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Running the Application](#-running-the-application)
- [Database Schema](#-database-schema)
- [User Roles](#-user-roles)
- [OOP Concepts Demonstrated](#-oop-concepts-demonstrated)
- [API Documentation](#-api-documentation)
- [Contributors](#-contributors)

---

## ✨ Features

### 👤 Client Features
- **User Registration & Authentication** - Secure signup/login with JWT tokens
- **Vehicle Management** - Add, edit, and manage multiple vehicles
- **Service Requests** - Request car services from mechanic shops
- **Request Tracking** - View status of service requests in real-time
- **Payment Processing** - Secure payment handling for completed services
- **Service History** - View past service records and invoices

### 🔧 Mechanic Features
- **Service Request Dashboard** - View and manage incoming service requests
- **Request Filtering** - Filter by status, client, or search terms
- **Service Management** - Accept, reject, or complete service requests
- **Status Updates** - Provide real-time updates to clients
- **Client Communication** - Add notes and communicate service details

### 👨‍💼 Admin Features
- **Analytics Dashboard** - View business metrics and statistics
- **User Management** - Manage clients, mechanics, and shops
- **Service Oversight** - Monitor all service requests across the system
- **Payment Tracking** - View payment history and financial reports
- **System Configuration** - Configure services and pricing

---

## 🛠 Technologies Used

### Core Technologies
- **Java 21** - Latest LTS version with modern language features
- **JavaFX 21.0.6** - Modern UI framework for desktop applications
- **Maven** - Dependency management and build automation

### Backend & Database
- **Supabase** - Backend-as-a-Service with PostgreSQL database
- **PostgreSQL** - Robust relational database
- **Supabase Auth** - Authentication and authorization with JWT
- **Row Level Security (RLS)** - Database-level security policies

### HTTP & Networking
- **OkHttp 4.12.0** - Modern HTTP client for API communication
- **Jackson 2.17.2** - JSON serialization/deserialization
- **Custom Interceptors** - Automatic authentication header injection

### Security
- **BCrypt** - Password hashing
- **JWT (JSON Web Tokens)** - Secure session management
- **Auth0 Java-JWT** - JWT parsing and validation

### UI
- **FXML** - Declarative UI design
- **BootstrapFX** - Modern styling framework
- **Custom CSS** - Enhanced visual design

---

## 🏗 Architecture & Design Patterns

### **1. Model-View-Controller (MVC)**
```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│    View     │ ───────>│ Controller   │ ───────>│    Model    │
│   (FXML)    │ <───────│   (Java)     │ <───────│   (Java)    │
└─────────────┘         └──────────────┘         └─────────────┘
```
- **View**: FXML files define UI structure
- **Controller**: Handle user interactions and business logic
- **Model**: Data entities (User, Vehicle, ServiceRequest, etc.)

### **2. Service Layer Pattern**
```
Controller → Service Layer → Supabase Client → Supabase API
```
- Separates business logic from data access
- Provides reusable CRUD operations
- Centralizes database interactions

### **3. Repository Pattern**
- Generic `Service<T>` interface defines standard operations
- `BaseSupabaseService<T>` implements common functionality
- Concrete services extend base class for specific entities

### **4. Singleton Pattern**
- Used for services: `UserService.getInstance()`
- Ensures single instance of database clients
- Efficient resource management

### **5. Interceptor Pattern**
- `SupabaseInterceptor`: Adds authentication headers to all REST API requests
- `AuthInterceptor`: Handles authentication API headers
- Implements cross-cutting concerns (authentication, logging)

### **6. Facade Pattern**
- `AuthService` provides simplified interface to `SupabaseAuthService`
- Maintains backward compatibility
- Simplifies complex subsystems

---

## 📁 Project Structure

```
Car-Servicinator-3000/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/programminggroupproject/
│   │   │       ├── CarServiceApp.java          # Application entry point
│   │   │       ├── Launcher.java               # JavaFX launcher
│   │   │       ├── client/                     # HTTP clients
│   │   │       │   ├── SupabaseAuthClient.java # Authentication API
│   │   │       │   └── SupabaseClient.java     # REST API client
│   │   │       ├── config/                     # Configuration
│   │   │       │   └── SupabaseConfig.java     # Supabase credentials
│   │   │       ├── controller/                 # MVC Controllers
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── RegisterController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   ├── ClientController.java
│   │   │       │   ├── ClientRequestsController.java
│   │   │       │   ├── MechanicController.java
│   │   │       │   ├── PaymentController.java
│   │   │       │   └── AdminController.java
│   │   │       ├── model/                      # Data models
│   │   │       │   ├── User.java
│   │   │       │   ├── Vehicle.java
│   │   │       │   ├── Service.java
│   │   │       │   ├── ServiceRequest.java
│   │   │       │   ├── ServiceRequestItem.java
│   │   │       │   ├── Payment.java
│   │   │       │   ├── MechanicShop.java
│   │   │       │   └── AuthResponse.java
│   │   │       ├── service/                    # Service layer
│   │   │       │   ├── Service.java            # Generic interface
│   │   │       │   ├── BaseSupabaseService.java # Base implementation
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── SupabaseAuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── VehicleService.java
│   │   │       │   ├── MechanicalService.java
│   │   │       │   ├── ServiceRequestService.java
│   │   │       │   ├── PaymentService.java
│   │   │       │   └── MechanicShopService.java
│   │   │       └── session/                    # Session management
│   │   │           └── Session.java
│   │   └── resources/
│   │       └── com/example/programminggroupproject/
│   │           ├── login-view.fxml
│   │           ├── register-view.fxml
│   │           ├── dashboard.fxml
│   │           ├── client-view.fxml
│   │           ├── client-requests-view.fxml
│   │           ├── mechanic-view.fxml
│   │           ├── payment-view.fxml
│   │           ├── admin-view.fxml
│   │           ├── styles.css
│   │           └── supabase.properties         # Database credentials
│   └── test/
├── pom.xml                                     # Maven configuration
└── README.md
```

---

## 📦 Prerequisites

Before running this application, ensure you have:

- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+**
- **Supabase Account** (free tier available)
- **Git** (for cloning the repository)

---

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/Car-Servicinator-3000.git
cd Car-Servicinator-3000
```

### 2. Set Up Supabase

1. Create a free account at [supabase.com](https://supabase.com)
2. Create a new project
3. Navigate to **Settings → API** to find your credentials
4. Create the database schema (see [Database Schema](#-database-schema))
5. Enable Row Level Security (RLS) on all tables
6. Set up authentication triggers (optional but recommended)

### 3. Configure Application

Edit `src/main/resources/supabase.properties`:

```properties
# Supabase Configuration
supabase.url=https://your-project.supabase.co
supabase.key=your-service-role-key
supabase.anon.key=your-anon-public-key
```

⚠️ **Security Note**: Never commit your actual credentials to version control. Use environment variables for production.

### 4. Install Dependencies
```bash
mvn clean install
```

---

## 🏃 Running the Application

### Option 1: Using Maven
```bash
mvn clean javafx:run
```

### Option 2: Using Your IDE
1. Open the project in IntelliJ IDEA or Eclipse
2. Navigate to `src/main/java/com/example/programminggroupproject/Launcher.java`
3. Right-click and select "Run"

### Option 3: Build JAR
```bash
mvn clean package
java -jar target/Programming-Group-project-1.0-SNAPSHOT.jar
```

---

## 🗄 Database Schema

### Core Tables

#### **users**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_user_id UUID REFERENCES auth.users(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('client', 'mechanic', 'admin')),
    shop_id UUID REFERENCES mechanic_shops(id),
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **vehicles**
```sql
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID REFERENCES users(id) ON DELETE CASCADE,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    license_plate VARCHAR(50) UNIQUE,
    vin VARCHAR(17),
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **services**
```sql
CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    estimated_duration INTEGER, -- minutes
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **service_requests**
```sql
CREATE TABLE service_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID REFERENCES users(id),
    vehicle_id UUID REFERENCES vehicles(id),
    shop_id UUID REFERENCES mechanic_shops(id),
    status VARCHAR(50) DEFAULT 'pending',
    notes TEXT,
    scheduled_date TIMESTAMP,
    completed_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **service_request_items**
```sql
CREATE TABLE service_request_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_request_id UUID REFERENCES service_requests(id) ON DELETE CASCADE,
    service_id UUID REFERENCES services(id),
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **payments**
```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_request_id UUID REFERENCES service_requests(id),
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **mechanic_shops**
```sql
CREATE TABLE mechanic_shops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 👥 User Roles

### 🔵 Client
- **Email**: client@example.com
- **Default Password**: Set during registration
- **Capabilities**: Request services, track requests, manage vehicles, process payments

### 🟢 Mechanic
- **Email**: mechanic@example.com
- **Default Password**: Set during registration
- **Capabilities**: View service requests, accept/reject requests, update service status

### 🔴 Admin
- **Email**: admin@example.com
- **Default Password**: Set during registration
- **Capabilities**: Full system access, analytics, user management

---

## 🎓 OOP Concepts Demonstrated

### 1. **Polymorphism**
- **Interface Implementation**: `Service<T>` interface with multiple implementations
- **Method Overloading**: Multiple `getAll()` methods with different parameters
- **Runtime Polymorphism**: All service classes extend `BaseSupabaseService<T>`

```java
Service<User> userService = UserService.getInstance();  // Polymorphic reference
Service<Vehicle> vehicleService = VehicleService.getInstance();
```

### 2. **Inheritance**
- `BaseSupabaseService<T>` provides common functionality
- All concrete services inherit CRUD operations
- Controllers inherit from JavaFX's base classes

### 3. **Encapsulation**
- Private fields with public getters/setters
- Data hiding in model classes
- Protected methods in base service class

### 4. **Abstraction**
- Abstract `BaseSupabaseService<T>` class
- `Service<T>` interface defines contract
- Hides implementation details from controllers

### 5. **Generics**
- Type-safe service layer: `Service<T>`
- Reusable code for different entity types
- Compile-time type checking

### 6. **Singleton Pattern**
```java
public static synchronized UserService getInstance() {
    if (instance == null) {
        instance = new UserService();
    }
    return instance;
}
```

### 7. **Composition**
- Controllers compose services
- Services compose HTTP clients
- Separation of concerns

---

## 📡 API Documentation

### Authentication Endpoints

#### Sign Up
```java
authService.signUp(email, password, fullName, role, shopId);
```

#### Sign In
```java
User user = authService.signIn(email, password);
```

#### Sign Out
```java
authService.signOut();
```

### Service Operations (Generic)

All services inherit these operations from `Service<T>`:

```java
// CRUD Operations
Optional<T> get(UUID id)
List<T> getAll()
T create(T object)
T update(UUID id, T object)
void delete(UUID id)

// Filtering
List<T> filter(String column, String operator, Object value)
List<T> findBy(String column, Object value)
Optional<T> findOne(Map<String, Object> criteria)

// Searching
List<T> search(String column, String searchTerm)
List<T> searchMultiple(String searchTerm, String... columns)

// Ordering
List<T> getAllOrdered(String orderBy, boolean ascending)

// Bulk Operations
List<T> createMultiple(List<T> objects)
void deleteMultiple(List<UUID> ids)

// Utilities
boolean exists(UUID id)
int count()

// Async Operations
CompletableFuture<T> createAsync(T object)
```

---

## 🔐 Security Features

- **JWT Authentication** - Secure token-based authentication
- **Row Level Security** - Database-level access control
- **BCrypt Password Hashing** - Secure password storage
- **Session Management** - Automatic token refresh
- **HTTP Interceptors** - Automatic authentication header injection
- **Role-Based Access Control** - Different permissions for different roles

---

## 🐛 Troubleshooting

### Common Issues

#### 1. **NullPointerException with headers**
**Solution**: Ensure `supabase.anon.key` is set in `supabase.properties`

#### 2. **"ComboBox is not a valid type" error**
**Solution**: Add `<?import javafx.scene.control.ComboBox?>` to FXML files

#### 3. **"Invalid login credentials"**
**Solution**: Register a new account first before attempting to log in
or use these credentials: vako@example.com for client and password "testpass123_".
dsokolov@gmail.com and password "Password1".
admin@example.com for admin and password "testpass123_".

#### 4. **Connection timeout**
**Solution**: Check your internet connection and Supabase project status

#### 5. **Maven build fails**
**Solution**: Ensure JDK 21 is installed and `JAVA_HOME` is set correctly

---

## 📝 Future Enhancements

- [ ] Email notifications for service updates
- [ ] SMS notifications via Twilio integration
- [ ] Real-time chat between clients and mechanics
- [ ] Mobile application (Android/iOS)
- [ ] Advanced analytics and reporting
- [ ] PDF invoice generation
- [ ] Calendar integration for scheduling
- [ ] Multi-language support
- [ ] Dark mode theme
- [ ] Service history export (CSV/Excel)

---

## 👨‍💻 Contributors

- **Team Members** - Michail Sifakis, Denis Sokolov, Vako Khvedelidze, Gonzalo Nicolas, Oliver Holmes

---

- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/Car-Servicinator-3000/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/yourusername/Car-Servicinator-3000/discussions)

---

<div align="center">
  <strong>Built with ❤️ using Java & JavaFX</strong>
  <br>
  <sub>Car Servicinator 3000 © 2024</sub>
</div>

