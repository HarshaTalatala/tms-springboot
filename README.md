# Transport Management System (TMS) - Backend

Spring Boot backend for managing transportation logistics: load scheduling, carrier bidding, booking allocation. Features optimistic locking, capacity validation, and comprehensive test coverage.

## Features

### Core Functionality
- **Load Management:** Create, list, retrieve, cancel loads with cargo details
- **Transporter Registry:** Register transporters, manage truck inventory, track ratings
- **Bidding System:** Submit bids with capacity constraints, automatic best-bid selection
- **Booking Engine:** Accept bids, allocate trucks, multi-truck load support
- **Status Tracking:** POSTED → OPEN_FOR_BIDS → BOOKED → COMPLETED lifecycle

### Technical Features
- **Optimistic Locking:** `@Version` on Load entity prevents concurrent update conflicts
- **Capacity Validation:** `trucksOffered ≤ availableTrucks`, automatic truck count management
- **Intelligent Scoring:** `score = 0.7 × (1/proposedRate) + 0.3 × (rating/5)`
- **Clock Abstraction:** Injected `Clock` bean for testable time-based operations
- **Clean Code:** No JavaDoc overhead, self-documenting code with clear types and naming
- **Request Validation:** Jakarta validation annotations on DTOs
- **Global Exception Handling:** Centralized error responses
- **Database Indexing:** Optimized queries on loadId, transporterId, status

### Test Coverage (47 Unit Tests)
- **BidServiceImplTest:** 14 tests - bid submission, listing with filters, status updates
- **BookingServiceImplTest:** 12 tests - booking creation, capacity validation, cancellation
- **LoadServiceImplTest:** 11 tests - load CRUD, status transitions, best bid selection
- **TransporterServiceImplTest:** 7 tests - registration, truck inventory updates
- **WeightUnitTest:** 2 tests - unit conversion logic
- **Integration Test:** Application context loads successfully

## Architecture

### Layered Design Pattern

```
Request
   ↓
Controller Layer (Request/Response handling)
   ↓
DTO Layer (Data transformation & validation)
   ↓
Service Layer (Business logic & rules)
   ↓
Repository Layer (Data access abstraction)
   ↓
Entity Layer (JPA models with optimistic locking)
   ↓
PostgreSQL Database
```

### Key Components

| Layer | Responsibility |
|-------|-----------------|
| **Controller** | HTTP endpoint mapping, request routing, status code management |
| **DTO** | Request validation, response serialization, API contracts |
| **Service** | Business rule enforcement, capacity validation, state transitions |
| **Repository** | Database queries, custom query execution via JPA |
| **Entity** | JPA-mapped domain objects with `@Version` for optimistic locking |
| **Exception Handler** | Centralized error handling and HTTP response transformation |

### Concurrency Control
- **Optimistic Locking:** `@Version` field on `Load` entity ensures consistency
- **Best Bid Uniqueness:** Database constraint prevents multiple ACCEPTED bids per load
- **Atomic Transactions:** Service methods use `@Transactional` for atomicity

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.0 | Core framework & web server |
| Java | 25 | Primary language |
| JPA / Hibernate | (Spring Data) | ORM & persistence |
## Tech Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.0 | Application framework |
| Java | 25 | Programming language |
| Spring Data JPA | (included) | ORM and repository layer |
| PostgreSQL | Latest | Relational database |
| Maven | 3.9.10 | Build & dependency management |
| Lombok | Latest | Reduce boilerplate |
| Jakarta Validation | (included) | Request validation |
| JUnit 5 Jupiter | Latest | Unit testing |
| Mockito | Latest | Test mocking |

## Architecture

**Layers:** Controller → DTO → Service → Repository → Entity

**Key Design Decisions:**
- **Constructor Injection:** All dependencies injected via constructors for immutability
- **DTO Pattern:** Request/Response DTOs separate from domain entities
- **Clock Abstraction:** Injected `Clock` bean makes time-based operations testable
- **No Entity Exposure:** Controllers never return entities directly
- **Spring Data JPA:** Query methods follow property path naming (`findByLoad_Id`, `findByTransporter_TransporterId`)

