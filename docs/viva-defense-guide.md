# Wall of Goodwill: Comprehensive Viva Defense Handbook & Demo Script

---

## 1. Project Abstract & Core Problem Statement

### Problem Statement
University campus environments frequently suffer from two recurring resource issues:
1. **Resource Wastage**: Graduating seniors often discard valuable academic textbooks, calculators, lab coats, and hostel furniture because existing public marketplaces (OLX, Quikr, Facebook Marketplace) cater to broad metropolitan areas, attract strangers, pose safety risks, and demand paid delivery or payment fees.
2. **Informal Chaos**: Campus WhatsApp and Telegram groups are disorganized, lack search filters, get flooded with chit-chat, and offer no accountability or verification of student identity.

### Solution
**Wall of Goodwill** is a dedicated campus-only Android mobile marketplace and peer-to-peer donation platform built on a zero-cost, high-performance architecture. It guarantees campus security through physical hostel notice-board QR verification, encourages circular campus economy through goodwill gamification, and provides structured donation claims and real-time item chat.

---

## 2. Competitive Analysis Matrix

| Feature / Metric | OLX / Facebook Marketplace | Campus WhatsApp / Telegram Groups | Wall of Goodwill |
|---|---|---|---|
| **Audience Scope** | City-wide strangers | Mixed (often includes alumni & outsiders) | **Strictly verified campus students only** |
| **Verification Method**| Phone OTP (no campus bond) | None (anyone with invite link) | **Physical Hostel Notice Board QR Scanning** |
| **Donation / Free Focus**| Low (dominated by commercial sellers)| Unstructured messages | **First-class Donation Claims Flow with review** |
| **Search & Filtering**| Broad geo-radius | None (manual scroll / keyword chat search) | **Category, Type (Donate/Sell), Hostel Block** |
| **Incentives & Morale**| None | None | **Goodwill Points, Philanthropist Badges** |
| **Cost to Run** | High cloud infrastructure | Free (third-party managed) | **Zero-Cost In-Memory & Open-Source Stack** |

---

## 3. High-Yield Viva Questions & Answers

### Category A: System Architecture & Zero-Cost Design

#### Q1: What does "Zero-Cost Architecture" mean in this project?
**Answer**:
The project runs without requiring external paid database engines (like AWS RDS, paid Firebase tiers, or MongoDB Atlas) or third-party SMS/email verification services. The Go backend operates as a self-contained, thread-safe in-memory service utilizing `sync.RWMutex`. This allows full local execution, lightning-fast automated testing, and deployment to free compute tiers (such as Render, Fly.io, or Railway) with zero cloud bills.

#### Q2: If the backend uses in-memory storage, how do you handle data persistence in a production deployment?
**Answer**:
The architecture follows clean domain separation. Domain interfaces (e.g., `ListingsRepository` or `UsersRepository`) decouple business logic from storage mechanics. In production, the in-memory store can be swapped for PostgreSQL (using `pgx` or `gorm`) or Google Cloud Firestore with zero changes to HTTP handlers or business logic.

---

### Category B: Go Backend & Concurrency Mechanics

#### Q3: Why did you choose Go for the backend instead of Node.js or Python?
**Answer**:
1. **Concurrency Performance**: Go's lightweight goroutines (initial memory footprint of ~2KB vs ~1MB per OS thread) allow thousands of concurrent student requests with minimal RAM.
2. **Type Safety & Maintainability**: Strong static typing prevents runtime type errors and ensures reliable JSON contracts.
3. **Single Binary Deployment**: Go compiles down to a single self-contained native binary with zero runtime dependency overhead.

#### Q4: How do you guarantee thread safety with concurrent HTTP requests in Go?
**Answer**:
Each incoming HTTP request in Go is handled inside an independent goroutine spawned by the `net/http` server. To prevent race conditions when reading and mutating shared slices and maps, our domain services embed `sync.RWMutex`:
- **`RLock()` / `RUnlock()`** are used during read operations (e.g., `ListListings`, `GetProfile`, `GetPoints`). Multiple goroutines can read concurrently without blocking one another.
- **`Lock()` / `Unlock()`** are used during write operations (e.g., `CreateListing`, `AcceptRequest`, `RecordReport`). This enforces exclusive write access, preventing data corruption and concurrent map writes.
- We verify this using the Go race detector (`go test -race ./...`).

