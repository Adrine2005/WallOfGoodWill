# Design

## Context

`MainActivity` currently serves as a monolithic screen holding the listings RecyclerView, filter chip horizontal scroll, search input, and header buttons that launch distinct activities (`WishlistActivity`, `ConversationsActivity`, `ProfileActivity`). The feed is presented via a single-column `LinearLayoutManager`.

See [proposal.md](file:///Users/adrinesolomon/MAD/openspec/changes/frontend-masonry-shell/proposal.md) for motivation.

## Goals / Non-Goals

**Goals:**
- Provide a centralized single-activity container in `MainActivity` hosting 4 primary Fragments (`FeedFragment`, `WishlistFragment`, `ConversationsFragment`, `ProfileFragment`).
- Build an edge-to-edge floating navigation dock pill elevated above the content area, with 5 touch targets: Explore, Wishlist, Post Listing (+ center CTA), Chats, and Profile.
- Implement a 2-column staggered masonry layout (`StaggeredGridLayoutManager`) for marketplace listings that allows variable-height photo cards and overlays price/donation badges.
- Retain existing standalone Activity classes (`WishlistActivity`, `ConversationsActivity`, `ProfileActivity`) so notification pending intents and direct deep-links remain fully functional.
- Maintain scroll position and transient search state across tab switches without reloading data unnecessarily.

**Non-Goals:**
- Modifying backend REST APIs or data transfer contracts.
- Modifying `CreateListingActivity`, `ListingDetailActivity`, or `ChatActivity` inner business logic.
- Introducing heavy third-party UI framework dependencies.

## Decisions

### Decision 1: Floating Dock Navigation Component
- **Choice**: Implement a floating `MaterialCardView` dock (`layout_floating_dock.xml`) positioned at `layout_gravity="bottom|center_horizontal"` with `layout_marginBottom="16dp"`, full pill corner radius (`app:cardCornerRadius="28dp"`), and `app:cardElevation="8dp"`.
- **Rationale**: Standard Material 3 `BottomNavigationView` attaches flush to the screen bottom edge and restricts center button elevation. A floating card dock provides the modern, lightweight pill aesthetic requested in Option 3 while seamlessly hosting a raised circular (+) button.
- **Alternatives Considered**:
  - *Standard BottomNavigationView*: Lacks floating pill aesthetics and requires complex custom menu item hacks for center elevated FAB styling.

### Decision 2: Fragment Lifecycle & State Preservation (Show/Hide vs Replace)
- **Choice**: Manage navigation fragments via `FragmentManager` using an attach/show/hide pattern:
  - Add each fragment to `FragmentContainerView` on first selection and retain instances.
  - Hide the previously active fragment and show the selected fragment.
- **Rationale**: `replace()` destroys and recreates fragment views, causing network re-fetching, image reload flickers, and loss of scroll position. `show()`/`hide()` maintains scroll state in the masonry feed when a user checks a chat and switches back.
- **Alternatives Considered**:
  - *Fragment replacement (`replace`)*: Leads to slow tab switching and re-triggering feed API requests.
  - *Jetpack Navigation Component with NavHostFragment*: Adds boilerplate navigation graphs and still requires custom backstack navigators to avoid view destruction on tab switching.

### Decision 3: Staggered Masonry Grid Architecture
- **Choice**: In `FeedFragment`, configure `rvListings` with `StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)`.
  - Set `gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS`.
  - Update `item_listing.xml` with dynamic card dimensions: image container uses `scaleType="centerCrop"` with variable heights, preserving a compact aspect ratio.
  - Ensure `rvListings.setClipToPadding(false)` with `paddingBottom="96dp"` so cards never hide behind the floating dock.
- **Rationale**: Maximizes screen real estate, accommodates varying aspect ratios of camera photos taken by students, and displays 4–6 items simultaneously.
- **Alternatives Considered**:
  - *Uniform 2-Column GridLayoutManager*: Forces rigid square aspect ratios, resulting in cropped or letterboxed product photos.

### Decision 4: Back Navigation Strategy
- **Choice**: Implement AndroidX `OnBackPressedCallback` in `MainActivity`. If the current selected tab is not `Explore` (Tab 0), back press switches selection to `Explore`. If already on `Explore`, default system back behavior (finishing the activity) executes.
- **Rationale**: Prevents accidental app exit when a student is browsing Wishlist or Chats inside the main container.

## Architecture & Layout Wireframe

```
+---------------------------------------------------------------+
| MainActivity (CoordinatorLayout)                              |
|                                                               |
|  +---------------------------------------------------------+  |
|  | FragmentContainerView (id/nav_host_fragment_container)  |  |
|  |                                                         |  |
|  | [Active Fragment: FeedFragment]                         |  |
|  |                                                         |  |
|  |  +---------------------------------------------------+  |  |
|  |  | Floating Search & Campus Bar                      |  |  |
|  |  +---------------------------------------------------+  |  |
|  |  [All] [🎁 Free] [🏷️ For Sale] [📚 Books]...            |  |
|  |                                                         |  |
|  |  +-------------------------+ +-----------------------+  |  |
|  |  | [Staggered Card 1]      | | [Staggered Card 2]    |  |  |
|  |  | 🎁 Free                 | | 🏷️ ₹1,200             |  |  |
|  |  | Mountain Bike           | | Engineering Physics   |  |  |
|  |  | Block B                 | | Block C               |  |  |
|  |  +-------------------------+ +-----------------------+  |  |
|  |                                                         |  |
|  +---------------------------------------------------------+  |
|                                                               |
|             +-----------------------------------+             |
|             |  🏠     📋     (+)     💬     👤  |             |  |
|             | Feed  Wishlist POST  Chats Profile|             |  |
|             +-----------------------------------+             |  |
|                   Floating Bottom Dock Pill                   |
+---------------------------------------------------------------+
```

## Risks / Trade-offs

- **[Risk: Staggered grid reordering jumps on scroll]** → *Mitigation*: Ensure `item_listing.xml` defines deterministic layout constraints and avoid calling `notifyDataSetChanged()` when `DiffUtil` or payload updates are available.
- **[Risk: Content obscured by floating dock]** → *Mitigation*: Enforce `android:paddingBottom="96dp"` and `android:clipToPadding="false"` across all fragment scrollable containers (`FeedFragment`, `WishlistFragment`, `ConversationsFragment`, `ProfileFragment`).
- **[Risk: Standalone Activity Intent breakage]** → *Mitigation*: Retain `WishlistActivity`, `ConversationsActivity`, and `ProfileActivity` classes in the manifest; they can continue to render standalone for notification deep links or delegate into the main shell.

