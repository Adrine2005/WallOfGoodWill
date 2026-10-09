# Proposal

## Why

On the campus marketplace, free donation items often receive requests from multiple students simultaneously. Donors need a transparent and structured way to review requester notes (e.g. course requirements or hostel proximity) and accept one student while automatically notifying or declining others, formally closing the donation.

## What Changes

- Add backend `requests` service in Go with thread-safe in-memory store supporting donation requests.
- Add endpoints:
  - `POST /v1/listings/{id}/requests`: Authenticated students submit a request with a personal note.
  - `GET /v1/listings/{id}/requests`: Donor views all requests for their listing.
  - `GET /v1/requests/incoming`: Donor views all incoming requests across their listings.
  - `POST /v1/requests/{id}/accept`: Donor accepts a request; the selected request is marked `ACCEPTED`, other requests for the same listing are auto-marked `DECLINED`, and the listing status transitions to `DONATED`.
  - `POST /v1/requests/{id}/decline`: Donor declines a specific request.
- Add Android client request dialog in `ListingDetailActivity` letting students submit their request message.
- Add `DonationRequestsActivity` (accessible from `ProfileActivity` or `ListingDetailActivity`) allowing donors to review incoming requests and accept or decline.

## Capabilities

### New Capabilities

- `donation-requests`: Enables campus students to submit donation requests with personal notes and gives donors a review and single-acceptance workflow that auto-resolves concurrent requests.

### Modified Capabilities

None.

## Impact

- `server/internal/requests`: New domain package and service.
- `server/internal/handlers`: New HTTP handlers for donation request operations.
- `android/app/src/main/java/com/goodwill/app/data/`: ApiService endpoints, DTOs, and repository methods.
- `android/app/src/main/java/com/goodwill/app/ui/requests`: UI screens and adapters for donation requests.
