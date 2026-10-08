# Proposal

## Why

The Wall of Goodwill project requires an established repository structure and foundational build configuration before core features (authentication, listings, chat) can be developed. Setting up a monorepo containing both the Go backend service and the Android Java application with standard build tools ensures rapid iteration, consistent environment variables, and reliable health diagnostics.

## What Changes

- Initialize Git repository at workspace root with comprehensive `.gitignore` rules for Go, Android, Gradle, and sensitive configuration files.
- Scaffold the Go backend service in `server/` with Go module initialization, Chi router, environment configuration structure, and a `GET /health` endpoint for liveness and wake-up verification.
- Scaffold the Android application in `android/` with Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`), Android 14 (API 34) target compatibility, and a base Android Java Activity.
- Establish baseline developer commands and scripts for running the server and building the Android application.

## Capabilities

### New Capabilities
- `backend-service`: Covers the HTTP API server foundation, runtime configuration loading, and health probe (`/health`) endpoint for server liveness.
- `app-shell`: Covers the Android client application container, build configuration, and base Activity runtime shell.

### Modified Capabilities
<!-- None: This is the initial capability setup for the project. -->

## Impact

- **Filesystem**: Adds `server/` and `android/` subdirectories alongside root `.gitignore`.
- **APIs**: Exposes `GET /health` on the backend service returning JSON status.
- **Dependencies**: Introduces Go standard library / Chi router dependencies and Android Gradle Plugin / Material 3 dependencies.
- **Systems**: No breaking changes to existing systems.
