# Proposal

## Why

Visual presentation is crucial for a campus marketplace. Currently, items without photos or with slow network loading display generic or blank placeholders. Enhancing visual assets with rich, category-curated fallbacks (books, electronics, cycles, stationery, kitchenware, furniture) and polished seed listings significantly elevates user experience and app aesthetics without requiring cloud storage costs.

## What Changes

- Add category-specific visual fallback drawables and rendering logic in Android (`ListingsAdapter`, `ListingDetailActivity`, `MyListingsAdapter`).
- Polish backend seed listings with high-res, verified, representative campus exchange images.
- Provide Quick Preset Photo Selector in `CreateListingActivity` (allowing students to tap common campus photo presets like "Textbook", "Calculator", "Kettle", "Cycle", "Desk Lamp") to easily attach high-quality visual assets.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `item-listings`: Adds visual asset fallback rendering and preset asset attachment requirements for marketplace items.

## Impact

- **Server**: Enriched image URLs and categories in `server/internal/listings/service.go`.
- **Android App**: Enhanced Glide image loading logic with category-specific placeholders, preset image helper in `CreateListingActivity`, and visual polish in `ListingsAdapter` and `ListingDetailActivity`.
