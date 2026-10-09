# Tasks

## 1. Backend Seed Visual Asset Polish

- [x] 1.1 Enrich default seed listings in `server/internal/listings/service.go` with high-resolution, verified campus exchange item photography; verify with Go build and tests.

## 2. Android Category Visual Fallbacks & Preset Selection

- [x] 2.1 Implement `CategoryPlaceholderHelper.java` in `com.goodwill.app.ui.listings` with category-to-drawable fallback resolution; verify helper compilation.
- [x] 2.2 Integrate `CategoryPlaceholderHelper` into `ListingsAdapter.java`, `ListingDetailActivity.java`, and `MyListingsAdapter.java`; verify Glide fallback integration.
- [x] 2.3 Add Quick Photo Presets chip group to `activity_create_listing.xml` and wire click listeners in `CreateListingActivity.java`; verify layout and activity compilation.

## 3. Verification and Specification Sync

- [x] 3.1 Run full backend test suite (`/usr/local/go/bin/go test -v ./...`) and validate change with `openspec validate image-upload-polish --strict`; verify all checks pass.
- [x] 3.2 Sync delta specs to main specification at `openspec/specs/item-listings/spec.md`, archive change, and push to GitHub; verify clean git status.