#### Q5: Why did you use Chi router instead of the standard library `net/http` ServeMux or Gin?
**Answer**:
Chi is 100% compatible with standard library `http.Handler` and `http.HandlerFunc`, without vendor lock-in. It provides efficient URL pattern routing (like `/v1/listings/{id}`), middleware chaining (`middleware.Logger`, `middleware.Recoverer`), and context injection with virtually zero memory allocation overhead compared to heavier frameworks.

---

### Category C: Android Architecture & Best Practices

#### Q6: Why did you choose the MVVM pattern over traditional MVC?
**Answer**:
- **Separation of Concerns**: In MVC, Activities become massive "God Objects" handling both UI manipulation, network callbacks, and state.
- **Lifecycle Awareness**: In MVVM, ViewModels survive configuration changes (such as device rotation), preserving UI state. LiveData observers ensure UI components are only updated when in an active lifecycle state (`STARTED` or `RESUMED`), eliminating memory leaks and crashes.
- **Testability**: Business logic inside ViewModels and Repositories can be unit-tested without requiring the Android UI framework.

#### Q7: Why use ViewBinding instead of `findViewById` or DataBinding?
**Answer**:
1. **Null Safety**: ViewBinding creates direct references to views with compile-time type safety; if a view is only in certain layout configurations, it is marked `@Nullable`.
2. **Type Safety**: The compiler generates fields typed according to the XML tags, eliminating `ClassCastException`.
3. **Performance**: Unlike DataBinding, ViewBinding does not require XML tags or annotation processing at build time, resulting in faster compilation.
4. **Cleanliness**: It completely eliminates verbose, error-prone `findViewById` boilerplate.

#### Q8: How does the application handle offline situations and broken image URLs?
**Answer**:
We implemented a centralized `CategoryPlaceholderHelper` on Android. When an item has no uploaded photo or the device is offline, Glide falls back to curated Material vector illustrations specific to the item category (`ic_placeholder_books`, `ic_placeholder_electronics`, `ic_placeholder_cycles`, etc.). This guarantees the UI looks complete and polished even with zero network connectivity.

---

### Category D: Security, Auth & Moderation

#### Q9: How does the Physical Notice Board QR verification work, and why is it superior to SMS OTP?
**Answer**:
1. **Cost & Reliability**: SMS gateways incur recurring fees per message and suffer from carrier latency.
2. **Physical Proximity Proof**: SMS OTP does not prove a student is physically in the college hostel. An admin generates a time-limited, usage-capped QR code (e.g., unexpired for 24h, max 100 uses) and posts it on the physical hostel notice board.
3. **Verification**: When a newly registered student scans the physical poster, the app submits the code to `POST /v1/auth/verify-qr`. Upon validation, their account status is elevated to `verified=true`.

#### Q10: How does Community Moderation prevent spam and abusive listings?
**Answer**:
Instead of relying solely on manual admin intervention, we implemented decentralized auto-concealment:
- Students can tap "Report Listing" and select a reason (e.g., inappropriate item, prohibited commercial sale, spam).
- Each report increments the listing's report counter.
- When reports reach the auto-concealment threshold, the listing status automatically switches to `HIDDEN`, which immediately excludes it from the public feed (`GET /v1/listings`).

#### Q11: How is Goodwill Gamification calculated?
**Answer**:
Students earn **50 Goodwill Points** every time they successfully complete a donation (when the donor marks a claim request as `ACCEPTED`). As their points cross predefined thresholds, they unlock achievement badges:
- **Campus Donor**: 1+ donations
- **Goodwill Ambassador**: 3+ donations (150+ points)
- **Legendary Philanthropist**: 5+ donations (250+ points)
This fosters a competitive and rewarding culture of generosity on campus.

---

## 4. Live Viva Demonstration Script (5-Minute Walkthrough)

Follow these exact steps during your practical viva evaluation or presentation:

### Preparation:
1. Open a terminal in `/Users/adrinesolomon/MAD/server`.
2. Start the Go backend:
   ```bash
   /usr/local/go/bin/go run ./cmd/api
   ```
3. Open a second terminal window for running API commands.

---

### Demonstration Flow:

#### Step 1: Prove System Health
Show the server is running and healthy:
```bash
curl -s http://localhost:8080/health | jq .
```
*Expected Output*: `{"status":"ok", "service":"wall-of-goodwill-api", ...}`

