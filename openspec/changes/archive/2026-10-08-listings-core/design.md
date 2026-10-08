# Design

## Context

See `proposal.md` for background and motivation. The project currently has authenticated student accounts, campus verification via time-limited QR codes, and persistent Android session management. This design details the listings data model, Go backend service, and Android UI architecture for the campus marketplace feed and post creation flows.

## Goals / Non-Goals

**Goals:**
- Provide Go backend REST endpoints for listing creation (`POST /v1/listings`), querying with filters (`GET /v1/listings`), item details (`GET /v1/listings/{id}`), my listings (`GET /v1/listings/my`), and deletion (`DELETE /v1/listings/{id}`).
- Maintain thread-safe Go listings storage with sample campus listings pre-seeded for instant demo and testing.
- Implement Android `ListingsRepository` and `ListingsViewModel` using Retrofit and LiveData.
- Build interactive Android home feed in `MainActivity` with Material 3 cards, DONATE vs SELL badges, filter chips (by category and hostel block), and floating action button.
- Build `CreateListingActivity` with category selection, type toggle (Donate/Sell), price input, condition picker, and local photo attachment preview.
- Build `ListingDetailActivity` displaying full listing details and seller contact card.

**Non-Goals:**
- Live Cloudinary signing or S3 storage (mock image URLs and local URIs are supported for offline development).
- Real-time one-to-one chat (planned for Phase 3).
- In-app payment processing (all sales are cash/UPI in person).

## Decisions

### 1. In-Memory Thread-Safe Go Store with Seed Data
- **Rationale**: Supports complete end-to-end testing, curl verification, and UI development without requiring external cloud databases or credentials. Pre-seeded campus items (e.g., engineering textbooks, kettle, study lamp) ensure the home feed looks lively on launch.
- **Alternatives considered**: SQLite file database. Deferred to maintain parity with the memory-based invite/user services and minimize CGO requirements.

### 2. Type-Driven Pricing Rules
- **Rationale**: If `type == DONATE`, price is strictly set to 0. If `type == SELL`, price must be greater than 0. This enforces the core campus culture: donations are 100% free goodwill items.
- **Alternatives considered**: Allowing donations with nominal fees. Rejected because "Wall of Goodwill" explicitly separates gifts from commercial sales.

### 3. Material 3 Chip-Based Dynamic Filtering
- **Rationale**: Students browse listings on mobile under time constraints (e.g. looking for books in Block A). Filter chips horizontally scrollable at the top of the feed allow instant category and block filtering.
- **Alternatives considered**: Multi-step filter dialog bottom sheet. Single-click chips provide a faster, more engaging UX.

### 4. Glide Image Integration with Material Placeholders
- **Rationale**: Glide handles memory caching, downsampling, and disk caching efficiently in Android Java. When an image is missing or device is offline, a themed vector placeholder is displayed.

## Risks / Trade-offs

- **[Risk] High-resolution camera photos causing out-of-memory errors** → **Mitigation**: Scale and compress bitmap before preview; Glide handles thumbnail downsampling.
- **[Risk] Non-sellers tampering with listings** → **Mitigation**: Go backend validates `claims.UID == listing.SellerID` before permitting deletion or status updates.
