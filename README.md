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

### 2. Building the Android Application

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