**Recent Improvements:**
- Removed redundant Booking entity columns (loadId, bidId, transporterId) - data accessible via relationships
- Removed verbose JavaDoc - modern code is self-documenting with clear types and annotations
- Fixed BidRepository method names to match Spring Data JPA property path syntax
- Added Clock bean configuration for testable time operations
- Created comprehensive unit tests for BookingServiceImpl and TransporterServiceImpl
- All 47 tests passing with 0 errors, 0 warnings

## Database Schema

### Entity-Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     TRANSPORTERS                                │
├─────────────────────────────────────────────────────────────────┤
│ PK  transporterId (UUID)                                        │
│     companyName (VARCHAR)                                       │
│     rating (DOUBLE)                                             │
└──────────┬──────────────────────────────────────────────────────┘
           │
           │ 1..N
           │
┌──────────▼──────────────────────────────────────────────────────┐
│                        TRUCKS                                   │
├─────────────────────────────────────────────────────────────────┤
│ PK  id (UUID)                                                   │
│ FK  transporter_id → TRANSPORTERS.transporterId                │
│     truckType (VARCHAR)                                         │
│     count (INTEGER)                                             │
└─────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                        LOADS                                    │
├─────────────────────────────────────────────────────────────────┤
│ PK  id (UUID)                                                   │
│     version (BIGINT) - Optimistic locking                       │
│     pickupLocation (VARCHAR)                                    │
│     deliveryLocation (VARCHAR)                                  │
│     weight (DECIMAL)                                            │
│     cargoType (VARCHAR)                                         │
│     pickupDate (TIMESTAMP)                                      │
│     deliveryDate (TIMESTAMP)                                    │
│     offeredPrice (DECIMAL)                                      │
│     trucksRequired (INTEGER)                                    │
│     remainingTrucks (INTEGER)                                   │
│     status (ENUM: POSTED, OPEN_FOR_BIDS, BOOKED, CANCELLED)   │
│     datePosted (TIMESTAMP)                                      │
│ IDX idx_load_status (status)                                    │
└──────────┬──────────────────────────────────────────────────────┘
           │
           │ 1..N
           │
┌──────────▼──────────────────────────────────────────────────────┐
│                        BIDS                                     │
├─────────────────────────────────────────────────────────────────┤
│ PK  bidId (UUID)                                                │
│ FK  load_id → LOADS.id                                          │
│ FK  transporter_id → TRANSPORTERS.transporterId                │
│     proposedRate (DOUBLE)                                       │
│     trucksOffered (INTEGER)                                     │
│     truckType (VARCHAR)                                         │
│     status (ENUM: PENDING, ACCEPTED, REJECTED)                 │
│     submittedAt (TIMESTAMP)                                     │
│ IDX idx_bid_load_id (load_id)                                   │
│ IDX idx_bid_transporter_id (transporter_id)                     │
└──────────┬──────────────────────────────────────────────────────┘
           │
           │ 1..N
           │
┌──────────▼──────────────────────────────────────────────────────┐
│                      BOOKINGS                                   │
├─────────────────────────────────────────────────────────────────┤
│ PK  bookingId (UUID)                                            │
│ FK  load_id → LOADS.id                                          │
│ FK  transporter_id → TRANSPORTERS.transporterId                │
│ FK  bid_id → BIDS.bidId                                         │
│     allocatedTrucks (INTEGER)                                   │
│     finalRate (DOUBLE)                                          │
│     status (ENUM: CONFIRMED, COMPLETED, CANCELLED)             │
│     bookedAt (TIMESTAMP)                                        │
│ IDX idx_booking_load_id (load_id)                               │
└─────────────────────────────────────────────────────────────────┘
```

### Database Constraints
- **Unique ACCEPTED Bid per Load:** Only one bid can have status ACCEPTED for a given load
- **Load Status Transitions:** Enforced via service-layer validation
- **Capacity Constraints:** `trucksOffered ≤ availableTrucks` validated at service layer
- **Version Column:** Prevents lost updates under concurrent load modifications

## Business Rules

### 1. Capacity Validation

**Rule:** A transporter cannot bid more trucks than available.

**Implementation:**
- When submitting a bid: `trucksOffered ≤ availableTrucks` is validated
- When accepting a bid: Available truck count is decremented atomically
- When cancelling a booking: Trucks are restored to available pool

**Example:**
```
Transporter ABC has 5 available trucks
- Bid on Load 1 with 3 trucks → Validation: 3 ≤ 5 ✓
- Accept Bid → availableTrucks becomes 5 - 3 = 2
- Cancel Booking → availableTrucks restored to 5
```

### 2. Load Status Transitions

**Valid State Machine:**
```
POSTED → OPEN_FOR_BIDS → BOOKED → COMPLETED
  ↓
  └──────────→ CANCELLED (from any state except COMPLETED)
