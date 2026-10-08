# Proposal

## Why

Once students post items on the campus marketplace, they need full control over their account and active listings. After donating a book or selling an electric kettle to a peer, the seller must be able to mark the item as "Sold" or "Donated" so other students stop contacting them. Furthermore, students need a personal profile screen to review their campus verification status, see their hostel details, and log out safely.

## What Changes

- Add backend REST endpoint `PUT /v1/listings/{id}/status` allowing authenticated sellers to update an item's status to `SOLD` or `DONATED`.
- Add a profile shortcut in the `MainActivity` header bar (`Profile` button/avatar) navigating to `ProfileActivity`.
- Build the `ProfileActivity` screen displaying:
  - Student Name, Email, Hostel Block, Room Number, and "Campus Verified" badge
  - "My Listings" section showing items posted by the logged-in student
  - Direct listing action buttons: "Mark as Sold", "Mark as Donated", and "Delete"
  - Sign Out button clearing local session and navigating to `LoginActivity`
- Implement Android `ProfileViewModel` and `MyListingsAdapter`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `item-listings`: Adds seller status modification requirements (`SOLD`, `DONATED`) and my listings queries.

## Impact

- **Backend**: Mounts `PUT /v1/listings/{id}/status` in `server/internal/handlers/listings.go` and `server/cmd/api/main.go`.
- **Android Client**: Updates `ApiService.java`, `ListingsRepository.java`, `MainActivity.java`, and introduces `ProfileActivity.java` with Material 3 layouts.
