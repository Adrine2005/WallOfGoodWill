# Tasks

## 1. Monorepo and Git Setup

- [ ] 1.1 Initialize Git repository in the workspace root and verify with `git status`
- [ ] 1.2 Create root `.gitignore` covering Go, Android, Gradle, IDEs, and environment files (`.env`, `serviceAccountKey.json`), and verify ignored files via `git status`

## 2. Go Backend Service Scaffolding

- [ ] 2.1 Initialize Go module in `server/` and add Chi router dependency in `server/go.mod`
- [ ] 2.2 Implement server entrypoint `server/cmd/api/main.go` and `GET /health` route returning JSON status
- [ ] 2.3 Add unit test for the health check handler and verify with `go test ./...` in `server/`
- [ ] 2.4 Start the backend server locally and verify live HTTP response using `curl http://localhost:8080/health`

## 3. Android Project Scaffolding

- [ ] 3.1 Create Android project directory structure in `android/` with `settings.gradle.kts`, root `build.gradle.kts`, and `gradle/libs.versions.toml`
- [ ] 3.2 Configure `android/app/build.gradle.kts` targeting API 34 with Java 17 compatibility and Material 3 dependencies
- [ ] 3.3 Create `AndroidManifest.xml` and baseline `MainActivity.java` in `android/app/src/main/`
- [ ] 3.4 Verify Android project structure and Gradle build configuration readiness

## 4. Documentation and Scaffolding Verification

- [ ] 4.1 Create project `README.md` documenting monorepo layout and instructions to run both `server` and `android`
- [ ] 4.2 Run final status check across the repository to verify all generated files are clean and properly tracked
