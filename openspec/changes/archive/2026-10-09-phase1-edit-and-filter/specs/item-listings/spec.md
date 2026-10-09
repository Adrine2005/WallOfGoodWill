# Spec Delta

## ADDED Requirements

### Requirement: Listing Update Management
The backend service and client application SHALL allow authenticated sellers to edit the title, description, price, condition, or category of their active listings.

#### Scenario: Seller edits listing attributes
- **WHEN** an authenticated seller submits an update request with valid changes to title, description, or price
- **THEN** the system updates the listing record and persists updated timestamp

#### Scenario: Unauthorized edit attempt rejected
- **WHEN** a user attempts to edit a listing created by another student
- **THEN** the system rejects the request with HTTP 403 Forbidden
