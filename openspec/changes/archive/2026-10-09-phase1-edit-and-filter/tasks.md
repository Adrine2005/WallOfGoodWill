# Tasks

## 1. Backend Listing Update Handler & Tests

- [x] 1.1 Implement `UpdateListing` in `server/internal/listings/service.go` and `PATCH /v1/listings/{id}` in `server/internal/handlers/listings.go`
- [x] 1.2 Mount route in `server/cmd/api/main.go` and add tests in `server/internal/handlers/listings_test.go` verified with `go test ./...`

## 2. Android Data Layer & Repository

- [x] 2.1 Add `@PATCH("v1/listings/{id}")` endpoint and `EditListingRequest` DTO in `ApiService.java` and `ListingsRepository.java`
- [x] 2.2 Add `editListing` method and state handling to `ListingsViewModel.java`

## 3. Android UI Screens & Navigation

- [x] 3.1 Add "Edit" action button in `item_my_listing.xml` and wire callback in `MyListingsAdapter.java`
- [x] 3.2 Update `CreateListingActivity.java` to handle Edit Mode when launched with existing listing
- [x] 3.3 Add Hostel Block filter option in `MainActivity.java` allowing students to filter listings by campus hostel
- [x] 3.4 Wire Edit action in `ProfileActivity.java` to launch `CreateListingActivity` and refresh on return
