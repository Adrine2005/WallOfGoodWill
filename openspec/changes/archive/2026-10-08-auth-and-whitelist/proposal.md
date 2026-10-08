# Proposal

## Why

Wall of Goodwill is a campus-only marketplace that requires ensuring only verified university hostel students can access and interact with listings. To avoid the administrative burden and privacy hurdles of maintaining pre-compiled student email rosters, access is granted via time-limited QR code invitations posted on hostel notice boards. Combining account registration with physical QR verification ensures authentic campus presence with minimal friction.

## What Changes

- Implement time-limited QR invitation generation and verification endpoints in the Go backend (`POST /v1/auth/verify-qr` and `POST /v1/admin/qr/generate`).
- Support pluggable token verification on the Go backend (mock verifier for local dev without requiring live Firebase keys, and Firebase Admin SDK when configured).
- Build Android authentication UI flows including Splash screen (auth & verification check), Login, and Registration (with hostel block selector).
- Implement Android Campus Gateway screen allowing students to scan the hostel notice board QR code (with support for optional instant scanning during sign-up).
- Provide Android `AuthRepository`, `AuthViewModel`, and HTTP authentication interceptor adding the Bearer token to API requests.

## Capabilities

### New Capabilities
- `user-auth`: Covers client authentication UI flows (Splash, Login, Register), hostel block selection, session state management, and authentication token injection.
- `campus-verification`: Covers backend time-limited QR invitation generation, verification API, and client QR scanning integration for student account activation.

### Modified Capabilities
<!-- None: app-shell and backend-service requirements remain intact. -->

## Impact

- **Backend APIs**: Exposes `POST /v1/auth/register`, `POST /v1/auth/verify-qr`, and `POST /v1/admin/qr/generate`.
- **Android UI**: Adds `SplashActivity`, `LoginActivity`, `RegisterActivity`, and `VerifyQrActivity` (or fragments) to `android/app/`.
- **Dependencies**: Introduces Retrofit, OkHttp, and Google Code Scanner / ZXing dependencies in Android, plus token verification and invite models in Go.
- **Breaking Changes**: None.
