# Proposal

## Why

In a hostel-centric campus donation and student exchange platform, community safety, trust, and zero-spam are paramount. Students occasionally encounter prohibited items (e.g., restricted campus equipment, contraband, commercial spam) or inappropriate descriptions. Providing a zero-friction, respectful reporting mechanism empowers students to flag listings and ensures automated/administrative safety controls to protect the student community.

## What Changes

- Add in-memory moderation service tracking listing reports, flag thresholds, and hidden statuses.
- Expose RESTful endpoints:
  - `POST /v1/listings/{id}/reports`: Submit a report for a listing with reason and comment.
  - `GET /v1/listings/{id}/reports`: Retrieve report audit entries (for moderators/author inquiry).
- When a listing reaches the threshold or is flagged, it is automatically marked hidden from search and feed results.
- Add Android reporting dialog bottom sheet accessible from `ListingDetailActivity` with Material 3 radio chips for reason selection.
- Add reporting confirmation Snackbar and dynamic state updates.

## Capabilities

### New Capabilities
- `listing-moderation`: Allows campus students to report listings for safety or guideline violations, tracks report logs, and automatically moderates item visibility.

### Modified Capabilities
<!-- None -->

## Impact

- **Server**: New `server/internal/moderation` domain package, `moderationHandler` in `server/internal/handlers/moderation.go`, and router wiring in `server/cmd/api/main.go`. Listing feed query can filter out hidden items.
- **Android App**: New DTOs (`ReportListingRequest`, `ReportDto`), new endpoints in `ApiService`, `ModerationRepository`, and `ReportBottomSheetDialogFragment` integrated into `ListingDetailActivity`.
