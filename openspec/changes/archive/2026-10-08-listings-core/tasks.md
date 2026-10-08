# Tasks

## 1. Go Backend Listings Service & REST Handlers

- [x] 1.1 Implement Listing data model, Category/Type/Condition enums, and thread-safe Service in `server/internal/listings/service.go` with unit tests in `server/internal/listings/service_test.go`
- [x] 1.2 Implement HTTP handlers (`CreateListing`, `ListListings`, `GetListing`, `GetMyListings`, `DeleteListing`) in `server/internal/handlers/listings.go`
- [x] 1.3 Mount `/v1/listings` routes in `server/cmd/api/main.go` and add integration tests in `server/internal/handlers/listings_test.go` verified with `go test ./...`

## 2. Android Client Data Layer & Repository

- [x] 2.1 Add Glide dependency to `android/gradle/libs.versions.toml` and `android/app/build.gradle.kts`
- [x] 2.2 Implement Listing DTOs (`ListingDto`, `CreateListingRequest`) and declare listings endpoints in `ApiService.java`
- [x] 2.3 Implement `ListingsRepository` and `ListingsViewModel` with LiveData for feed items, filter queries, and submission states

## 3. Android Home Feed & Marketplace UI

- [x] 3.1 Create `item_listing.xml` layout featuring Material 3 card, DONATE (free) vs SELL (₹) badges, hostel block label, and condition tag
- [x] 3.2 Implement `ListingsAdapter` with DiffUtil and item click callback navigating to details
- [x] 3.3 Update `activity_main.xml` and `MainActivity.java` with RecyclerView, horizontal category/block filter chips, search input, and Floating Action Button

## 4. Post Creation & Listing Details Screens

- [x] 4.1 Create `activity_create_listing.xml` layout and `CreateListingActivity.java` with category picker, Donate/Sell switch, price validation, condition selector, and posting logic
- [x] 4.2 Create `activity_listing_detail.xml` layout and `ListingDetailActivity.java` with item details, seller hostel room info, and contact prompt
- [x] 4.3 Register activities in `AndroidManifest.xml`, update `README.md` with listings API documentation, and verify with tests
