# Tasks

## 1. UI Layout Resources & Floating Dock

- [x] 1.1 Create `layout_floating_dock.xml` implementing the 5-destination pill dock (`Explore`, `Wishlist`, `Post [+]`, `Chats`, `Profile`) with notification/message badge indicators and rounded corner styling, and verify XML attributes compile.
- [x] 1.2 Refactor `item_listing.xml` into a masonry card layout with dynamic aspect-ratio image container, overlaid type badge (`DONATE • Free` / `₹ Price`), and bottom metadata, and verify layout preview inflation.
- [x] 1.3 Refactor `activity_main.xml` to embed `FragmentContainerView` (`nav_host_fragment_container`) and include `layout_floating_dock.xml` elevated at the bottom center.

## 2. Fragment Extraction & Navigation Setup

- [x] 2.1 Implement `FeedFragment.java` and `fragment_feed.xml` encapsulating the search input, horizontal category chips, `rvListings` configured with `StaggeredGridLayoutManager(2, VERTICAL)`, and bottom padding `96dp` to accommodate the dock.
- [x] 2.2 Implement `WishlistFragment.java` and `fragment_wishlist.xml` integrating the existing `WishlistViewModel` to render campus wishlist requests within the fragment container.
- [x] 2.3 Implement `ConversationsFragment.java` and `fragment_conversations.xml` integrating existing conversation list logic and unread count observers within the fragment container.
- [x] 2.4 Implement `ProfileFragment.java` and `fragment_profile.xml` displaying student verification details, hostel block, goodwill karma points, and user listings within the fragment container.

## 3. Shell Orchestration & Back Navigation in MainActivity

- [x] 3.1 Update `MainActivity.java` to initialize and cache the four navigation fragments using `FragmentManager` `show()`/`hide()` transactions to preserve scroll and filter state.
- [x] 3.2 Wire tab click listeners in `MainActivity` to update dock active states and launch `CreateListingActivity` on the center (+) button.
- [x] 3.3 Implement `OnBackPressedCallback` in `MainActivity` to revert to the `Explore` tab if a secondary tab is active before exiting the application.
- [x] 3.4 Bind unread message count and notification observers to display badges on the floating dock and header icons.

## 4. Verification & Build Validation

- [x] 4.1 Run `./gradlew testDebugUnitTest` and verify existing unit tests pass without regression.
- [x] 4.2 Execute `./gradlew assembleDebug` to confirm clean compilation, resource linking, and APK generation.

