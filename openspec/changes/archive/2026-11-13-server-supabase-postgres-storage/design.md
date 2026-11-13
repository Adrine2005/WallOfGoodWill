# Design

## Context

See `proposal.md` for background. The Wall of Goodwill backend is a Go application (`chi` HTTP router) that currently keeps domain models in memory and stores image files locally on disk in `server/uploads/`. Supabase provides a managed PostgreSQL instance and an S3-compatible Storage bucket. This design establishes how the Go server connects to Supabase PostgreSQL, uploads media to Supabase Storage, and packages for cloud deployment.

## Goals / Non-Goals

**Goals:**
- Provide a unified SQL schema (`migrations/001_initial_schema.sql`) compatible with Supabase PostgreSQL.
- Implement PostgreSQL connection pooling using `github.com/jackc/pgx/v5/pgxpool`.
- Maintain a dual-mode persistence architecture: automatically connect to PostgreSQL when `DATABASE_URL` is set, and fall back to in-memory maps when omitted.
- Implement a lightweight HTTP client for Supabase Storage REST API (`/storage/v1/object/`) to store images and return public CDN URLs without bulky AWS SDK dependencies.
- Provide a multi-stage `Dockerfile` and `render.yaml` for one-click deployment on free hosting platforms.

**Non-Goals:**
- Modifying the Android client REST API contract or endpoints.
- Introducing a bulky ORM (like GORM) when idiomatic standard SQL with `pgx` provides better transparency and control.

## Decisions

### Decision 1: Database Driver & Connection Management
- **Choice**: Use `github.com/jackc/pgx/v5/pgxpool`.
- **Rationale**: `pgx` is the highest-performance, most widely adopted PostgreSQL driver in the Go ecosystem. It provides native support for connection pooling, context cancellation, and prepared statements.
- **Alternatives considered**: `database/sql` with `lib/pq` (maintenance-only status, slower), `gorm` (heavy abstraction layer unnecessary for this schema size).

### Decision 2: Schema Migrations & Table Layout
- **Choice**: Deliver a single consolidated `migrations/001_initial_schema.sql` file executed automatically upon server startup when connected to Postgres.
- **Tables**:
  - `users`: `id`, `name`, `email`, `password_hash`, `role`, `hostel_block`, `room_no`, `phone`, `verified`, `verified_via_code`, `created_at`
  - `campus_invites`: `code`, `label`, `expires_at`, `max_uses`, `used_count`, `is_active`, `created_at`
  - `listings`: `id`, `seller_id`, `seller_name`, `hostel_block`, `room_no`, `title`, `description`, `category`, `type`, `price`, `condition`, `status`, `created_at`, `updated_at`
  - `listing_photos`: `listing_id`, `photo_url`, `display_order`
  - `wishlist_items`: `id`, `user_id`, `user_name`, `title`, `description`, `category`, `fulfilled`, `created_at`
  - `donation_requests`: `id`, `listing_id`, `requester_id`, `requester_name`, `status`, `note`, `created_at`
  - `conversations`: `id`, `listing_id`, `buyer_id`, `seller_id`, `last_message`, `updated_at`
  - `messages`: `id`, `conversation_id`, `sender_id`, `text`, `sent_at`
  - `notifications`: `id`, `user_id`, `type`, `title`, `message`, `read`, `created_at`
  - `user_karma`: `user_id`, `points`, `tier_name`, `donation_count`
  - `user_badges`: `user_id`, `badge_id`, `name`, `icon`, `awarded_at`
  - `moderation_reports`: `id`, `listing_id`, `reporter_id`, `reason`, `status`, `created_at`

### Decision 3: Supabase Storage Integration
- **Choice**: Use Supabase's native Storage REST API directly via Go standard `net/http`.
- **Flow**:
  1. Client sends multipart file to `POST /v1/upload`.
  2. Server generates unique filename: `{timestamp}_{randomHex}.{ext}`.
  3. Server dispatches `POST {SUPABASE_URL}/storage/v1/object/goodwill-images/{filename}` with header `Authorization: Bearer {SUPABASE_SERVICE_ROLE_KEY}`.
  4. Returns public URL: `{SUPABASE_URL}/storage/v1/object/public/goodwill-images/{filename}`.
  5. If `SUPABASE_URL` is omitted, saves to local `server/uploads/` folder and returns local URL.
- **Rationale**: Direct REST API call requires zero third-party dependencies, keeping the Go binary tiny and robust.

### Decision 4: Container Packaging
- **Choice**: Multi-stage `Dockerfile` with `golang:1.24-alpine` build stage and `alpine:latest` runtime stage with CA certificates.
- **Result**: Ultra-light container (< 25MB) deployable on Render, Fly.io, or any container platform.

## Risks / Trade-offs

- **[Risk] Free Render Web Service Idle Sleep**: Free Render instances spin down after 15 minutes of inactivity, causing a 30-50 second cold start on the first subsequent request.
  - **Mitigation**: Standard HTTP timeouts in Android Retrofit are set to 30s. A free monitor (like Cron / UptimeRobot) can ping `/health` periodically to maintain warm status.
- **[Risk] Supabase Free Tier Pausing**: Supabase projects pause after 7 days of zero database queries.
  - **Mitigation**: Documented in setup instructions; unpausing takes 1 click in the Supabase dashboard.

