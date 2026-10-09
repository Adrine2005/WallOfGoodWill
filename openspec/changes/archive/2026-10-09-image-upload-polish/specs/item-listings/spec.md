# Spec Delta

## ADDED Requirements

### Requirement: Listing Visual Assets and Preset Selection
The client and backend SHALL support rich visual assets for listings, including category-specific fallback illustrations and common preset photo suggestions during item publishing.

#### Scenario: Item rendered with category fallback when photo absent or offline
- **WHEN** a listing has no photos or image fails to load
- **THEN** the client displays a curated category-themed visual illustration placeholder

#### Scenario: Student selects quick photo preset
- **WHEN** a user taps a category photo preset in create listing screen
- **THEN** the preset photo URL is attached to the listing request
