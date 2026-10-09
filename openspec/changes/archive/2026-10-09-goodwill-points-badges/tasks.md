# Tasks

## 1. Backend Points Domain & Handlers

- [x] 1.1 Implement `server/internal/points/service.go` with thread-safe score models, badges computation, leaderboard rankings, and seed data; verify with Go build.
- [x] 1.2 Implement HTTP handlers in `server/internal/handlers/points.go`, mount routes in `server/cmd/api/main.go`, and hook point awards in `requests.Service`; verify with handler compilation.
- [x] 1.3 Add unit and integration tests in `server/internal/handlers/points_test.go` covering point awards, badge tier advancement, and leaderboard ranking; verify with `/usr/local/go/bin/go test -v ./...`.

## 2. Android Points Data Layer

- [x] 2.1 Add Java DTOs (`UserPointsDto`, `LeaderboardEntryDto`) and update `ApiService` with points endpoints; verify by inspecting Java models.
- [x] 2.2 Implement `PointsRepository` and `PointsViewModel` for retrieving user points and campus leaderboard; verify classes compile cleanly.

## 3. Android Gamification UI & Integration

- [x] 3.1 Create gamification layout files (`activity_leaderboard.xml`, `item_leaderboard.xml`) and `LeaderboardAdapter`; verify layout resource files are valid XML.
- [x] 3.2 Implement `LeaderboardActivity.java`, register in `AndroidManifest.xml`, and add Goodwill Points showcase card & leaderboard button in `ProfileActivity`; verify with activity wiring.

## 4. Verification and Specification Sync

- [x] 4.1 Run full backend test suite (`/usr/local/go/bin/go test -v ./...`) and validate change with `openspec validate goodwill-points-badges --strict`; verify all checks pass.
- [x] 4.2 Sync main specification to `openspec/specs/goodwill-gamification/spec.md`, archive change, and push to GitHub; verify clean git status.