```

**Rules:**
- Load starts in POSTED after creation
- Transitions to OPEN_FOR_BIDS when first bid arrives
- Moves to BOOKED when a bid is accepted
- Cannot accept bids on CANCELLED or already-BOOKED loads
- Cannot cancel a load with confirmed bookings (except CANCELLED status)

### 3. Multi-Truck Allocation

**Rule:** A load requiring N trucks can be fulfilled by multiple transporters.

**Implementation:**
- `trucksRequired` = total trucks needed
- `remainingTrucks` = trucks still needed after allocations
- Multiple bookings can exist for one load, each with different transporters
- Booking is complete when `remainingTrucks ≤ 0`

**Example:**
```
Load requires 5 trucks
- Bid 1: Transporter A offers 3 trucks → Accept → remainingTrucks = 2
- Bid 2: Transporter B offers 2 trucks → Accept → remainingTrucks = 0
- Bid 3: Transporter C offers 1 truck → Reject (load already fulfilled)
```

### 4. Best-Bid Scoring Algorithm

**Formula:**
```
score = 0.7 × (1 / proposedRate) + 0.3 × (transporterRating / 5.0)
```

**Components:**
- **Price Factor (70%):** Lower rates score higher. Normalized by inversion.
- **Rating Factor (30%):** Transporter's star rating (0-5). Higher is better.

**Example Calculation:**
```
Bid 1: Rate = $100, Transporter Rating = 4.5 stars
  score1 = 0.7 × (1/100) + 0.3 × (4.5/5)
         = 0.7 × 0.01 + 0.3 × 0.9
         = 0.007 + 0.27 = 0.277

Bid 2: Rate = $120, Transporter Rating = 5.0 stars
  score2 = 0.7 × (1/120) + 0.3 × (5.0/5)
         = 0.7 × 0.0083 + 0.3 × 1.0
         = 0.0058 + 0.3 = 0.3058

Bid 2 wins despite higher rate (score: 0.3058 > 0.277)
```

### 5. Bidding Constraints

- **No bids on CANCELLED loads:** System rejects with validation error
- **No bids on already-BOOKED loads:** System rejects with validation error
- **Duplicate bids allowed:** Same transporter can bid multiple times (different rates/truck counts)

### 6. Booking Cancellation

**Rules:**
- Only CONFIRMED bookings can be cancelled
- Cancelling a booking:
  - Reverts load status from BOOKED to OPEN_FOR_BIDS (if no other bookings exist)
  - Restores transporter's available trucks
  - Marks booking as CANCELLED
  - Updates `remainingTrucks` if necessary

### 7. Optimistic Locking Behavior

- Load entity includes `@Version` field for concurrency control
- On concurrent updates to same load, one transaction commits successfully
- Subsequent transaction fails with `OptimisticLockingFailureException`
- Client must retry the operation with fresh load state
- Prevents scenarios like double-acceptance of bids or phantom reads

---

## API Documentation

### Base URL
```
http://localhost:8080
```

### Load APIs

#### 1. Create Load
**POST** `/load`

Create a new transportation load.

**Request:**
```json
{
  "pickupLocation": "New York, NY",
  "deliveryLocation": "Los Angeles, CA",
  "weight": 5000.50,
  "cargoType": "Electronics",
  "pickupDate": "2025-12-20T09:00:00",
  "deliveryDate": "2025-12-25T18:00:00",
  "offeredPrice": 15000.00,
  "trucksRequired": 3
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "pickupLocation": "New York, NY",
  "deliveryLocation": "Los Angeles, CA",
  "weight": 5000.50,
  "cargoType": "Electronics",
  "pickupDate": "2025-12-20T09:00:00",
  "deliveryDate": "2025-12-25T18:00:00",
  "offeredPrice": 15000.00,
  "trucksRequired": 3,
  "remainingTrucks": 3,
  "status": "POSTED",
  "datePosted": "2025-12-04T10:30:45"
}
```

**Status:** 201 Created

---

#### 2. List Loads
**GET** `/load?page=0&size=20&sort=datePosted,desc`

Retrieve all loads with pagination.

**Response:**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "pickupLocation": "New York, NY",
      "deliveryLocation": "Los Angeles, CA",
      "weight": 5000.50,
      "cargoType": "Electronics",
      "pickupDate": "2025-12-20T09:00:00",
      "deliveryDate": "2025-12-25T18:00:00",
      "offeredPrice": 15000.00,
      "trucksRequired": 3,
      "remainingTrucks": 1,
      "status": "BOOKED",
      "datePosted": "2025-12-04T10:30:45"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

**Status:** 200 OK

---

#### 3. Get Load by ID
**GET** `/load/{id}`

Retrieve a specific load by its UUID.

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "pickupLocation": "New York, NY",
  "deliveryLocation": "Los Angeles, CA",
  "weight": 5000.50,
  "cargoType": "Electronics",
  "pickupDate": "2025-12-20T09:00:00",
  "deliveryDate": "2025-12-25T18:00:00",
  "offeredPrice": 15000.00,
  "trucksRequired": 3,
  "remainingTrucks": 0,
  "status": "BOOKED",
  "datePosted": "2025-12-04T10:30:45"
}
```

