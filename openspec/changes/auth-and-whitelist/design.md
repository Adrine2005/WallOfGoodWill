# Design

## Context

See `proposal.md` for background and motivation. The repository currently has foundational scaffolding in `server/` (Go Chi router) and `android/` (Material 3 app shell). This design details the student onboarding, authentication, and time-limited QR verification architecture.

## Goals / Non-Goals

**Goals:**
- Provide Go backend handlers for student profile registration (`POST /v1/auth/register`), QR code verification (`POST /v1/auth/verify-qr`), and admin invite generation (`POST /v1/admin/qr/generate`).
- Introduce an authentication token verifier interface in Go with both a development mock verifier and a Firebase Admin verifier for zero-configuration local development.
- Build clean Android authentication screens: Splash, Login, Registration (with hostel block dropdown), and Campus QR Gateway.
- Implement Android `AuthRepository`, `AuthViewModel`, and an OkHttp `AuthInterceptor` for Bearer token injection.
- Support both post-registration QR scanning and optional instant scanning during sign-up.

**Non-Goals:**
- Handling listings, chat, or image uploads (deferred to subsequent changes).
- Enforcing payment or UPI deep-links.

## Decisions

### 1. Time-Limited QR Code Invitations instead of Static Email Whitelists
- **Rationale**: Physical QR posters placed on hostel notice boards guarantee physical campus presence, removing the privacy, administrative, and maintenance overhead of collecting and synchronizing university email rosters.
- **Alternatives considered**: Pre-compiled CSV whitelist in Firestore. Rejected because getting university rosters is bureaucratic and difficult to maintain across semester graduations and admissions.

### 2. Pluggable Go TokenVerifier Interface
- **Rationale**: Defines a `TokenVerifier` interface with a `MockVerifier` (for local dev and testing) and `FirebaseVerifier` (for production). The server starts and passes all tests without requiring `serviceAccountKey.json`.
- **Alternatives considered**: Requiring live Firebase Admin credentials on startup. Rejected because it blocks local testing and prototyping.

### 3. Google Code Scanner & Manual Entry Fallback in Android
- **Rationale**: Android's `play-services-code-scanner` scans QR codes via a lightweight Google Play Services bottom sheet without requiring dangerous camera permissions. A manual text code entry is also provided as a fallback.
- **Alternatives considered**: Full CameraX integration with custom overlay. Rejected because Google Code Scanner provides a cleaner user experience with zero permission dialogs.

### 4. SharedPreferences / EncryptedSharedPreferences Token Store
- **Rationale**: Caches the active auth token and user profile (`uid`, `name`, `hostelBlock`, `verified`) locally to enable instant offline app launches and route decisions in `SplashActivity`.

## Risks / Trade-offs

- **[Risk] QR code photo forwarded to outsiders via messaging apps** → **Mitigation**: Admin generates time-limited codes (e.g. 24–48 hour validity) with maximum usage counters.
- **[Risk] Device camera unavailable or permission denied** → **Mitigation**: Support manual alphanumeric invite code entry beneath the scanner button.

## Migration Plan

1. Implement Go `internal/auth/verifier.go`, `internal/invites/service.go`, and handler routes.
2. Verify Go endpoints with unit and integration tests.
3. Build Android authentication layouts, `AuthRepository`, `AuthViewModel`, and activities.
4. Verify end-to-end registration and QR verification with curl and mock tests.
