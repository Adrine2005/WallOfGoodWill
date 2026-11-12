# Proposal

## Why

The current main activity layout relies on an overloaded top toolbar with four small action buttons (Notifications, Messages, Wishlist, Profile) and a single-column vertical feed. On modern tall smartphones, reaching the top corners with one hand is ergonomically difficult, while single-column cards display only 1.5 items per screen. By modernizing the frontend with a Pinterest-inspired staggered masonry grid and an edge-to-edge floating navigation dock hosted inside `MainActivity`, students can browse 3x more listings per scroll and navigate effortlessly between Feed, Wishlist, Messaging, and Profile using natural thumb zones.

## What Changes

- **Unified Single-Activity Fragment Navigation Shell**: Refactor `MainActivity` into a centralized navigation host using an AndroidX Fragment container hosting `FeedFragment`, `WishlistFragment`, `ConversationsFragment`, and `ProfileFragment`.
- **Floating Island Dock Navigation Bar**: Implement a floating Material 3 pill navigation dock at the bottom of the screen featuring 5 destinations:
  1. `Explore / Feed`
  2. `Wishlist` (Hostel requests board)
  3. `Post Item (+)` (Elevated center action that launches `CreateListingActivity`)
  4. `Chats` (Conversations list with unread badge)
  5. `Profile` (Student karma, hostel block, posted listings)
- **Staggered Masonry Product Grid**: Replace the single-column `LinearLayoutManager` in the feed with a 2-column `StaggeredGridLayoutManager`. Listing cards support dynamic aspect ratios with elevated price/donation badges, hostel block tags, and category fallback placeholders.
- **Floating Translucent App Bar**: Replace the bulky top toolbar with a clean Material 3 search bar with quick hostel block filtering and notification bell access.
- **Preserved Deep Links & Existing Activities**: Retain existing standalone activities (`WishlistActivity`, `ConversationsActivity`, `ProfileActivity`) for external intent and notification deep-linking, while sharing common ViewModel and UI state with the hosted Fragments.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `app-shell`: Specifies the unified fragment-hosting architecture inside `MainActivity` with a floating pill navigation dock and edge-to-edge layout styling.
- `item-listings`: Specifies the 2-column staggered masonry grid presentation for marketplace feed items with dynamic height cards and badge overlays.

## Impact

- **UI Layouts**: Refactors `activity_main.xml` and `item_listing.xml`; adds `fragment_feed.xml`, `layout_floating_dock.xml`, and fragment containers.
- **Java Code**: Updates `MainActivity.java` to manage fragment transactions and bottom dock item selections; introduces `FeedFragment.java` and adapts `Wishlist`, `Conversations`, and `Profile` UI into fragments.
- **Dependencies**: Uses existing AndroidX Fragment (`androidx.fragment:fragment`), Material Components (`com.google.android.material`), and ViewBinding. No new external library required.
- **APIs & Backend**: No breaking backend API changes; consumes existing listings, chat, and wishlist endpoints.

