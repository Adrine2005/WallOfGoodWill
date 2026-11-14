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

### Requirement: Listing Visual Assets and Preset Selection
The client and backend SHALL support rich visual assets for listings, including category-specific fallback illustrations and common preset photo suggestions during item publishing.

#### Scenario: Item rendered with category fallback when photo absent or offline
- **WHEN** a listing has no photos or image fails to load
- **THEN** the client displays a curated category-themed visual illustration placeholder

#### Scenario: Student selects quick photo preset
- **WHEN** a user taps a category photo preset in create listing screen
- **THEN** the preset photo URL is attached to the listing request

### Requirement: Staggered Masonry Listing Presentation
The client application SHALL present marketplace items in a 2-column staggered masonry grid that accommodates variable card heights, preserves original image aspect ratios, and displays overlay badges for price and donation status.

#### Scenario: Rendering listings in dual-column masonry format
- **WHEN** the feed loads active items
- **THEN** listings are laid out across two vertical staggered columns allowing differing card heights without awkward empty spaces

#### Scenario: Visual badges on masonry cards
- **WHEN** an item is displayed in the staggered grid
- **THEN** a prominent type badge (e.g., `DONATE • Free` or `₹ Price`) is anchored on top of the preview image

#### Scenario: Fallback illustration for image-less items
- **WHEN** an item has no image or image loading is unavailable
- **THEN** the card renders a themed category placeholder illustration preserving grid layout proportions

### Requirement: Client-Side Image Optimization and Fast Upload
The client application SHALL downscale selected device photos to a maximum dimension of 1280 pixels and compress them to JPEG format before streaming upload payloads, keeping media payload sizes under 500 kilobytes for fast transmission.

#### Scenario: User attaches high-resolution camera photo
- **WHEN** a user selects a high-resolution gallery or camera photo exceeding 2000px dimension
- **THEN** the client application downscales the image while preserving aspect ratio, compresses it to JPEG, and uploads the optimized payload to the backend service

#### Scenario: Image upload progress feedback
- **WHEN** an image upload is actively in flight
- **THEN** the client interface displays an uploading progress overlay and disables final submission until all photos have completed uploading

### Requirement: Resilient Remote Media Rendering
The client application SHALL utilize a resilient HTTP transport layer with fallback DNS mapping to load remote listing photos hosted on cloud storage CDNs, preventing DNS lookup failures from blocking image display.

#### Scenario: Image loading in restricted DNS environment
- **WHEN** the client application renders a listing photo hosted on a cloud storage domain in a network environment where system DNS resolution fails
- **THEN** the image loader resolves the domain to designated fallback CDN IP addresses and loads the photo successfully

#### Scenario: Placeholder display on unresolvable image error
- **WHEN** an image URL is completely unavailable or malformed
- **THEN** the client displays the appropriate category-specific fallback illustration without crashing

### Requirement: Direct Listing Removal from Detail View
The client application and backend service SHALL allow an authenticated seller to delete their active listing directly from the listing detail screen with a confirmation prompt.

#### Scenario: Seller deletes listing from detail screen
- **WHEN** the authenticated seller views their own listing and confirms deletion in the prompt dialog
- **THEN** the client dispatches a DELETE request, the backend marks the listing as inactive, and the client navigates back to the marketplace feed with updated state

