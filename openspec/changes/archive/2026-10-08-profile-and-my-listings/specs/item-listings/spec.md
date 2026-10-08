# Spec Delta

## MODIFIED Requirements

### Requirement: Listing Status Management
The system SHALL allow the seller to view their own listings, mark them as completed (`SOLD` or `DONATED`), or remove them from the public feed.

#### Scenario: Seller deletes active listing
- **WHEN** authenticated seller requests deletion of their own listing
- **THEN** the system marks the item as inactive or removes it from public feed results

#### Scenario: Unauthorized deletion rejected
- **WHEN** a user attempts to delete or update a listing belonging to another student
- **THEN** the system rejects the request with HTTP 403 Forbidden

#### Scenario: Seller marks item as sold
- **WHEN** authenticated seller submits a status update specifying status SOLD
- **THEN** the system updates the listing status to SOLD and hides it from the active public feed

#### Scenario: Seller marks item as donated
- **WHEN** authenticated seller submits a status update specifying status DONATED
- **THEN** the system updates the listing status to DONATED and records completion
