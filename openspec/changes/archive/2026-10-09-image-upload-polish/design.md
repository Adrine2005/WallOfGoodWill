# Design

## Context

Visuals drive campus engagement on Wall of Goodwill. When students browse hostel items or don't have custom photos readily available, having curated category-specific placeholders and preset suggestions ensures the interface looks vibrant, modern, and reliable both online and offline.

## Goals / Non-Goals

**Goals:**
- Provide `CategoryPlaceholderHelper` to map item categories to clean, themed visual vector/drawable assets.
- Integrate category placeholders into `ListingsAdapter`, `ListingDetailActivity`, and `MyListingsAdapter`.
- Update backend seed listings in Go with high-res, verified campus exchange image URLs.
- Provide Quick Preset Photo Selector chips in `activity_create_listing.xml` and `CreateListingActivity.java`.

**Non-Goals:**
- External cloud storage buckets like AWS S3 / Cloudinary (preserves zero-cost offline architecture).

## Decisions

1. **CategoryPlaceholderHelper**:
   - Centralizes category-to-drawable fallback mapping in Android.
   - Prevents blank/broken images when offline or when photos are omitted.

2. **Quick Preset Photo Selector**:
   - Chip group on `CreateListingActivity` allowing students to pick common hostel item presets with one tap, pre-filling high-resolution curated asset URLs.

## Risks / Trade-offs

- **[Risk] Slow network connections dropping remote images** → Mitigation: CategoryPlaceholderHelper provides instant local drawables immediately as Glide placeholder and error target.
