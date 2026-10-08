# campus-verification Specification

## Purpose

Provides time-limited QR invitation generation, verification endpoints, and client QR scanning for campus access authorization.

## Requirements

### Requirement: QR Code Invite Validation
The backend service SHALL expose an authenticated endpoint at POST /v1/auth/verify-qr that validates whether a submitted invite code is active and unexpired.

#### Scenario: Valid unexpired QR code verification
- **WHEN** an authenticated user submits an active QR code before its expiration timestamp
- **THEN** the server marks the user profile as verified and returns HTTP 200 with verification status true

#### Scenario: Expired QR code verification
- **WHEN** an authenticated user submits an invite code whose expiration timestamp has passed
- **THEN** the server rejects the request with HTTP 400 Bad Request and an error message indicating the QR code has expired

#### Scenario: Non-existent QR code
- **WHEN** a user submits an unrecognized invite code
- **THEN** the server returns HTTP 404 Not Found

### Requirement: Time-Limited Invite Generation
The backend service SHALL allow administrative users to generate time-limited invitation codes specifying an expiration duration and location label.

#### Scenario: Admin generates time-limited code
- **WHEN** an authorized admin requests a new QR invite code with a valid validity window
- **THEN** the server creates an invite record with code, creation time, expiration time, and active status
