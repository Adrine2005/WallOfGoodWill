# Design

## Context

Wall of Goodwill allows campus students to exchange hostel items. To preserve hostel trust and safety, students need an in-app mechanism to flag listings violating university rules or safety guidelines without incurring database or third-party service costs.

## Goals / Non-Goals

**Goals:**
- Provide thread-safe in-memory reporting and moderation tracking in Go.
- Offer clean REST endpoints for report creation and listing audit.
- Support automated hiding of listings upon reaching flag thresholds.
- Implement an intuitive Material 3 Bottom Sheet Dialog in Android `ListingDetailActivity` for report submissions.

**Non-Goals:**
- Complex ML-based image content filtering (outside zero-cost offline scope).
- Automated user bans or punitive disciplinary actions.

## Decisions

1. **In-Memory Thread-Safe Moderation Store**:
   - `moderation.Service` stores reports keyed by listing ID and report ID using `sync.RWMutex`.
   - Rationale: Consistent with existing domain design (`requests`, `notifications`, `chat`, `wishlist`, `points`).

2. **Listing Status Hiding**:
   - When a listing reaches 2 or more reports, or is explicitly flagged, `listingsService.UpdateStatus(listingID, sellerID, listings.StatusHidden)` is called or marked hidden so `ListListings` filters it out of the public feed.

3. **Android BottomSheetDialogFragment UI**:
   - `ReportBottomSheetDialogFragment` presents structured violation reasons:
     - 🚫 Prohibited Item (Weapons, Alcohol, Restricted lab gear)
     - ⚠️ Inappropriate / Offensive Description
     - 🛑 Spam or Commercial Advertisement
     - 📝 Other Campus Violation
   - Includes optional comment field and instant submission.

## Risks / Trade-offs

- **[Risk] Abuse of reporting to troll legitimate student sellers** → Mitigation: Record reporter ID; require valid authenticated session; threshold requires distinct reports before automated concealment.
