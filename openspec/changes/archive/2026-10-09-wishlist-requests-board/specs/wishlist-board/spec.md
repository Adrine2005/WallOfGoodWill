# Spec Delta

## Purpose

Enables authenticated campus students to broadcast items they are looking for and enables peers to discover and fulfill those needs.

## ADDED Requirements

### Requirement: Wishlist Item Creation
The system SHALL allow authenticated students to post a wishlist request specifying the needed item title, description, category, and preferred price or free donation preference.

#### Scenario: Student posts valid wishlist request
- **WHEN** an authenticated student submits a request with title and category
- **THEN** the system stores the wishlist item with ACTIVE status and returns the created record

#### Scenario: Request with missing title rejected
- **WHEN** a user attempts to post a wishlist item with an empty title
- **THEN** the system rejects the request with a validation error

### Requirement: Wishlist Board Feed and Filtering
The system SHALL allow authenticated students to browse active wishlist requests and filter by category or text search.

#### Scenario: Student queries active wishlist items
- **WHEN** an authenticated user queries the wishlist feed
- **THEN** the system returns active items ordered by newest creation date

#### Scenario: Student filters wishlist by category
- **WHEN** a user queries the wishlist with a category filter parameter
- **THEN** the system returns only active items matching the specified category

### Requirement: Personal Wishlist Management and Deletion
The system SHALL allow students to view their own wishlist items and delete or close items when fulfilled.

#### Scenario: Student views their own posted wishlist items
- **WHEN** an authenticated student requests their own wishlist items
- **THEN** the system returns only wishlist items authored by the user

#### Scenario: Author deletes fulfilled wishlist item
- **WHEN** the item author requests deletion of their wishlist item
- **THEN** the system removes or marks the item as closed

#### Scenario: Unauthorized user cannot delete peer's wishlist item
- **WHEN** a user attempts to delete a wishlist item belonging to another student
- **THEN** the system rejects the deletion with a forbidden error
