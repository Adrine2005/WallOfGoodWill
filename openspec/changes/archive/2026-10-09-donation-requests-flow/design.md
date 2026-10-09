# Design

## Context

See `proposal.md` for motivation. Currently listings are created with type `DONATE` or `SELL`. For donation items, students only see a toast message upon clicking "Request Item from Donor". This design adds structured request tracking and donor resolution.

## Goals / Non-Goals

**Goals:**
- Provide a `requests.Service` in Go with methods `CreateRequest`, `GetListingRequests`, `GetIncomingRequests`, `AcceptRequest`, and `DeclineRequest`.
- Coordinate request acceptance with `listings.Service` to automatically mark the item `DONATED` and decline competing requests.
- Provide a Request Submission BottomSheet in `ListingDetailActivity` for recipients.
- Provide a `DonationRequestsActivity` for donors to review requester notes and accept/decline.

**Non-Goals:**
- Real FCM push messaging (using in-app state updates and immediate feedback).

## Decisions

1. **Atomic Request Acceptance with Auto-Decline**:
   In `requests.Service.AcceptRequest`, a write lock is acquired across requests for the target `listingId`. The target request is updated to `ACCEPTED`, all other `PENDING` requests for that listing are updated to `DECLINED`, and the listing status in `listings.Service` is set to `DONATED`.

2. **Android BottomSheet for Request Submission**:
   Use `RequestItemBottomSheetDialogFragment` with a message input field (`tilRequestMessage`) to allow students to articulate their need before submitting.

3. **Incoming Requests Navigation**:
   Provide direct access to `DonationRequestsActivity` from both `ProfileActivity` ("View Incoming Requests") and `ListingDetailActivity` (for owners viewing their own donation item).

## Risks / Trade-offs

- [Risk: Concurrent Acceptance Race Condition] → Mitigation: Synchronize on the requests mutex and verify listing status is still `ACTIVE` before accepting.
