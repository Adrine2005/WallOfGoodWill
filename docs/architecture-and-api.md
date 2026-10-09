# Wall of Goodwill: System Architecture & API Specifications

This document outlines the detailed system architecture, concurrency guarantees, data models, sequence workflows, and API specifications for the **Wall of Goodwill** campus marketplace and donation platform.

---

## 1. High-Level System Architecture

The project consists of a native Android mobile client interacting with a modular Go HTTP backend. Both components are engineered around zero-cost, offline-first design principles.

```mermaid
graph TD
    subgraph Android Client [Android Application - Java & Material 3]
        UI[Activities / Fragments<br/>ViewBinding]
        VM[ViewModels<br/>LiveData Observers]
        REPO[Repository Layer<br/>SessionManager]
        NET[Retrofit 2 & OkHttp 3<br/>AuthInterceptor]
        
        UI -->|User Actions| VM
        VM -->|Data Stream| UI
        VM -->|Fetch / Mutate| REPO
        REPO -->|HTTP Requests| NET
    end

    subgraph Go Backend [Go Server - Chi Router & Clean Domain]
        ROUTER[Chi Router / Mux]
        AUTH_MW[Auth Middleware<br/>Token Extraction & Validation]
        HANDLERS[HTTP Handlers<br/>JSON Serialization]
        
        NET -->|REST / JSON| ROUTER
        ROUTER --> AUTH_MW
        AUTH_MW --> HANDLERS
        
        subgraph Domain Services [Synchronized In-Memory Domain Layer]
            AUTH_SVC[Auth & Users Service]
            LIST_SVC[Listings Service]
            REQ_SVC[Donation Requests Service]
            NOTIF_SVC[Notifications Service]
            CHAT_SVC[P2P Chat Service]
            WISH_SVC[Wishlist Service]
            PTS_SVC[Goodwill Points Service]
            MOD_SVC[Moderation Service]
        end
        
        HANDLERS --> AUTH_SVC
        HANDLERS --> LIST_SVC
        HANDLERS --> REQ_SVC
        HANDLERS --> NOTIF_SVC
        HANDLERS --> CHAT_SVC
        HANDLERS --> WISH_SVC
        HANDLERS --> PTS_SVC
        HANDLERS --> MOD_SVC
        
        subgraph Storage [Thread-Safe In-Memory Stores]
            STORE[(sync.RWMutex Protected<br/>In-Memory Slices & Maps)]
        end
        
        AUTH_SVC --> STORE
        LIST_SVC --> STORE
        REQ_SVC --> STORE
        NOTIF_SVC --> STORE
        CHAT_SVC --> STORE
        WISH_SVC --> STORE
        PTS_SVC --> STORE
        MOD_SVC --> STORE
    end
```

---

## 2. Concurrency & Thread-Safety Model

The backend operates without external database engines, maintaining in-memory state protected against race conditions:

### Read/Write Mutual Exclusion (`sync.RWMutex`)
Each domain service embeds a `sync.RWMutex` to protect its data structures:
- **Concurrent Readers (`RLock()` / `RUnlock()`)**: Multiple incoming HTTP requests (e.g., retrieving the public feed, checking points, browsing wishlist) acquire read locks simultaneously without blocking each other.
- **Exclusive Writers (`Lock()` / `Unlock()`)**: State-modifying operations (creating a listing, submitting a claim request, posting a chat message, submitting a report) acquire an exclusive write lock, guaranteeing zero data races (`-race` clean).

```go
type Service struct {
    mu       sync.RWMutex
    listings map[string]Listing
}

func (s *Service) ListListings() []Listing {
    s.mu.RLock()
    defer s.mu.RUnlock()
    // Concurrent read safe across hundreds of client goroutines
    ...
}

func (s *Service) CreateListing(item Listing) Listing {
    s.mu.Lock()
    defer s.mu.Unlock()
    // Exclusive write guarantees zero race conditions
    ...
}
```

---

## 3. Core Data Models

### 3.1 User & Verification
```go
type User struct {
    UID         string    `json:"uid"`
    Name        string    `json:"name"`
    Email       string    `json:"email"`
    HostelBlock string    `json:"hostelBlock"`
    RoomNo      string    `json:"roomNo"`
    Verified    bool      `json:"verified"`
    CreatedAt   time.Time `json:"createdAt"`
}

type InviteCode struct {
    Code      string    `json:"code"`
    Label     string    `json:"label"`
    ExpiresAt time.Time `json:"expiresAt"`
    MaxUses   int       `json:"maxUses"`
    UsedCount int       `json:"usedCount"`
}
```

