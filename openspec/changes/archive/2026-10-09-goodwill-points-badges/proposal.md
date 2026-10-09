# Proposal

## Why

Hostel students donating useful items for free embody the core mission of Wall of Goodwill. To encourage donation rather than disposal, an in-app gamification system awarding **Goodwill Points**, donor tiers, and verifiable campus badges provides positive social proof, builds trustworthiness, and promotes campus-wide sustainability.

## What Changes

- Add backend `points` service with thread-safe karma tracking, badge computation, and campus leaderboard.
- Hook into `requests.Service` and `listings.Service` so successful donations immediately award +50 Goodwill Points and evaluate badge achievements.
- Add REST endpoints:
  - `GET /v1/points/me`: Retrieve authenticated student's points, total donations, current badge tier, and next milestone.
  - `GET /v1/points/leaderboard`: Retrieve top campus donors ranked by goodwill karma.
- Add Android client gamification features:
  - Profile screen Goodwill Points card with points counter and tier progress bar.
  - Badge achievement row on Profile.
  - `LeaderboardActivity`: Top campus contributors leaderboard.
  - `PointsRepository`, `PointsViewModel`, and DTOs.

## Capabilities

### New Capabilities
- `goodwill-gamification`: Tracks student donation milestones, awards points, manages badge tiers, and renders campus donation leaderboards.

### Modified Capabilities
None.

## Impact

- `server/internal/points`: New domain package and service.
- `server/internal/handlers`: HTTP handlers for points and leaderboard endpoints.
- `server/internal/requests`: Hook point award on request acceptance.
- `server/cmd/api/main.go`: Route group `/v1/points` registration.
- `android/app/src/main/java/com/goodwill/app/data/`: ApiService endpoints, DTOs, and repository.
- `android/app/src/main/java/com/goodwill/app/ui/points/`: Leaderboard activity, adapter, and ViewModel.
- `android/app/src/main/java/com/goodwill/app/ui/profile/`: Goodwill Points card and badge display.
