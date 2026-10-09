# Tasks

## 1. Backend Wishlist Service & Handlers

- [x] 1.1 Implement `server/internal/wishlist/service.go` with thread-safe models, seed items, and methods for create, list, filter, get my, and delete; verify with Go build.
- [x] 1.2 Implement HTTP handlers in `server/internal/handlers/wishlist.go` and mount protected routes in `server/cmd/api/main.go`; verify with handler compilation.
- [x] 1.3 Add unit and integration tests in `server/internal/handlers/wishlist_test.go` covering creation, filtering, author deletion, and authorization; verify with `/usr/local/go/bin/go test -v ./...`.

## 2. Android Wishlist Data Layer

- [x] 2.1 Add Java DTOs (`WishlistItemDto`, `CreateWishlistRequest`) and update `ApiService` with wishlist endpoints; verify by inspecting Java models.
- [x] 2.2 Implement `WishlistRepository` and `WishlistViewModel` for loading feed, filtering, creating, and deleting wishlist requests; verify classes compile cleanly.

## 3. Android Wishlist UI & Navigation

- [x] 3.1 Create wishlist UI layout files (`activity_wishlist.xml`, `item_wishlist.xml`, `bottom_sheet_create_wishlist.xml`) and `WishlistAdapter`; verify layout resource files are valid XML.
- [x] 3.2 Implement `WishlistActivity.java` and `CreateWishlistBottomSheetDialogFragment.java`, register in `AndroidManifest.xml`, and add entry points in `MainActivity` and `ProfileActivity`; verify with activity wiring.

## 4. Verification and Specification Sync

- [x] 4.1 Run full backend test suite (`/usr/local/go/bin/go test -v ./...`) and validate change with `openspec validate wishlist-requests-board --strict`; verify all checks pass.
- [x] 4.2 Sync main specification to `openspec/specs/wishlist-board/spec.md`, archive change, and push to GitHub; verify clean git status.