**Status:** 200 OK | 404 Not Found

---

#### 4. Cancel Load
**PATCH** `/load/{id}/cancel`

Cancel a load and reject all pending bids.

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "pickupLocation": "New York, NY",
  "deliveryLocation": "Los Angeles, CA",
  "weight": 5000.50,
  "cargoType": "Electronics",
  "pickupDate": "2025-12-20T09:00:00",
  "deliveryDate": "2025-12-25T18:00:00",
  "offeredPrice": 15000.00,
  "trucksRequired": 3,
  "remainingTrucks": 3,
  "status": "CANCELLED",
  "datePosted": "2025-12-04T10:30:45"
}
```

**Status:** 200 OK | 404 Not Found | 409 Conflict (Optimistic lock fail)

---

#### 5. Get Best Bids for Load
**GET** `/load/{id}/best-bids`

Retrieve all bids for a load sorted by score (highest first).

**Response:**
```json
[
  {
    "bidId": "660e8400-e29b-41d4-a716-446655440001",
    "loadId": "550e8400-e29b-41d4-a716-446655440000",
    "transporterId": "770e8400-e29b-41d4-a716-446655440002",
    "proposedRate": 100.00,
    "trucksOffered": 3,
    "truckType": "Flatbed",
    "status": "ACCEPTED",
    "submittedAt": "2025-12-04T11:00:00",
    "score": 0.305
  },
  {
    "bidId": "880e8400-e29b-41d4-a716-446655440003",
    "loadId": "550e8400-e29b-41d4-a716-446655440000",
    "transporterId": "990e8400-e29b-41d4-a716-446655440004",
    "proposedRate": 120.00,
    "trucksOffered": 2,
    "truckType": "Van",
    "status": "PENDING",
    "submittedAt": "2025-12-04T11:30:00",
    "score": 0.277
  }
]
```

**Status:** 200 OK | 404 Not Found

---

### Transporter APIs

#### 1. Create Transporter
**POST** `/transporter`

Register a new transporter company.

**Request:**
```json
{
  "companyName": "FastFreight Logistics",
  "rating": 4.5
}
```

**Response:**
```json
{
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "companyName": "FastFreight Logistics",
  "rating": 4.5,
  "availableTrucks": [],
  "bids": [],
  "bookings": []
}
```

**Status:** 201 Created

---

#### 2. Get Transporter by ID
**GET** `/transporter/{id}`

Retrieve transporter details and truck inventory.

**Response:**
```json
{
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "companyName": "FastFreight Logistics",
  "rating": 4.8,
  "availableTrucks": [
    {
      "id": "aaa0-e29b-41d4-a716-bbb",
      "truckType": "Flatbed",
      "count": 5
    },
    {
      "id": "ccc0-e29b-41d4-a716-ddd",
      "truckType": "Tanker",
      "count": 2
    }
  ],
  "bids": 12,
  "bookings": 8
}
```

**Status:** 200 OK | 404 Not Found

---

#### 3. Update Transporter Trucks
**PUT** `/transporter/{id}/trucks`

Add or update truck inventory for a transporter.

**Request:**
```json
{
  "truckType": "Flatbed",
  "count": 10
}
```

**Response:**
```json
{
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "companyName": "FastFreight Logistics",
  "rating": 4.8,
  "availableTrucks": [
    {
      "id": "aaa0-e29b-41d4-a716-bbb",
      "truckType": "Flatbed",
      "count": 10
    }
  ]
}
```

**Status:** 200 OK | 404 Not Found

---

### Bid APIs

#### 1. Submit Bid
**POST** `/bid`

Submit a competitive bid for a load.

**Request:**
```json
{
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "proposedRate": 100.00,
  "trucksOffered": 3,
  "truckType": "Flatbed"
}
```

**Response:**
```json
{
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "proposedRate": 100.00,
  "trucksOffered": 3,
  "truckType": "Flatbed",
  "status": "PENDING",
  "submittedAt": "2025-12-04T11:00:00",
  "score": 0.305
}
```

**Status:** 201 Created | 400 Bad Request (validation error) | 409 Conflict (load CANCELLED/BOOKED)

---

#### 2. List All Bids
**GET** `/bid`

Retrieve all submitted bids.

**Response:**
```json
[
  {
    "bidId": "660e8400-e29b-41d4-a716-446655440001",
    "loadId": "550e8400-e29b-41d4-a716-446655440000",
    "transporterId": "770e8400-e29b-41d4-a716-446655440002",
    "proposedRate": 100.00,
    "trucksOffered": 3,
    "truckType": "Flatbed",
    "status": "PENDING",
    "submittedAt": "2025-12-04T11:00:00",
    "score": 0.305
  }
]
```

**Status:** 200 OK

---

#### 3. Get Bid by ID
**GET** `/bid/{id}`

Retrieve a specific bid.

**Response:**
```json
{
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "proposedRate": 100.00,
  "trucksOffered": 3,
  "truckType": "Flatbed",
  "status": "PENDING",
  "submittedAt": "2025-12-04T11:00:00",
  "score": 0.305
}
```

**Status:** 200 OK | 404 Not Found

---

#### 4. Reject Bid
**PATCH** `/bid/{id}/reject`

Reject a bid and mark it as REJECTED.

**Response:**
```json
{
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "proposedRate": 100.00,
  "trucksOffered": 3,
  "truckType": "Flatbed",
  "status": "REJECTED",
  "submittedAt": "2025-12-04T11:00:00",
  "score": 0.305
}
```

**Status:** 200 OK | 404 Not Found

---

### Booking APIs

#### 1. Create Booking
**POST** `/booking`

Accept a bid and create a booking (allocation).

**Request:**
```json
{
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "allocatedTrucks": 3,
  "finalRate": 100.00
}
```

**Response:**
```json
{
  "bookingId": "111e8400-e29b-41d4-a716-222",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "allocatedTrucks": 3,
  "finalRate": 100.00,
  "status": "CONFIRMED",
  "bookedAt": "2025-12-04T11:30:00"
}
```

**Status:** 201 Created | 400 Bad Request | 409 Conflict (duplicate accepted bid)

---

#### 2. Get Booking by ID
**GET** `/booking/{id}`

Retrieve booking details.

**Response:**
```json
{
  "bookingId": "111e8400-e29b-41d4-a716-222",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "allocatedTrucks": 3,
  "finalRate": 100.00,
  "status": "CONFIRMED",
  "bookedAt": "2025-12-04T11:30:00"
}
```

**Status:** 200 OK | 404 Not Found

---

#### 3. Cancel Booking
**PATCH** `/booking/{id}/cancel`

Cancel a confirmed booking and restore truck availability.

**Response:**
```json
{
  "bookingId": "111e8400-e29b-41d4-a716-222",
  "loadId": "550e8400-e29b-41d4-a716-446655440000",
  "transporterId": "770e8400-e29b-41d4-a716-446655440002",
  "bidId": "660e8400-e29b-41d4-a716-446655440001",
  "allocatedTrucks": 3,
  "finalRate": 100.00,
  "status": "CANCELLED",
  "bookedAt": "2025-12-04T11:30:00"
}
```

**Status:** 200 OK | 404 Not Found | 409 Conflict

---

## Setup Instructions

### Prerequisites
- **Java 25** or higher installed
- **PostgreSQL 12** or higher running locally
- **Maven 3.9** or higher
- **Git** for cloning the repository

### Step 1: Clone the Repository
```bash
git clone https://github.com/HarshaTalatala/tms-springboot.git
cd tms-springboot
```

### Step 2: Create PostgreSQL Database
```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE "TransportManagementSystem";