---

#### Step 2: Admin Generates Physical Notice Board QR Code
Simulate the campus warden generating an invite poster code:
```bash
curl -s -X POST http://localhost:8080/v1/admin/qr/generate \
  -H "Content-Type: application/json" \
  -d '{"label": "Hostel Block A Main Notice Board", "durationSeconds": 86400, "maxUses": 50}' | jq .
```
*Take note of the generated code (e.g., `GW-XXXXXX`).*

---

#### Step 3: Register a New Student (Unverified State)
Register a student who hasn't scanned the board yet:
```bash
curl -s -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"uid": "student-viva", "name": "Viva Student", "email": "viva@campus.edu", "hostelBlock": "Hostel Block A (Boys)", "roomNo": "301"}' | jq .
```
*Highlight to the examiner*: The response shows `"verified": false`.

---

#### Step 4: Scan Notice Board QR & Attain Campus Verification
Submit the QR code scanned from the poster:
```bash
curl -s -X POST http://localhost:8080/v1/auth/verify-qr \
  -H "Authorization: Bearer mock-token-student-viva" \
  -H "Content-Type: application/json" \
  -d '{"code": "GW-NOTICEBOARD-A"}' | jq .
```
*Highlight*: Account is now elevated to `"verified": true`.

---

#### Step 5: Post a Free Donation Listing
Create a textbook donation listing:
```bash
curl -s -X POST http://localhost:8080/v1/listings \
  -H "Authorization: Bearer mock-token-student-viva" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Operating Systems Concepts (Silberschatz)",
    "description": "Donating to juniors preparing for sem exams",
    "category": "BOOKS",
    "type": "DONATE",
    "condition": "LIKE_NEW"
  }' | jq .
```
*Take note of the created listing ID.*

---

#### Step 6: Peer Submits a Donation Claim Request
A junior student submits a claim request:
```bash
curl -s -X POST http://localhost:8080/v1/requests \
  -H "Authorization: Bearer mock-token-junior-42" \
  -H "Content-Type: application/json" \
  -d '{
    "listingId": "listing-1",
    "message": "Hi senior, I am taking OS this semester and would love to have this book!"
  }' | jq .
```
*Highlight*: Donor automatically receives an in-app notification!

---

#### Step 7: Donor Accepts Claim & Receives Goodwill Points
Donor accepts the junior's claim:
```bash
# Donor fetches incoming requests
curl -s -H "Authorization: Bearer mock-token-student-viva" http://localhost:8080/v1/requests/incoming | jq .

# Check Donor's Goodwill points balance (awarded automatically)
curl -s -H "Authorization: Bearer mock-token-student-viva" http://localhost:8080/v1/points/me | jq .
```
*Highlight*: Points incremented, and status badges unlocked!

---

#### Step 8: Community Moderation & Auto-Concealment
Demonstrate security by reporting an abusive listing:
```bash
# Student reports listing for the first time
curl -s -X POST http://localhost:8080/v1/listings/listing-1/report \
  -H "Authorization: Bearer mock-token-reporter-1" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Inappropriate or commercial listing"}' | jq .

# Second student reports same listing -> Triggers Auto-Concealment
curl -s -X POST http://localhost:8080/v1/listings/listing-1/report \
  -H "Authorization: Bearer mock-token-reporter-2" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Spam"}' | jq .
```
*Highlight*: The listing status switches to `HIDDEN`, and querying `GET /v1/listings` confirms the item has vanished from public feeds.

---

## 5. Potential Professor Follow-Up Questions & Quick Answers

- **"What if two people try to claim a donation at the exact same millisecond?"**
  - *Answer*: Go's `sync.RWMutex.Lock()` serializes request processing. The donor evaluates all incoming requests via `/v1/requests/incoming` and explicitly chooses the single recipient.
- **"Why does the Android app use Gradle Version Catalog (`libs.versions.toml`)?"**
  - *Answer*: It provides centralized dependency management, type-safe accessor generation in Kotlin DSL build scripts, and avoids version conflicts across multi-module builds.
- **"How does the app protect against unauthorized edits?"**
  - *Answer*: Handlers extract the student's UID from the authenticated token context and compare it with `listing.SellerID`. If they do not match, HTTP `403 Forbidden` is returned immediately.
