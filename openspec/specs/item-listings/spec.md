# item-listings Specification

## Purpose

Provides campus marketplace listing creation, home feed discovery, multi-criteria filtering, and item status management for students.

## Requirements

### Requirement: Marketplace Item Creation
The backend service and client application SHALL allow verified students to publish marketplace items specifying title, description, category, type (`DONATE` or `SELL`), price, condition, and hostel block location.

#### Scenario: Create donation listing with free price
- **WHEN** verified student submits a listing with type DONATE
- **THEN** the system sets price to 0 and stores the listing with active status

#### Scenario: Create sale listing with positive price
- **WHEN** verified student submits a listing with type SELL and a positive price
- **THEN** the system records the listing with specified price and active status

#### Scenario: Rejection of invalid sale price
- **WHEN** a user submits a listing with type SELL and a price less than or equal to zero
- **THEN** the system rejects the submission with HTTP 400 Bad Request

### Requirement: Campus Marketplace Feed and Filtering
The backend service and client application SHALL allow students to retrieve and filter active listings by category, item type, hostel block, and search query keywords.

#### Scenario: Filter listings by category and type
- **WHEN** user requests listings specifying category `BOOKS` and type `DONATE`
- **THEN** the system returns only active book donation listings matching the criteria

#### Scenario: Filter listings by hostel block
- **WHEN** user requests listings filtered by a specific hostel block
- **THEN** the system returns items located in that hostel block ordered by creation time

#### Scenario: Search query keyword matching
- **WHEN** user searches with keyword text
- **THEN** the system returns listings matching the keyword in title or description case-insensitively

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

### Requirement: Listing Update Management
The backend service and client application SHALL allow authenticated sellers to edit the title, description, price, condition, or category of their active listings.

#### Scenario: Seller edits listing attributes
- **WHEN** an authenticated seller submits an update request with valid changes to title, description, or price
- **THEN** the system updates the listing record and persists updated timestamp

#### Scenario: Unauthorized edit attempt rejected
- **WHEN** a user attempts to edit a listing created by another student
- **THEN** the system rejects the request with HTTP 403 Forbidden
