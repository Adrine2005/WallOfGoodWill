# Proposal

## Why

Currently, the Wall of Goodwill Go backend service stores all data in volatile in-memory data structures and writes image uploads to a local filesystem folder (`server/uploads/`). Whenever the server restarts or deploys to cloud hosting platforms, all registered student profiles, listings, campus QR codes, chats, karma points, and photos are lost. Integrating Supabase delivers a free, production-ready PostgreSQL relational database and cloud object storage (Supabase Storage) for image uploads, allowing the server to be hosted 24/7 on free container platforms (such as Render.com or Fly.io) without losing student data.

## What Changes

- **PostgreSQL Database Integration**: Integrate PostgreSQL driver (`github.com/jackc/pgx/v5`) into the Go backend, connecting via standard `DATABASE_URL`.
- **Database Schema & Migrations**: Create SQL migration scripts covering all core tables: `users`, `campus_invites`, `listings`, `listing_photos`, `wishlist_items`, `donation_requests`, `conversations`, `messages`, `notifications`, `user_points`, `user_badges`, and `moderation_reports`.
- **Dual-Mode Repository Architecture**: Support both PostgreSQL and in-memory persistence: if `DATABASE_URL` is set, the server operates on PostgreSQL; if omitted, it falls back to in-memory mode for offline development and continuous integration unit tests.
- **Supabase Cloud Image Storage**: Update `POST /v1/upload` in the Go backend to upload multipart photos directly into a Supabase Storage bucket (`goodwill-images`) and return public CDN URLs when `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` (or storage credentials) are configured, falling back to local disk storage when running locally.
- **Containerized Free Cloud Deployment**: Provide a production-grade multi-stage `Dockerfile` and `render.yaml` configuration for free hosting on Render.com or Fly.io.
- **Client Cloud Endpoint Readiness**: Ensure Android client `ApiClient.java` handles cloud HTTPS base URLs smoothly while maintaining emulator reverse-proxy support during development.

## Capabilities

### Modified Capabilities
- `backend-service`: Add requirements for PostgreSQL relational database persistence, Supabase cloud image bucket storage, and containerized deployment readiness.

## Impact

- **Backend**: `server/go.mod`, `server/cmd/api/main.go`, `server/internal/config`, `server/internal/db`, `server/internal/handlers/upload.go`.
- **Infrastructure**: New `server/Dockerfile`, `server/render.yaml`, `server/migrations/001_initial_schema.sql`.
- **Android Client**: Compatible with existing REST payloads; photos load from public Supabase CDN URLs.