# Exit psql
\q
```

### Step 3: Configure Database Connection
Edit `src/main/resources/application.properties`:

```properties
spring.application.name=TransportManagementSystem

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/TransportManagementSystem
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

**Available `ddl-auto` options:**
- `update`: Create tables if missing, update existing schema
- `create`: Drop & recreate tables on every startup (development only)
- `validate`: Verify schema matches entities (production recommended)
- `none`: No automatic schema management

### Step 4: Build the Project
```bash
mvn clean install
```

This command:
- Cleans previous builds (`clean`)
- Downloads dependencies (`install`)
- Runs all tests
- Creates JAR in `target/` directory

### Step 5: Run the Application
```bash
mvn spring-boot:run
```

**Expected output:**
```
2025-12-04 10:30:45.123 - Started TransportManagementSystemApplication in 3.456 seconds
2025-12-04 10:30:45.124 - Tomcat started on port(s): 8080 (http)
```

---

## How to Run Locally

### Option 1: Using Maven (Development)
```bash
# In project root directory
mvn clean install
mvn spring-boot:run
```

Access application: `http://localhost:8080`

### Option 2: Using JAR (Production-like)
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/TransportManagementSystem-0.0.1-SNAPSHOT.jar
```

### Option 3: With Custom Database Properties
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.password=your_password"
```

