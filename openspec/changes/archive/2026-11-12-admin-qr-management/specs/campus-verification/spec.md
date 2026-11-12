# Spec Delta

## ADDED Requirements

### Requirement: Client Administrative QR Management
The Android client application SHALL provide administrative users with a dedicated QR management interface to generate new time-limited invitation codes with location labels, view active invitations with usage statistics, and render scannable QR code bitmaps.

#### Scenario: Admin creates new location-tagged QR code
- **WHEN** an authenticated administrator submits a location label, validity duration, and maximum uses
- **THEN** the client sends the generation request to the backend and renders the newly created QR code bitmap alongside the human-readable code text

#### Scenario: Admin inspects active campus invitations
- **WHEN** an administrator opens the QR management screen
- **THEN** the client retrieves and displays all active invitation records with used count, max uses, and expiration status

#### Scenario: Non-admin users restricted from admin panel
- **WHEN** an authenticated student without admin privileges views their profile
- **THEN** the client hides the administration console entry and restricts access to administrative tools

