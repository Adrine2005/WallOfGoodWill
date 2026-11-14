# Design

## Context

See proposal.md for motivation and context. The Android client uploads images to the Go backend (`/v1/upload`), which uploads to Supabase Cloud Storage and returns public URLs. Uploads were slow due to uncompressed raw images (>5MB) and remote rendering failed in the emulator because Glide's standard `HttpURLConnection` transport could not resolve the cloud storage domain over the host's IPv6 DNS network.

## Goals / Non-Goals

**Goals:**
- Shrink upload file sizes by ~98% via client-side downscaling (max 1280px) and JPEG compression (quality ~82%) before streaming.
- Accelerate single image upload from ~25s to <300ms.
- Connect Glide's HTTP loader to the application's resilient `OkHttpClient` configured with fallback DNS for Supabase storage domains (`172.64.149.246`, `104.18.38.10`).
- Add a direct "Delete Listing" action with confirmation dialog to `ListingDetailActivity` for active item owners.
- Provide a backend fallback endpoint `/v1/upload/{filename}` in the Go server for retrieving local/cloud media.

**Non-Goals:**
- Video or animated GIF uploads (marketplace listings only support photos).
- Multi-region bucket replication.

## Decisions

### 1. Client-Side Downscaling via Android Bitmap Pipeline vs Server-Side Resizing
- **Decision:** Downscale and compress on the client in a background executor thread before initiating the HTTP upload request.
- **Rationale:** Transmitting 10MB of cellular/WiFi data just to have the server resize it wastes student bandwidth and takes 20-30s. Downscaling client-side to max 1280px dimension at 82% quality produces a crisp ~150KB JPEG that uploads almost instantaneously.
- **Alternatives Considered:** Server-side resizing only (rejected because the network latency bottleneck is during client upload).

### 2. Custom OkHttp Dns Fallback vs System Hosts File Editing
- **Decision:** Extend OkHttp's `Dns` interface in `ApiClient` to resolve both `wallofgoodwill.onrender.com` and `*.supabase.co` to known Cloudflare edge IPs when system DNS fails with `UnknownHostException`.
- **Rationale:** Modifying system hosts files on physical devices or production emulator builds requires root permissions (`adbd cannot run as root in production builds`). Application-level DNS fallback is 100% portable, requires zero root privileges, and preserves full TLS certificate validation via SNI.
- **Alternatives Considered:** Relying on default DNS (rejected because IPv6 DNS on host Macs breaks emulator DNS lookup).

### 3. Glide Integration with OkHttp
- **Decision:** Register a Glide `OkHttpUrlLoader` factory so all Glide network calls (`ListingsAdapter`, `ListingDetailActivity`, `CreateListingActivity`, `MyListingsAdapter`) use the resilient OkHttpClient.
- **Rationale:** Unifies all networking in the app under a single pooled, logged, and DNS-resilient HTTP client.

### 4. Delete Action Placement
- **Decision:** Add a "Delete" button in `activity_listing_detail.xml` visible only when `currentUser.uid.equals(listing.getSellerId())`.
- **Rationale:** Currently sellers could only delete items from Profile > My Listings, creating friction when viewing an item from the main feed.

## Risks / Trade-offs

- **[Risk]** Cloudflare IP rotation could outdate hardcoded fallback IPs.
  - **Mitigation:** Fallback IPs are only queried when system DNS throws `UnknownHostException`. If system DNS works, live DNS responses are always used first.
- **[Risk]** Bitmap memory allocation out-of-memory (OOM) on low-end devices.
  - **Mitigation:** Use `BitmapFactory.Options.inSampleSize` to subsample during decoding from `InputStream`, avoiding allocating full 4000x3000 bitmaps in memory.