### Stopping the Server
Press `Ctrl+C` in the terminal running the application.

---

## Testing Instructions

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=LoadServiceImplTest
```

### Run Tests with Coverage Report
```bash
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Manual API Testing via cURL

#### Create a Transporter
```bash
curl -X POST http://localhost:8080/transporter \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "FastFreight Logistics",
    "rating": 4.5
  }'
```

#### Create a Load
```bash
curl -X POST http://localhost:8080/load \
  -H "Content-Type: application/json" \
  -d '{
    "pickupLocation": "New York, NY",
    "deliveryLocation": "Los Angeles, CA",
    "weight": 5000.50,
    "cargoType": "Electronics",
    "pickupDate": "2025-12-20T09:00:00",
    "deliveryDate": "2025-12-25T18:00:00",
    "offeredPrice": 15000.00,
    "trucksRequired": 3
  }'
```

#### Submit a Bid
```bash
curl -X POST http://localhost:8080/bid \
  -H "Content-Type: application/json" \
  -d '{
    "loadId": "YOUR_LOAD_ID",
    "transporterId": "YOUR_TRANSPORTER_ID",
    "proposedRate": 100.00,
    "trucksOffered": 3,
    "truckType": "Flatbed"
  }'
```

### Automated Testing with Postman

A Postman collection (`TMS-Collection.postman_collection.json`) is available in the repository root. 
To import:
1. Open Postman
2. Click **Import** → **Choose Files**
3. Select `TMS-Collection.postman_collection.json`
4. Configure environment variables (database credentials)
5. Run collection with **Runner**

