# Tasks

## 1. Backend Status Update Handler & Routing

- [x] 1.1 Implement `PUT /v1/listings/{id}/status` handler in `server/internal/handlers/listings.go` allowing sellers to update status to `SOLD` or `DONATED`
- [x] 1.2 Mount status update route in `server/cmd/api/main.go` and add test cases in `server/internal/handlers/listings_test.go` verified with `go test ./...`

## 2. Android Data Layer & Repository

- [x] 2.1 Add status update endpoint (`updateListingStatus`) and request DTO in `ApiService.java` and `ListingsRepository.java`
- [x] 2.2 Implement `ProfileViewModel` providing LiveData for user profile, my posted listings, and status modification actions

## 3. Android UI Screens & Navigation

- [x] 3.1 Create `item_my_listing.xml` layout with status tags and action buttons (Mark as Sold, Mark as Donated, Delete)
- [x] 3.2 Implement `MyListingsAdapter` with action callbacks
- [x] 3.3 Create `activity_profile.xml` layout and `ProfileActivity.java` with student profile card, My Listings list, and Sign Out confirmation
- [x] 3.4 Add Profile navigation button in `activity_main.xml` and register `ProfileActivity` in `AndroidManifest.xml`
- [x] 3.5 Run repository verification tests and confirm working tree is clean
