# Design

## Context

See `proposal.md` for background and motivation. The developer environment currently has Go 1.27.1, Android SDK (API 34), and command-line tools installed. The project repository is at the initial phase with no git history or source code directories.

## Goals / Non-Goals

**Goals:**
- Establish a clean monorepo structure containing `server/` and `android/`.
- Provide a Git setup with a comprehensive `.gitignore` preventing secrets and build artifacts from leaking into version control.
- Scaffold the Go server with `chi` router and a working `GET /health` handler.
- Scaffold the Android app using Java, Material 3, and Gradle Kotlin DSL with `libs.versions.toml`.
- Provide verifiable run and build checks for both components.

**Non-Goals:**
- Implementing Firebase Authentication, custom claims, or Firestore rules (scoped to Phase 1 auth changes).
- Implementing Cloudinary signed uploads or listing APIs (scoped to Phase 1 listings changes).
- Building complex UI layouts or navigation graphs (scoped to UI changes).

## Decisions

### 1. Monorepo Layout (`server/` and `android/`)
- **Rationale**: Retaining both backend and mobile client in the same repository streamlines local development, synchronizes API contracts, and simplifies final-year academic project review and submission.
- **Alternatives considered**: Separate Git repositories. Rejected due to increased coordination overhead for a solo developer.

### 2. Lightweight Go Router (`chi` v5)
- **Rationale**: `chi` is idiomatic, lightweight, completely compatible with standard `net/http`, and ideal for composing modular middleware (such as Firebase ID token validation and CORS).
- **Alternatives considered**: Gin (more heavyweight, proprietary context), Standard library `http.ServeMux` (less ergonomic middleware chaining in Go).

### 3. Gradle Kotlin DSL with Version Catalog (`libs.versions.toml`)
- **Rationale**: Modern Android project standard that centralizes dependency versions, plugin versions, and library aliases in a type-safe format.
- **Alternatives considered**: Legacy Groovy `build.gradle`. Rejected because Gradle Kotlin DSL provides better auto-completion and aligns with modern Android guidelines.

### 4. Java 17 Compatibility & API 34 Target
- **Rationale**: Complies with the hard constraint in the project plan (Java language for Android) while utilizing Android 14 (API 34) capabilities and modern Java 17 language features.
- **Alternatives considered**: Kotlin. Rejected per project constraints.

## Risks / Trade-offs

- **[Risk] Secrets exposure in repo** → **Mitigation**: Add comprehensive ignore rules for `.env`, `serviceAccountKey.json`, `keystore`, and local properties in root and subproject `.gitignore` files.
- **[Risk] Heavy initial Android build overhead** → **Mitigation**: Keep the initial app module minimal with an Empty Activity shell before pulling in Firebase BoM and Retrofit dependencies.

## Migration Plan

1. Initialize Git repository with root `.gitignore`.
2. Create and verify the Go service: `cd server && go test ./...` and `curl http://localhost:8080/health`.
3. Create and verify the Android application: `cd android && ./gradlew assembleDebug`.
