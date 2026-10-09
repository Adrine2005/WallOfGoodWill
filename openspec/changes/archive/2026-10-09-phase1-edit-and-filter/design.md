# Design

## Context

See `proposal.md` for motivation. The backend currently supports `GET`, `POST`, `PUT /{id}/status`, and `DELETE /{id}` for listings. The Android client provides feed browsing and a seller management screen in `ProfileActivity`.

## Goals / Non-Goals

**Goals:**
- Provide a `PATCH /v1/listings/{id}` endpoint allowing authenticated owners to edit title, description, price, condition, or category.
- Enable sellers to launch an Edit mode from "My Listings" in `ProfileActivity`.
- Provide Hostel Block and Price range filtering options on the Android home feed.

**Non-Goals:**
- Cloudinary photo uploading overhaul (maintains existing photos during edit).
- Real-time Firestore direct synchronization (Phase 2 scope).

## Decisions

1. **Reusing CreateListingActivity in Edit Mode**:
   Instead of introducing duplicate layout code, `CreateListingActivity` will support an `EXTRA_EDIT_LISTING` bundle. When passed, it pre-fills the form, updates button text to "Save Changes", and executes `PATCH /v1/listings/{id}` via `ListingsViewModel`.

2. **Hostel Block and Price Filter UI**:
   Introduce a filter bottom sheet / dialog accessible via a dedicated filter icon button on the search bar in `MainActivity`, letting students filter by hostel block and price constraints.

## Risks / Trade-offs

- [Risk: Unauthorized listing modifications] → Mitigation: Check seller ID against JWT/session claims in Go `UpdateListing` handler and return 403 Forbidden on mismatch.
