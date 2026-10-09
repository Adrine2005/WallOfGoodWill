# Proposal

## Why

To fully complete Phase 1 MVP requirements of the "Wall of Goodwill" project plan, students require the ability to edit existing listings they have posted (correcting prices, descriptions, or conditions) and apply hostel block and price range filters directly from the Android marketplace feed.

## What Changes

- Add Go backend endpoint `PATCH /v1/listings/{id}` allowing owners to update mutable listing attributes (title, description, price, condition, category).
- Add backend support and client integration for price range filtering (`minPrice`, `maxPrice`) and hostel block filtering.
- Add an "Edit" action to "My Listings" screen and implement an `EditListingActivity` (or reuse `CreateListingActivity` in edit mode).
- Add a Hostel Block and Price filter bottom sheet or dialog to `MainActivity` on Android.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `item-listings`: Adds listing edit capability for owners and advanced feed filtering by hostel block and price range.

## Impact

- `server/internal/listings`: Add `UpdateListing` service method.
- `server/internal/handlers`: Add `UpdateListing` HTTP handler mounted at `PATCH /v1/listings/{id}`.
- `android/app/src/main/java/com/goodwill/app/data/remote/ApiService.java`: Add `@PATCH("v1/listings/{id}")`.
- `android/app/src/main/java/com/goodwill/app/ui/listings`: Support editing listings and filtering feed by hostel block.
