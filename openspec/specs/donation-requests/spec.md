# donation-requests Specification

## Purpose

Enables campus students to submit donation requests with personal notes and gives donors a review and single-acceptance workflow that auto-resolves concurrent requests.

## Requirements

### Requirement: Donation Request Submission
The backend service and client application SHALL allow authenticated students to submit a donation request with a personal message for any active donation listing.

#### Scenario: Student requests donation item
- **WHEN** an authenticated student submits a request with a non-empty message for an active donation listing
- **THEN** the system creates a pending donation request and returns HTTP 201 Created

#### Scenario: Non-donation item cannot be requested
- **WHEN** a user submits a donation request for a sale listing
- **THEN** the system rejects the request with HTTP 400 Bad Request

#### Scenario: Donor cannot request their own listing
- **WHEN** the donor of an item attempts to submit a request for their own listing
- **THEN** the system rejects the request with HTTP 400 Bad Request

### Requirement: Donor Request Resolution and Single Acceptance
The backend service and client application SHALL allow donors to review incoming requests and accept exactly one requester, automatically declining all other concurrent requests for that item.

#### Scenario: Donor accepts a requester
- **WHEN** a donor accepts a pending request
- **THEN** the system marks the chosen request as ACCEPTED, marks all other pending requests for the listing as DECLINED, and updates the listing status to DONATED

#### Scenario: Donor declines a requester
- **WHEN** a donor declines a pending request
- **THEN** the system marks the request as DECLINED while keeping other requests open

#### Scenario: Unauthorized request resolution rejected
- **WHEN** a user who is not the donor attempts to accept or decline a request
- **THEN** the system rejects the request with HTTP 403 Forbidden
