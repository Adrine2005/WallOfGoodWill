# Wall of Goodwill (Campus-Only Marketplace & Donation App)

A campus-only Android application where verified students can **donate** items for free or **sell** used items at a price they choose. Built on a zero-cost stack (Firebase Spark, Cloudinary, Go backend).

---

## 📁 Repository Structure

```
.
├── android/                         # Android mobile application (Java, Material 3, API 34)
│   ├── app/                         # App module with ViewBinding and MVVM architecture
│   ├── gradle/libs.versions.toml    # Gradle version catalog for dependencies
│   ├── build.gradle.kts             # Root Gradle build script (Kotlin DSL)
│   └── settings.gradle.kts          # Gradle settings
│
├── server/                          # Go backend service (Chi router, Firebase Admin SDK)
│   ├── cmd/api/main.go              # Entrypoint and HTTP server
│   ├── internal/config/             # Runtime environment configuration
│   ├── internal/handlers/           # HTTP handlers (e.g. GET /health)
│   ├── go.mod                       # Go module dependencies
│   └── .env.example                 # Environment variables template
│
├── openspec/                        # OpenSpec specifications and changes
└── wall-of-goodwill-project-plan.md # Full project roadmap and architectural reference
```

---

## 🛠️ Prerequisites

- **Go**: 1.27+ installed (`go version`)
- **Java (JDK)**: OpenJDK 17+ (Bundled with Android Studio in `/Applications/Android Studio.app/Contents/jbr/Contents/Home`)
- **Android SDK**: API level 34 installed in `~/Library/Android/sdk`
- **Android Studio**: Android Studio (Koala / Ladybug or later)

---

## 🚀 Getting Started

### 1. Running the Go Backend

```bash
cd server

# Run tests
go test ./...

# Start the server (defaults to port 8080)
go run ./cmd/api

# In another terminal, verify the health endpoint
curl http://localhost:8080/health
# Response: {"status":"ok","timestamp":"...","service":"wall-of-goodwill-api"}
```

### 2. Authentication & Campus Verification API

The backend features a pluggable authentication token verification system with time-limited QR invitation codes posted on physical hostel notice boards to verify campus presence:

#### Endpoints:
- **`POST /v1/auth/register`**: Register student profile with name, email, hostel block, room number, and optional instant QR invite code.
- **`POST /v1/auth/verify-qr`** *(Authenticated)*: Validates unexpired QR code and grants campus verified status.
- **`GET /v1/auth/me`** *(Authenticated)*: Fetches current student profile and verification status.
- **`POST /v1/admin/qr/generate`**: Admin generates time-limited QR code (e.g., 24h duration with usage count limits).
- **`GET /v1/admin/qr/list`**: List active QR codes and usage counters.

#### Testing the Verification Lifecycle with `curl`:
```bash
# 1. Admin generates a notice board QR code
curl -X POST http://localhost:8080/v1/admin/qr/generate \
  -H "Content-Type: application/json" \
  -d '{"label": "Hostel Block A Notice Board", "durationSeconds": 86400, "maxUses": 100}'
# Example Response: {"code":"GW-8F2A1C", ...}

# 2. Student registers (without instant code -> unverified status)
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"uid": "student-101", "name": "Adrine Solomon", "email": "adrine@campus.edu", "hostelBlock": "Hostel Block A (Boys)", "roomNo": "302"}'
# Response: {"verified": false, "message": "user registered successfully", ...}

# 3. Student scans QR poster at the notice board
curl -X POST http://localhost:8080/v1/auth/verify-qr \
  -H "Authorization: Bearer mock-token-student-101" \
  -H "Content-Type: application/json" \
  -d '{"code": "GW-8F2A1C"}'
# Response: {"verified": true, "message": "campus verification successful", ...}
```

### 3. Campus Listings & Marketplace API

The marketplace supports both **Donations** (100% free) and **Sales** with multi-parameter filtering:

#### Endpoints:
- **`GET /v1/listings`**: Retrieve active listings with optional query filters:
  - `?category=BOOKS` (Options: `BOOKS`, `ELECTRONICS`, `CYCLES`, `KITCHENWARE`, `FURNITURE`, `FASHION`, `STATIONERY`, `OTHER`)
  - `?type=DONATE` or `?type=SELL`
  - `?hostelBlock=Hostel Block A (Boys)`
  - `?search=kettle`
- **`POST /v1/listings`** *(Authenticated)*: Create a new listing.
  - For `DONATE`, `price` is automatically 0.
  - For `SELL`, `price` must be greater than 0.
- **`GET /v1/listings/{id}`**: Get single listing details.
- **`GET /v1/listings/my`** *(Authenticated)*: Get all listings created by the logged-in student.
- **`DELETE /v1/listings/{id}`** *(Authenticated)*: Delete or close a listing (restricted to seller).

#### Testing Listings with `curl`:
```bash
# 1. Fetch public feed
curl http://localhost:8080/v1/listings

# 2. Filter by category
curl "http://localhost:8080/v1/listings?category=BOOKS"

# 3. Post a donation item (free)
curl -X POST http://localhost:8080/v1/listings \
  -H "Authorization: Bearer mock-token-student-101" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Data Structures & Algorithms Book",
    "description": "Donating to juniors after final semester",
    "category": "BOOKS",
    "type": "DONATE",
    "condition": "GOOD"
  }'

# 4. Post a sale item
curl -X POST http://localhost:8080/v1/listings \
  -H "Authorization: Bearer mock-token-student-101" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Study Table Lamp",
    "description": "Warm light, 3 brightness levels",
    "category": "ELECTRONICS",
    "type": "SELL",
    "price": 250,
    "condition": "LIKE_NEW"
  }'
```

### 4. Building the Android Application

#### Android Architecture:
- **`SplashActivity`**: Session check router routing unauthenticated users to `LoginActivity`, unverified students to `VerifyQrActivity`, and verified students directly to `MainActivity`.
- **`LoginActivity`**: Email & password authentication with input validation.
- **`RegisterActivity`**: Form with student details, hostel block selection dropdown, and optional instant QR code entry.
- **`VerifyQrActivity`**: Campus verification gateway with Google Code Scanner button and manual alphanumeric code entry fallback.
- **`AuthRepository` & `SessionManager`**: MVVM repository caching auth tokens and profile in SharedPreferences with Retrofit OkHttp `AuthInterceptor`.

#### Option A: Command Line
```bash
cd android

# Build debug APK
./gradlew assembleDebug
```

#### Option B: Android Studio
1. Open **Android Studio**.
2. Select **Open** and choose the `android/` directory (`/Users/adrinesolomon/MAD/android`).
3. Allow Gradle to sync.
4. Select an emulator or connected device and click **Run (▶)**.

---

## 🔒 Security & Version Control

- **`.gitignore`** is configured at the workspace root to ensure:
  - Secrets (`.env`, `serviceAccountKey.json`, `.pem`) are never committed.
  - Android build artifacts (`.gradle/`, `build/`, `*.apk`) are untracked.
  - Agent and workflow files (`.agents/`) are ignored.
