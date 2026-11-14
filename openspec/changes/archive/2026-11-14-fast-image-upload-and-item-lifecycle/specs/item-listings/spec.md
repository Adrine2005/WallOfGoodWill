# Spec Delta

## ADDED Requirements

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

