# Tasks

## 1. Backend Moderation Service & Endpoints

- [x] 1.1 Implement `server/internal/moderation/service.go` with report models, report submission logic, concealment thresholds, and audit retrieval; verify with Go build.
- [x] 1.2 Implement HTTP handlers in `server/internal/handlers/moderation.go`, wire moderation routes in `server/cmd/api/main.go`, and filter hidden items in listings feed; verify with handler compilation.
- [x] 1.3 Add unit and integration tests in `server/internal/handlers/moderation_test.go` covering report submission, validation, and auto-concealment; verify with `/usr/local/go/bin/go test -v ./...`.

## 2. Android Reporting Data Layer

- [x] 2.1 Add Java DTOs (`ReportListingRequest`, `ReportDto`) and register endpoints in `ApiService.java`; verify with Java model inspection.
- [x] 2.2 Implement `ModerationRepository.java` for asynchronous reporting requests; verify class compilation.

## 3. Android Moderation UI & Integration

- [x] 3.1 Create `dialog_report_listing.xml` with reason selection chips and comment input; verify valid XML.
- [x] 3.2 Implement `ReportBottomSheetDialogFragment.java` and wire report action menu / button in `ListingDetailActivity.java`; verify activity and dialog wiring.

## 4. Verification and Specification Sync

- [x] 4.1 Run full backend test suite (`/usr/local/go/bin/go test -v ./...`) and validate change with `openspec validate listing-reporting-moderation --strict`; verify all checks pass.
- [x] 4.2 Sync main specification to `openspec/specs/listing-moderation/spec.md`, archive change, and push to GitHub; verify clean git status.