### 3.2 Listings
```go
type ListingType string // "DONATE" or "SELL"
type ListingStatus string // "ACTIVE", "SOLD", "DONATED", "HIDDEN"

type Listing struct {
    ID          string        `json:"id"`
    Title       string        `json:"title"`
    Description string        `json:"description"`
    Category    string        `json:"category"`    // BOOKS, CYCLES, ELECTRONICS, etc.
    Type        ListingType   `json:"type"`        // DONATE | SELL
    Price       float64       `json:"price"`       // 0 for DONATE
    Condition   string        `json:"condition"`   // LIKE_NEW, GOOD, FAIR
    Status      ListingStatus `json:"status"`      // ACTIVE, SOLD, DONATED, HIDDEN
    SellerID    string        `json:"sellerId"`
    SellerName  string        `json:"sellerName"`
    HostelBlock string        `json:"hostelBlock"`
    Images      []string      `json:"images"`
    CreatedAt   time.Time     `json:"createdAt"`
}
```

### 3.3 Donation Requests
```go
type RequestStatus string // "PENDING", "ACCEPTED", "REJECTED"

type DonationRequest struct {
    ID           string        `json:"id"`
    ListingID    string        `json:"listingId"`
    ListingTitle string        `json:"listingTitle"`
    RequesterID  string        `json:"requesterId"`
    RequesterName string       `json:"requesterName"`
    DonorID      string        `json:"donorId"`
    Message      string        `json:"message"`
    Status       RequestStatus `json:"status"`
    CreatedAt    time.Time     `json:"createdAt"`
}
```

### 3.4 In-App Notifications
```go
type Notification struct {
    ID        string    `json:"id"`
    UserID    string    `json:"userId"`
    Title     string    `json:"title"`
    Body      string    `json:"body"`
    Type      string    `json:"type"`      // "REQUEST_RECEIVED", "REQUEST_ACCEPTED", etc.
    Data      map[string]string `json:"data,omitempty"`
    Read      bool      `json:"read"`
    CreatedAt time.Time `json:"createdAt"`
}
```

### 3.5 Item-Centric P2P Chat
```go
type ChatMessage struct {
    ID         string    `json:"id"`
    ListingID  string    `json:"listingId"`
    SenderID   string    `json:"senderId"`
    SenderName string    `json:"senderName"`
    ReceiverID string    `json:"receiverId"`
    Content    string    `json:"content"`
    CreatedAt  time.Time `json:"createdAt"`
}
```

### 3.6 Campus Wishlist
```go
type WishlistRequest struct {
    ID          string    `json:"id"`
    Title       string    `json:"title"`
    Description string    `json:"description"`
    Category    string    `json:"category"`
    StudentId   string    `json:"studentId"`
    StudentName string    `json:"studentName"`
    HostelBlock string    `json:"hostelBlock"`
    Status      string    `json:"status"` // "OPEN", "FULFILLED"
    CreatedAt   time.Time `json:"createdAt"`
}
```

### 3.7 Goodwill Points & Gamification
```go
type UserPoints struct {
    UserID      string   `json:"userId"`
    Points      int      `json:"points"`
    Donations   int      `json:"donationsCount"`
    Badges      []string `json:"badges"` // e.g., ["CAMPUS_DONOR", "GOODWILL_AMBASSADOR"]
    LastUpdated time.Time`json:"lastUpdated"`
}
```

### 3.8 Moderation & Reports
```go
type Report struct {
    ID        string    `json:"id"`
    ListingID string    `json:"listingId"`
    ReporterID string   `json:"reporterId"`
    Reason    string    `json:"reason"`
    CreatedAt time.Time `json:"createdAt"`
}
```

---

## 4. Sequence Workflows

### 4.1 Physical Campus QR Notice Board Verification

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Campus Admin
    actor Student as Student
    participant NoticeBoard as Hostel Notice Board Poster
    participant App as Android Client
    participant Server as Go Backend

    Admin->>Server: POST /v1/admin/qr/generate (duration=24h, maxUses=100)
    Server-->>Admin: QR Code payload {"code": "GW-9A2B1C"}
    Admin->>NoticeBoard: Print and post QR code on Physical Notice Board
    
    Student->>App: Register profile (name, hostelBlock, roomNo)
    App->>Server: POST /v1/auth/register
    Server-->>App: Registered (verified=false)
    
    Student->>NoticeBoard: Walks to Notice Board & Scans QR
    App->>Server: POST /v1/auth/verify-qr {"code": "GW-9A2B1C"}
    Server->>Server: Validate unexpired & usage limits
    Server->>Server: Set student.verified = true
    Server-->>App: 200 OK {"verified": true}
    App->>App: Unlocks full Marketplace access
