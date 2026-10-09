# Proposal

## Why

Students frequently need specific academic or hostel items (such as specialized textbooks, lab coats, drawing kits, induction kettles, or bicycle pumps) that may not currently appear on the active marketplace feed. A dedicated campus "Looking For" Wishlist Board allows students to broadcast what they need so dorm peers with unused items can step forward to donate or sell them.

## What Changes

- Add backend `wishlist` service with thread-safe in-memory store supporting item requests with category, status, and campus location.
- Add REST endpoints:
  - `POST /v1/wishlist`: Post a new "looking for" item request.
  - `GET /v1/wishlist`: List active campus wishlist requests with optional category and search filters.
  - `GET /v1/wishlist/my`: List the authenticated student's posted wishlist items.
  - `DELETE /v1/wishlist/{id}`: Delete or mark a wishlist request as fulfilled.
- Add Android client Wishlist features:
  - `WishlistActivity`: Feed of campus item requests with search bar and category filters.
  - `CreateWishlistBottomSheetDialogFragment`: Form for posting an item request.
  - `WishlistAdapter`: Item cards displaying what is sought, requester details, and "I Can Help / Chat" contact button.
  - Entry points in `MainActivity` (header button / navigation) and `ProfileActivity`.
  - `WishlistRepository`, `WishlistViewModel`, and DTOs.

## Capabilities

### New Capabilities
- `wishlist-board`: Enables authenticated campus students to broadcast items they are looking for and enables peers to discover and fulfill those needs.

### Modified Capabilities
None.

## Impact

- `server/internal/wishlist`: New domain package and service.
- `server/internal/handlers`: HTTP handlers for wishlist endpoints.
- `server/cmd/api/main.go`: Route group `/v1/wishlist` registration.
- `android/app/src/main/java/com/goodwill/app/data/`: Retrofit endpoints, DTOs, and repository.
- `android/app/src/main/java/com/goodwill/app/ui/wishlist/`: Activity, bottom sheet, adapter, and ViewModel.
