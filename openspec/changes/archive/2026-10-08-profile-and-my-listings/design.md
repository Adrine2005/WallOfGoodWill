# Design

## Context

See `proposal.md` for background and motivation. The project currently has authenticated student accounts, campus verification via time-limited QR codes, and home feed marketplace listings. This design specifies the student profile screen, "My Listings" management actions, status updates, and sign-out flow.

## Goals / Non-Goals

**Goals:**
- Provide Go backend endpoint `PUT /v1/listings/{id}/status` to update listing status (`ACTIVE`, `SOLD`, `DONATED`), strictly authorized to the item seller.
- Build Android `ProfileActivity` displaying student profile info (name, email, hostel block, room number, verification badge).
- Include "My Listings" RecyclerView in `ProfileActivity` with quick action buttons:
  - If type `SELL`: "Mark as Sold"
  - If type `DONATE`: "Mark as Donated"
  - "Delete Listing"
- Add a profile shortcut / avatar button in the `MainActivity` top header bar.
- Add a secure Sign Out action that clears local `SessionManager` preferences and navigates back to `LoginActivity`.

**Non-Goals:**
- Editing listing text or re-uploading photos (can be done by deleting and re-creating).
- Avatar image uploads to Cloudinary (uses student initial avatar).

## Decisions

### 1. Dedicated Status Update Endpoint
- **Rationale**: Exposing `PUT /v1/listings/{id}/status` allows atomic status updates without requiring the client to send the full listing payload again.
- **Alternatives considered**: Generic PUT `/v1/listings/{id}` with full body. A dedicated status endpoint is simpler, safer, and prevents unintended field modifications.

### 2. Combined Profile and My Listings View
- **Rationale**: Students typically look for their own items and account details in one cohesive place. Combining profile information with their posted items in a single scrollable activity avoids unnecessary navigation nesting.
- **Alternatives considered**: Separate "Profile" and "My Listings" activities. Combining them into `ProfileActivity` gives a much cleaner and faster mobile experience.

### 3. Immediate Optimistic UI Updates on Status Changes
- **Rationale**: When a student marks an item as "Sold" or "Donated", updating the local list item immediately provides instantaneous feedback.

## Risks / Trade-offs

- **[Risk] Accidental deletion of an active listing** → **Mitigation**: MaterialAlertDialog confirmation dialog before deleting an item.
