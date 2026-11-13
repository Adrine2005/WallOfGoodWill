# Tasks

## 1. Database Dependencies & PostgreSQL Schema Migrations

- [x] 1.1 Add `github.com/jackc/pgx/v5` to `server/go.mod` and verify `go mod tidy` resolves dependencies cleanly in `server/`.
- [x] 1.2 Create `server/migrations/001_initial_schema.sql` containing idempotent DDL definitions (`CREATE TABLE IF NOT EXISTS`) and indexes for `users`, `campus_invites`, `listings`, `listing_photos`, `wishlist_items`, `donation_requests`, `conversations`, `messages`, `notifications`, `user_karma`, `user_badges`, and `moderation_reports`.
- [x] 1.3 Implement `server/internal/db/db.go` with `pgxpool` connection management, connection ping, and SQL migration execution on startup.

## 2. PostgreSQL Repositories with Dual-Mode Fallback

- [x] 2.1 Update `server/internal/config/config.go` and `server/.env.example` to parse `DATABASE_URL`, `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, and `SUPABASE_STORAGE_BUCKET`.
- [x] 2.2 Implement PostgreSQL repositories for listings, users, and invites in `server/internal/db/`, maintaining dual-mode persistence that falls back gracefully to in-memory storage when `DATABASE_URL` is not set.
- [x] 2.3 Wire database initialization and repository selection into `server/cmd/api/main.go` and verify unit tests pass with `cd server && go test ./...`.

## 3. Supabase Cloud Storage Client & Upload Handler

- [x] 3.1 Implement `server/internal/storage/supabase.go` with HTTP upload methods for the Supabase Storage REST API (`/storage/v1/object/`) returning public CDN URLs.
- [x] 3.2 Update `server/internal/handlers/upload.go` to dispatch uploads to Supabase Storage when credentials are provided and fall back to local disk storage otherwise.

## 4. Containerization, Cloud Deployment & Validation

- [x] 4.1 Create multi-stage `server/Dockerfile` and `server/render.yaml` deployment blueprint for free cloud deployment on Render.com or Fly.io.
- [x] 4.2 Compile the server with `cd server && go build -o bin/server ./cmd/api` and execute verification requests against `/health` and `/v1/upload`.

