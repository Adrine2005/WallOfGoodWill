# Proposal

## Why

Although the backend provides administrative endpoints to generate and list campus invitation QR codes (`POST /v1/admin/qr/generate` and `GET /v1/admin/qr/list`), the Android application currently lacks any user interface for administrators to generate codes, view active physical notice board invitations, or render scannable QR bitmaps. Without this interface, administrators cannot easily create on-campus verification posters or physically verify new student residents.

## What Changes

- **Admin Console Entry Point in Profile**: Add a conditional "🛡️ Campus Administration" panel in `ProfileFragment` that is exclusively visible when the authenticated user's role is `admin`.
- **Admin QR & Code Management Screen (`AdminQrActivity`)**:
  - Displays a real-time list of all active campus invitation codes with location label, expiration countdown, and usage statistics (`usedCount` / `maxUses`).
  - Provides a "Generate New QR" modal allowing admins to specify location label (e.g., "Hostel Block B Main Gate", "Library Ground Floor"), expiration duration, and maximum student uses.
- **On-Device QR Code Bitmap Rendering**: Integrate ZXing Core to render high-contrast, scannable QR bitmaps directly on screen, allowing incoming students to scan the physical device screen or notice board posters with Google Code Scanner.
- **Client Networking & Data Layer**: Add `GenerateQrRequest`, `InviteDto`, Retrofit endpoints in `ApiService`, `AdminRepository`, and `AdminViewModel`.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `campus-verification`: Specifies the client administrative QR generation and management interface, QR bitmap rendering, and invite list inspection.

## Impact

- **UI Layouts**: Adds `activity_admin_qr.xml`, `item_admin_invite.xml`, `dialog_generate_qr.xml`, `dialog_view_qr.xml`; updates `fragment_profile.xml` with admin management card.
- **Java Code**: Adds `AdminQrActivity.java`, `AdminViewModel.java`, `AdminRepository.java`, `QrCodeGenerator.java`, and DTOs (`InviteDto`, `GenerateQrRequest`). Updates `ApiService.java` and `ProfileFragment.java`.
- **Dependencies**: Adds `com.google.zxing:core:3.5.3` for zero-dependency local QR code bitmap encoding.
