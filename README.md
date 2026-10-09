# Wall of Goodwill (Campus-Only Marketplace & Donation App)

[![Go](https://img.shields.io/badge/Go-1.22+-00ADD8?style=flat&logo=go)](https://golang.org)
[![Android](https://img.shields.io/badge/Android-API%2034-3DDC84?style=flat&logo=android)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20Go-blueviolet)](docs/architecture-and-api.md)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Wall of Goodwill** is a campus-only peer-to-peer mobile marketplace and donation platform designed specifically for university students. It empowers students to **donate** academic and lifestyle items for free to peers or **sell** pre-owned items at fair student prices. The system is engineered around a **zero-cost, offline-resilient architecture** that eliminates external database fees and cloud server costs while guaranteeing high performance, data consistency, and physical campus trust.

---

## 🌟 Key Features

1. **Physical Campus Verification**:
   - Location-bound trust model using time-limited physical QR poster invite codes placed on hostel notice boards.
   - Prevents outside spam, non-student fraud, and scam listings without requiring paid SMS or email APIs.

2. **Dual-Mode Listings (Donation & Sales)**:
   - **Donations (100% Free)**: Dedicated to passing on textbooks, stationery, and lab equipment to juniors.
   - **Sales (Fair Price)**: Student-to-student transactions for bicycles, study lamps, electronics, and room furniture.
   - Rich multi-parameter filtering by Category, Listing Type (`DONATE` vs `SELL`), Hostel Block, and keyword search.

3. **Peer-to-Peer Donation Request & Claim Flow**:
   - Interested students submit structured claims explaining their academic/personal need.
   - Donors review pending requests, approve the most deserving recipient, and coordinate item pickup.
   - Auto-resolves listings upon donation completion.

4. **In-App Notification Center**:
   - Real-time in-app alerts for incoming donation claims, claim approvals/rejections, and system announcements.
   - Unread badges and instant one-tap navigation.

5. **Direct Item-Centric P2P Chat**:
   - Item-scoped messaging threads between prospective buyers/claimants and sellers.
   - Thread-safe in-memory message history with timestamping and sender identification.

6. **Campus Wishlist & Student Requests Board**:
   - Reverse-marketplace where students post items they desperately need (e.g., engineering calculators, lab coats).
   - Other students can fulfill requests directly or respond with matching items.

7. **Goodwill Gamification (Points & Badges)**:
   - Rewards generous students with Goodwill Points for verified donations (+50 pts) and positive ratings (+10 pts).
   - Milestone badges: *Campus Donor*, *Goodwill Ambassador*, and *Legendary Philanthropist*.

8. **Community Moderation & Auto-Concealment**:
   - Decentralized student reporting for prohibited items, spam, or misleading content.
   - Automatic concealment threshold hides flagged items immediately pending administrative review.

9. **Offline Seed & Visual Fallbacks**:
   - Category-themed vector illustrations (Books, Electronics, Cycles, Stationery, Kitchenware, Furniture) for smooth offline browsing without broken placeholders.
   - Quick photo preset chips in item publishing.

---

## 📁 Repository Structure

```
.
├── android/                         # Android mobile application (Java, Material 3, API 34)
│   ├── app/                         # App module with ViewBinding and MVVM architecture
│   │   ├── src/main/java/com/goodwill/app/
│   │   │   ├── data/                # Data models, API interfaces & Repositories
│   │   │   └── ui/                  # Activities, Adapters & ViewModels
│   │   └── src/main/res/            # Layouts, vector drawables & design tokens
│   ├── gradle/libs.versions.toml    # Gradle version catalog
│   └── build.gradle.kts             # Gradle root build configuration
│
├── server/                          # Go backend service (Chi router, RESTful JSON)
│   ├── cmd/api/main.go              # Entrypoint and HTTP server routing
│   ├── internal/auth/               # Authentication domain & token parsing
│   ├── internal/chat/               # Item-centric P2P chat service
│   ├── internal/listings/           # Listings CRUD, search, filter & seed data
│   ├── internal/moderation/         # Community reports & auto-concealment service
│   ├── internal/notifications/      # In-app notification center service
│   ├── internal/points/             # Goodwill gamification & badges service
│   ├── internal/requests/           # Donation request claim lifecycle service
│   ├── internal/users/              # Student profile & hostel block management
│   ├── internal/wishlist/           # Campus wishlist request board service
│   └── internal/handlers/           # HTTP handlers & unit/integration tests
│
├── docs/                            # In-depth architectural & viva defense documentation
│   ├── architecture-and-api.md      # System topology, concurrency models & schemas
│   └── viva-defense-guide.md        # Comprehensive viva examination handbook & demo script
│
├── openspec/                        # OpenSpec change management & capability specifications
└── README.md                        # Project overview and run guide
```

---

## 🛠️ Prerequisites

- **Go**: 1.22+ (`go version`)
- **Java (JDK)**: OpenJDK 17+ (e.g., Android Studio JBR: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`)
- **Android SDK**: API level 34 (`~/Library/Android/sdk`)
- **Android Studio**: Android Studio Koala / Ladybug or later

---

## 🚀 Running the Project

### 1. Start the Go Backend

```bash
cd server

# Run the complete test suite
/usr/local/go/bin/go test ./...

# Start the server (runs on port 8080 by default)
/usr/local/go/bin/go run ./cmd/api
```

Verify service health:
```bash
curl http://localhost:8080/health
# Response: {"status":"ok","timestamp":"...","service":"wall-of-goodwill-api"}
```

### 2. Run the Android Application

#### Via Android Studio:
1. Open Android Studio.
2. Select **Open** and choose the `android/` directory (`/Users/adrinesolomon/MAD/android`).
3. Sync Gradle and click **Run (▶)** on an emulator or physical device.

#### Via Command Line:
```bash
cd android
./gradlew assembleDebug
```

---

## 📡 Complete REST API Reference

All protected endpoints accept an `Authorization: Bearer <token>` header. For local and offline testing, any bearer token prefixed with `mock-token-<uid>` (e.g., `mock-token-student-101`) is recognized and mapped to the respective user ID.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| **System** | | | |
| `GET` | `/health` | No | Service liveness and health status check |
| **Auth & Campus Verification** | | | |
| `POST` | `/v1/auth/register` | No | Register student profile with hostel details |
| `POST` | `/v1/auth/verify-qr` | Yes | Verify campus physical notice board QR invite code |
| `GET` | `/v1/auth/me` | Yes | Get currently authenticated student profile |
| `POST` | `/v1/admin/qr/generate` | No | Admin endpoint to generate time-limited QR code |
| `GET` | `/v1/admin/qr/list` | No | Admin endpoint to list all active QR codes |
| **Listings & Marketplace** | | | |
| `GET` | `/v1/listings` | No | List active listings with `?category=`, `?type=`, `?search=` filters |
| `POST` | `/v1/listings` | Yes | Create new listing (`DONATE` or `SELL`) |
| `GET` | `/v1/listings/{id}` | No | Get detailed information for a specific listing |
| `PUT` | `/v1/listings/{id}` | Yes | Edit listing attributes (title, price, category, etc.) |
| `PUT` | `/v1/listings/{id}/status` | Yes | Update listing status (`ACTIVE`, `SOLD`, `DONATED`) |
| `DELETE` | `/v1/listings/{id}` | Yes | Delete listing (owner only) |
| `GET` | `/v1/listings/my` | Yes | Get all listings posted by authenticated student |
| **Donation Requests** | | | |
| `POST` | `/v1/requests` | Yes | Submit a claim request for a donation item |
| `GET` | `/v1/requests/incoming` | Yes | List incoming claim requests received by donor |
| `GET` | `/v1/requests/my` | Yes | List claim requests submitted by student |
| `PUT` | `/v1/requests/{id}/status` | Yes | Donor accepts or rejects a claim request |
| **In-App Notifications** | | | |
| `GET` | `/v1/notifications` | Yes | Get list of user notifications with unread counts |
| `PUT` | `/v1/notifications/{id}/read`| Yes | Mark specific notification as read |
| **P2P Item Chat** | | | |
| `POST` | `/v1/chat` | Yes | Send a message in an item-scoped chat thread |
| `GET` | `/v1/chat/history` | Yes | Fetch conversation history for a specific listing |
| **Campus Wishlist Board** | | | |
| `GET` | `/v1/wishlist` | No | Get active student wishlist requests |
| `POST` | `/v1/wishlist` | Yes | Post a new item request to the wishlist board |
| `DELETE` | `/v1/wishlist/{id}` | Yes | Fulfill or remove a wishlist request |
| **Goodwill Points & Gamification** | | | |
| `GET` | `/v1/points/me` | Yes | Get student goodwill points balance and badges |
| `POST` | `/v1/points/award` | Yes | Award goodwill points for completed donations |
| **Community Moderation** | | | |
| `POST` | `/v1/listings/{id}/report` | Yes | Report an inappropriate listing (triggers auto-concealment) |

---

## 🏛️ Architecture & Zero-Cost Principles

- **Zero Cloud Costs**: All domain services utilize thread-safe in-memory stores protected by `sync.RWMutex`, allowing the entire application to be demonstrated and run without external cloud database charges or API keys.
- **Strict Separation of Concerns**: Clean Go backend architecture with segregated domain packages (`auth`, `listings`, `requests`, `notifications`, `chat`, `wishlist`, `points`, `moderation`).
- **Android MVVM + ViewBinding**: Safe view references without runtime `NullPointerExceptions` or boilerplate `findViewById`, combined with Repository pattern and LiveData observables.
- **Offline Resilience**: Curated vector drawable placeholders prevent broken image cards when internet connectivity is restricted.

---

## 📚 In-Depth Documentation

- [Detailed Architecture & API Specification](docs/architecture-and-api.md) - System topology, concurrency models, ER schemas, and sequence diagrams.
- [Viva Defense Handbook & Demo Script](docs/viva-defense-guide.md) - Comprehensive viva Q&A, trade-off defenses, and a 5-minute live demo guide.

---

## 📄 License

This project is licensed under the MIT License.
