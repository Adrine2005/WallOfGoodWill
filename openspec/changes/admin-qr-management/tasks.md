# Tasks

## 1. Data Models, Retrofit Endpoints & Dependencies

- [ ] 1.1 Add `zxing-core = "3.5.3"` to `libs.versions.toml` and `build.gradle.kts` and verify Gradle sync succeeds.
- [ ] 1.2 Create `InviteDto.java` and `GenerateQrRequest.java` model classes with Gson `@SerializedName` mapping backend fields (`code`, `label`, `expiresAt`, `maxUses`, `usedCount`, `isActive`).
- [ ] 1.3 Add `generateInvite()` and `listInvites()` endpoints to `ApiService.java` and implement `AdminRepository.java`.

## 2. ViewModel & QR Code Bitmap Generator

- [ ] 2.1 Implement `QrCodeGenerator.java` using ZXing `QRCodeWriter` to convert string codes into scannable black-and-white Android `Bitmap`s.
- [ ] 2.2 Implement `AdminViewModel.java` with LiveData for invite listing, generation dispatch, loading states, and error handling.
- [ ] 2.3 Implement unit tests in `AdminViewModelTest.java` verifying invite generation and listing operations.

## 3. Admin UI Screens & Profile Integration

- [ ] 3.1 Create layout resources `activity_admin_qr.xml`, `item_admin_invite.xml`, `dialog_generate_qr.xml`, and `dialog_view_qr.xml`.
- [ ] 3.2 Implement `AdminQrActivity.java` and its RecyclerView adapter, wiring pull-to-refresh, generation bottom sheet, and scannable QR popup dialog.
- [ ] 3.3 Register `AdminQrActivity` in `AndroidManifest.xml` and update `fragment_profile.xml` and `ProfileFragment.java` to display the Admin Console card when `user.getRole().equalsIgnoreCase("admin")`.

## 4. Verification & Validation

- [ ] 4.1 Run `./gradlew testDebugUnitTest` and verify all tests pass.
- [ ] 4.2 Execute `./gradlew assembleDebug` to confirm clean compilation and install the updated build on the emulator.
