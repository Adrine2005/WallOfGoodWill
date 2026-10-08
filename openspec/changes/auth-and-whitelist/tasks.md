# Tasks

## 1. Backend Pluggable Auth & QR Verification

- [x] 1.1 Implement `TokenVerifier` interface and `MockVerifier` in `server/internal/auth/verifier.go` and verify with unit tests
- [x] 1.2 Implement QR invitation model and memory/service store in `server/internal/invites/service.go` and verify expiration logic
- [x] 1.3 Implement `POST /v1/auth/register` and `POST /v1/auth/verify-qr` HTTP handlers in `server/internal/handlers/auth.go`
- [x] 1.4 Implement `POST /v1/admin/qr/generate` HTTP handler in `server/internal/handlers/admin.go`
- [x] 1.5 Add test suite for registration and QR code verification lifecycle in `server/internal/handlers/auth_test.go` and verify with `go test ./...`

## 2. Android Client Networking & Data Layer

- [x] 2.1 Add Retrofit, Gson converter, and OkHttp dependencies to `android/gradle/libs.versions.toml` and `android/app/build.gradle.kts`
- [x] 2.2 Implement `ApiService`, `AuthInterceptor`, and request/response DTOs in `android/app/src/main/java/com/goodwill/app/data/remote/`
- [x] 2.3 Implement `AuthRepository` and local session preferences in `android/app/src/main/java/com/goodwill/app/data/repository/`
- [x] 2.4 Implement `AuthViewModel` with LiveData state for login, registration, and QR verification

## 3. Android Authentication UI & Campus Gateway

- [x] 3.1 Create `SplashActivity` and layout with session check routing to login, verification gateway, or home
- [x] 3.2 Create `LoginActivity` layout and activity with email/password validation
- [x] 3.3 Create `RegisterActivity` layout and activity with hostel block dropdown, room number, and optional instant QR code entry
- [x] 3.4 Create `VerifyQrActivity` layout and activity with scanner trigger button and manual code input fallback

## 4. Verification and Documentation

- [x] 4.1 Update project `README.md` with QR verification API endpoints and testing guide
- [x] 4.2 Run repository status checks to verify all code passes tests and working tree is clean