---

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/harsha/tms/
│   │       ├── TransportManagementSystemApplication.java     (Entry point)
│   │       ├── controller/
│   │       │   ├── LoadController.java                       (Load endpoints)
│   │       │   ├── TransporterController.java                (Transporter endpoints)
│   │       │   ├── BidController.java                        (Bid endpoints)
│   │       │   └── BookingController.java                    (Booking endpoints)
│   │       ├── service/
│   │       │   ├── LoadService.java                          (Load business logic interface)
│   │       │   ├── TransporterService.java                   (Transporter business logic interface)
│   │       │   ├── BidService.java                           (Bid business logic interface)
│   │       │   ├── BookingService.java                       (Booking business logic interface)
│   │       │   ├── LoadStatusValidator.java                  (Status transition rules)
│   │       │   ├── ScoreWeights.java                         (Bid scoring constants)
│   │       │   └── impl/
│   │       │       ├── LoadServiceImpl.java                   (Load implementation)
│   │       │       ├── TransporterServiceImpl.java            (Transporter implementation)
│   │       │       ├── BidServiceImpl.java                    (Bid implementation)
│   │       │       └── BookingServiceImpl.java                (Booking implementation)
│   │       ├── repository/
│   │       │   ├── LoadRepository.java                       (Load data access)
│   │       │   ├── TransporterRepository.java                (Transporter data access)
│   │       │   ├── BidRepository.java                        (Bid data access)
│   │       │   ├── BookingRepository.java                    (Booking data access)
│   │       │   └── TruckRepository.java                      (Truck data access)
│   │       ├── entity/
│   │       │   ├── Load.java                                 (Load JPA entity)
│   │       │   ├── Transporter.java                          (Transporter JPA entity)
│   │       │   ├── Bid.java                                  (Bid JPA entity)
│   │       │   ├── Booking.java                              (Booking JPA entity)
│   │       │   ├── Truck.java                                (Truck JPA entity)
│   │       │   ├── BookingStatus.java                        (Enum: CONFIRMED, COMPLETED, CANCELLED)
│   │       │   └── BidStatus.java                            (Enum: PENDING, ACCEPTED, REJECTED)
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   │   ├── LoadRequestDTO.java
│   │       │   │   ├── TransporterRequestDTO.java
│   │       │   │   ├── BidRequestDTO.java
│   │       │   │   ├── BookingRequestDTO.java
│   │       │   │   └── UpdateTrucksRequestDTO.java
│   │       │   └── response/
│   │       │       ├── LoadResponseDTO.java
│   │       │       ├── TransporterResponseDTO.java
│   │       │       ├── BidResponseDTO.java
│   │       │       └── BookingResponseDTO.java
│   │       └── exception/
│   │           ├── ResourceNotFoundException.java            (Resource not found)
│   │           ├── InvalidStatusTransitionException.java     (Invalid status change)
│   │           ├── InsufficientCapacityException.java        (Capacity violation)
│   │           ├── LoadAlreadyBookedException.java           (Load already booked)
│   │           └── handler/
│   │               └── GlobalExceptionHandler.java           (Centralized error handling)
│   └── resources/
│       └── application.properties                            (Configuration)
└── test/
    └── java/
        └── com/harsha/tms/
            ├── service/
            │   ├── WeightUnitTest.java                       (2 tests)
            │   └── impl/
            │       ├── BidServiceImplTest.java               (14 tests)
            │       ├── BookingServiceImplTest.java           (12 tests)
            │       ├── LoadServiceImplTest.java              (11 tests)
            │       └── TransporterServiceImplTest.java       (7 tests)
            └── TransportManagementSystemApplicationTests.java (1 test)
```

**Directory Purposes:**

| Directory | Purpose |
|-----------|---------|
| `controller/` | HTTP endpoints and request routing |
| `service/` | Business logic, validation, orchestration |
| `repository/` | Database queries (Spring Data JPA) |
| `entity/` | JPA entity models and enums |
| `dto/request/` | Request DTOs with validation |
| `dto/response/` | Response DTOs |
| `exception/` | Custom exceptions and global error handler |
| `test/` | Unit tests with JUnit 5 + Mockito |

---

## Future Enhancements

**Authentication & Security:**
- Spring Security with JWT, role-based access control (RBAC)
- OAuth 2.0 integration

**Deployment:**
- Docker + Kubernetes, CI/CD pipelines
- Blue-green deployment

**Performance:**
- Redis caching for frequently accessed data
- Distributed session management

**Monitoring:**
- Prometheus metrics, Grafana dashboards
- Distributed tracing (Jaeger)

**Async Processing:**
- Message queues (RabbitMQ/Kafka) for bid notifications
- WebSocket support for real-time updates

---

---

## Project Status

**Build Status:** ✅ SUCCESS  
**Test Coverage:** 47 unit tests, 0 failures, 0 errors  
**Code Quality:** 0 compiler warnings, 0 IDE warnings

## Author

**Name:** Harsha Vardhan Reddy Talatala  
**Email:** harsha.talatala@gmail.com  
**GitHub:** [https://github.com/HarshaTalatala/tms-springboot](https://github.com/HarshaTalatala/tms-springboot)

## Common Issues

**PostgreSQL Connection Error:**
```
FATAL: database "TransportManagementSystem" does not exist
```
Create database manually: `CREATE DATABASE "TransportManagementSystem";`

**Port Already in Use:**
```
Address already in use: bind
```
Change `server.port=8081` in `application.properties`

**Optimistic Lock Failure:**
```
OptimisticLockingFailureException
```
Expected under high concurrency. Retry with fresh data from GET request.
