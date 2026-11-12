# Spec Delta

## ADDED Requirements

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

