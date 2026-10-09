# Design

## Context

While Wall of Goodwill supports marketplace items posted by donors and sellers, students often require items that are not yet listed on campus. The Wishlist Board flips the interaction: students broadcast their need, and peers with spare items can offer assistance. See `proposal.md` for background and user motivation.

## Goals / Non-Goals

**Goals:**
- Provide a thread-safe Go `wishlist.Service` supporting creation, filtering, retrieval, and deletion of item requests.
- Allow students to categorize requests (e.g., Books, Electronics, Cycles, Furniture) and indicate whether they prefer a free donation or have a maximum purchase budget.
- Provide responsive Android UI:
  - `WishlistActivity`: Feed of campus wishlist requests with category chips and search.
  - `CreateWishlistBottomSheetDialogFragment`: Fast input sheet with validation.
  - Navigation access from `MainActivity` and `ProfileActivity`.

**Non-Goals:**
- Automated web scraping or external retail price matching.
- Escrow or in-app money deposits for wishlist bounties.

## Decisions

### 1. Unified Domain Model with Category Taxonomy
- **Choice**: Reuses `listings.ItemCategory` (BOOKS, ELECTRONICS, CYCLES, etc.) for wishlist requests.
- **Rationale**: Ensures visual and semantic consistency across marketplace listings and wishlist boards. Students can filter wishlist items using the same categories they use on the home feed.
- **Alternatives Considered**: Free-form category tags; discarded because structured categories make browsing far more reliable.

### 2. Thread-Safe In-Memory Store with Mutex
- **Choice**: Implement `wishlist.Service` using internal `sync.RWMutex` and mapping by ID.
- **Rationale**: Maintains zero-cost, zero-dependency architecture across all services with instant test suite execution.
- **Alternatives Considered**: External database; rejected to preserve consistent zero-cost local operation.

### 3. Client Bottom Sheet for Rapid Creation
- **Choice**: Implement `CreateWishlistBottomSheetDialogFragment` on Android instead of a full separate activity.
- **Rationale**: Keeps users in the context of the wishlist feed, enabling rapid posting without losing scroll position.
- **Alternatives Considered**: Fullscreen activity; adds unnecessary navigation stack overhead.

## Risks / Trade-offs

- **[Risk] Stale wishlist items lingering after offline fulfillment** → **Mitigation**: Users can easily delete or resolve their requests; future auto-expiry can retire items older than 30 days.
- **[Risk] High volume of wishlist items** → **Mitigation**: Built-in category filtering and text search ensure quick discovery.
