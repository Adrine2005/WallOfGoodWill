# Tasks

## 1. Backend Requests Domain & Service

- [x] 1.1 Implement `requests.Service` in `server/internal/requests/service.go` with request model, validation, and auto-decline logic
- [x] 1.2 Implement HTTP handlers in `server/internal/handlers/requests.go` for creating, listing, accepting, and declining requests
- [x] 1.3 Mount requests routes in `server/cmd/api/main.go` and add unit/integration tests in `server/internal/handlers/requests_test.go` verified with `go test ./...`

## 2. Android Data Layer & Repository

- [x] 2.1 Add requests DTOs, API endpoints in `ApiService.java`, and repository methods in `RequestsRepository.java`
- [x] 2.2 Implement `RequestsViewModel.java` providing LiveData for incoming requests, submission state, and accept/decline actions

## 3. Android UI Screens & Integration

- [x] 3.1 Implement `RequestItemBottomSheetDialogFragment` and wire it in `ListingDetailActivity.java`
- [x] 3.2 Create `item_donation_request.xml` layout and `DonationRequestsAdapter.java` with Accept/Decline action buttons
- [x] 3.3 Create `activity_donation_requests.xml` layout and `DonationRequestsActivity.java` for donor management of requests
- [x] 3.4 Add "Incoming Requests" navigation button in `ProfileActivity.java` and register `DonationRequestsActivity` in `AndroidManifest.xml`
