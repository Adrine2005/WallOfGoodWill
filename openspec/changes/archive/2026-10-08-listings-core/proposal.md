# Proposal

## Why

Students in university hostels regularly need to donate unwanted items (like books, kitchenware, furniture, cycles, and lab equipment) or sell used goods to peers without bureaucratic friction. Currently, campus communication relies on noisy WhatsApp or Telegram groups where listings get lost and there are no standardized item conditions, price tags, or hostel-specific filters. Providing a dedicated campus marketplace feed with clear DONATE vs SELL distinctions allows verified students to discover and share items efficiently within their community.

## What Changes

- Add backend REST APIs for creating, listing, filtering, searching, and managing marketplace items (`POST /v1/listings`, `GET /v1/listings`, `GET /v1/listings/{id}`, `GET /v1/listings/my`, `DELETE /v1/listings/{id}`).
- Support item types: `DONATE` (free, price = 0) and `SELL` (custom price set by student seller).
- Support standard campus item categories: `BOOKS`, `ELECTRONICS`, `KITCHENWARE`, `CYCLES`, `FURNITURE`, `FASHION`, `STATIONERY`, `OTHER`.
- Support item conditions: `NEW`, `LIKE_NEW`, `GOOD`, `FAIR`.
- Add an in-memory/mock listings store in the Go backend with search and multi-parameter filtering (category, type, hostel block, price range).
- Build the Android Home Feed screen (`HomeFragment` / `MainActivity`) featuring Material 3 item cards with distinct donation/sale badges, condition tags, and hostel block labels.
- Build category and hostel filter chips with instant real-time list filtering.
- Build the "Post Listing" screen (`CreateListingActivity`) with title, description, category selector, type toggle, price input, condition picker, and local image selection/preview.
- Build the "Listing Details" screen (`ListingDetailActivity`) with full item information, seller contact/hostel details, and action prompts.
- Implement Android `ListingsRepository` and `ListingsViewModel` with reactive LiveData.

## Capabilities

### New Capabilities

- `item-listings`: Defines the behavioral contract for creating campus marketplace listings, querying listings with category and hostel filters, and managing item statuses (active, sold, donated).

### Modified Capabilities

None. Existing authentication, campus verification, and app-shell contracts remain unchanged.

## Impact

- **Backend**: New Go package `server/internal/listings` and HTTP handlers mounted under `/v1/listings`.
- **Android Client**: New networking DTOs and endpoints in `ApiService`, `ListingsRepository`, `ListingsViewModel`, `CreateListingActivity`, and `ListingDetailActivity`.
- **Dependencies**: Uses existing Retrofit/OkHttp/Gson and Material 3 components; adds Glide for efficient image caching and loading.