```

### 4.2 Donation Request Claim & Handshake Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Donor as Donor (Student A)
    actor Claimant as Claimant (Student B)
    participant App as Android Client
    participant Server as Go Backend

    Donor->>Server: POST /v1/listings (type=DONATE, price=0)
    Server-->>Donor: Listing created (status=ACTIVE)
    
    Claimant->>App: Browses feed & views listing detail
    Claimant->>Server: POST /v1/requests {"listingId": "...", "message": "Need book for Sem 3"}
    Server->>Server: Create DonationRequest (status=PENDING)
    Server->>Server: Dispatch Notification to Donor
    Server-->>Claimant: 201 Created
    
    Donor->>Server: GET /v1/notifications
    Donor->>Server: GET /v1/requests/incoming
    Server-->>Donor: Returns list of pending claims
    
    Donor->>Server: PUT /v1/requests/{id}/status {"status": "ACCEPTED"}
    Server->>Server: Mark request ACCEPTED
    Server->>Server: Dispatch Notification to Claimant
    Server->>Server: Award Donor +50 Goodwill Points
    Server-->>Donor: 200 OK
```

### 4.3 Community Moderation & Auto-Concealment

```mermaid
sequenceDiagram
    autonumber
    actor Student1 as Student 1
    actor Student2 as Student 2
    participant Server as Go Backend

    Student1->>Server: POST /v1/listings/{id}/report {"reason": "prohibited item"}
    Server->>Server: Record Report (Count = 1)
    Server-->>Student1: Report recorded
    
    Student2->>Server: POST /v1/listings/{id}/report {"reason": "spam listing"}
    Server->>Server: Record Report (Count = 2 >= Threshold)
    Server->>Server: Set listing.Status = HIDDEN
    Server-->>Student2: Report recorded (Listing concealed)
    
    Note over Server: All future GET /v1/listings queries automatically exclude concealed listing
```

---

## 5. REST API Specifications

### 5.1 Auth Endpoints
- **`POST /v1/auth/register`**
  - Payload: `{"uid": "student-1", "name": "Adrine Solomon", "email": "adrine@univ.edu", "hostelBlock": "Block A", "roomNo": "204"}`
  - Response: `{"verified": false, "message": "user registered successfully", "user": {...}}`
- **`POST /v1/auth/verify-qr`**
  - Headers: `Authorization: Bearer mock-token-student-1`
  - Payload: `{"code": "GW-9A2B1C"}`
  - Response: `{"verified": true, "message": "campus verification successful"}`

### 5.2 Listings Endpoints
- **`GET /v1/listings?category=BOOKS&type=DONATE&search=algorithms`**
  - Response: `{"listings": [...], "count": 1}`
- **`POST /v1/listings`**
  - Headers: `Authorization: Bearer mock-token-student-1`
  - Payload:
    ```json
    {
      "title": "Calculus 9th Edition",
      "description": "Donating to 1st year students",
      "category": "BOOKS",
      "type": "DONATE",
      "condition": "GOOD"
    }
    ```
  - Response: `201 Created` with created listing object.

### 5.3 Chat Endpoints
- **`POST /v1/chat`**
  - Headers: `Authorization: Bearer mock-token-student-1`
  - Payload: `{"listingId": "listing-1", "receiverId": "seller-2", "content": "Can I pick this up tomorrow at 5pm?"}`
  - Response: `201 Created` with chat message object.
- **`GET /v1/chat/history?listingId=listing-1`**
  - Headers: `Authorization: Bearer mock-token-student-1`
  - Response: `{"messages": [...]}`

### 5.4 Goodwill Gamification
- **`GET /v1/points/me`**
  - Headers: `Authorization: Bearer mock-token-student-1`
  - Response:
    ```json
    {
      "userId": "student-1",
      "points": 150,
      "donationsCount": 3,
      "badges": ["CAMPUS_DONOR", "GOODWILL_AMBASSADOR"]
    }
    ```
