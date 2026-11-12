# Design

## Context

The backend service already provides administrative invite capabilities in `server/internal/handlers/admin.go`:
- `POST /v1/admin/qr/generate`: Generates an invite code with `label`, `durationSeconds`, and `maxUses`.
- `GET /v1/admin/qr/list`: Returns existing invites with creation dates, expiration dates, and usage counts.

In the Android application, `VerifyQrActivity` enables students to scan or submit invite codes, but there is no corresponding administrative interface to create, inspect, or render QR codes on-screen.

See [proposal.md](file:///Users/adrinesolomon/MAD/openspec/changes/admin-qr-management/proposal.md) for motivation.

## Goals / Non-Goals

**Goals:**
- Provide Retrofit endpoints and repository layer for administrative QR operations (`GenerateQrRequest`, `InviteDto`, `AdminRepository`, `AdminViewModel`).
- Add an Admin Console entry card in `ProfileFragment` that is exclusively shown when the current authenticated user has `role == "admin"`.
- Implement `AdminQrActivity` to view all campus invite codes with real-time usage statistics (`usedCount` / `maxUses`) and active status.
- Implement `QrCodeGenerator` using `com.google.zxing:core:3.5.3` to render clean scannable QR bitmaps directly on the device display.
- Provide a generator dialog to specify custom location labels, expiration duration presets (24 Hours, 7 Days, 30 Days, 1 Year), and maximum usage caps.
- Provide a scannable QR popup dialog with one-tap code copying to clipboard.

**Non-Goals:**
- Modifying backend invite generation algorithms or database schemas.
- Changing student verification flow in `VerifyQrActivity`.

## Decisions

### Decision 1: QR Bitmap Generation Library (ZXing Core)
- **Choice**: Add `com.google.zxing:core:3.5.3` as an Android compile dependency.
- **Rationale**: ZXing Core is a lightweight pure-Java library (~500 KB) with no native library overhead or runtime permissions. It generates standard `BitMatrix` representations of QR codes in milliseconds offline, which convert cleanly into Android `Bitmap`s for `ImageView` display.
- **Alternatives Considered**:
  - *Remote QR API (e.g., Google Chart API / QR Server)*: Requires active internet access to render QR images, risks latency, and leaks invite codes to third-party endpoints.
  - *Manual custom canvas rendering*: Prone to QR alignment pattern encoding bugs.

### Decision 2: Access Control & Admin Entry Point
- **Choice**: Display `cardAdminConsole` in `ProfileFragment` only if `sessionManager.getUser() != null && "admin".equalsIgnoreCase(sessionManager.getUser().getRole())`.
- **Rationale**: `ProfileFragment` is the primary hub for user management and account identity. Highlighting administrative privileges here preserves separation of concerns without cluttering the student marketplace feed.
- **Alternatives Considered**:
  - *Bottom navigation tab*: Adds unnecessary clutter for the 99% of normal student users who do not have admin roles.

### Decision 3: QR Presentation & Sharing Flow
- **Choice**: Each invite card in `AdminQrActivity` has a "Show QR Code" button that opens an enlarged scannable dialog displaying the QR bitmap, large formatted code text (e.g. `GW-31D63A7C`), and a "Copy Code" button.
- **Rationale**: Notice board administrators need to either present their screen directly to a student for instant scanning or copy/print the code for hostel notice boards.

## User Interface Wireframes

```
PROFILE SCREEN (When logged in as Admin)
+---------------------------------------------------------------+
| Student Profile                                               |
| +-----------------------------------------------------------+ |
| | [S] Campus Management (Admin)                             | |
| |     ✓ CAMPUS VERIFIED  [ROLE: ADMIN]                      | |
| |     ✉ admin@campus.edu                                    | |
| +-----------------------------------------------------------+ |
|                                                               |
| +-----------------------------------------------------------+ |
| | 🛡️ CAMPUS ADMINISTRATION CONSOLE                          | |
| | Manage resident verification, physical notice board       | |
| | posters, and campus security invites                      | |
| |                                                           | |
| | [ 🎟️ Manage Campus QR Codes & Invites ]                   | |
| +-----------------------------------------------------------+ |
| ...                                                           |
+---------------------------------------------------------------+

ADMIN QR ACTIVITY (AdminQrActivity)
+---------------------------------------------------------------+
| <- Campus QR & Verification Codes            [ 🔄 Refresh ]   |
|---------------------------------------------------------------|
| ACTIVE CAMPUS INVITATIONS (3)                                 |
|                                                               |
| +-----------------------------------------------------------+ |
| | 📍 Hostel Block A Main Notice Board            [ ACTIVE ] | |
| | Code: GW-NOTICEBOARD-A                      [ 📋 Copy ]   | |
| | Uses: 14 / 1000 students                                  | |
| | Expires: in 11 months (Nov 12, 2027)                      | |
| | [ 📱 Show Scannable QR ]                                   | |
| +-----------------------------------------------------------+ |
|                                                               |
| +-----------------------------------------------------------+ |
| | 📍 Library Ground Floor Poster                 [ ACTIVE ] | |
| | Code: GW-EA1B4210                           [ 📋 Copy ]   | |
| | Uses: 0 / 100 students                                    | |
| | Expires: in 23 hours                                      | |
| | [ 📱 Show Scannable QR ]                                   | |
| +-----------------------------------------------------------+ |
|                                                               |
|                                             +---------------+ |
|                                             | + Generate QR | |
+---------------------------------------------+---------------+--+

SCANNABLE QR POPUP DIALOG
+---------------------------------------------------------------+
|                  Campus Verification QR                       |
|           Hostel Block A Main Notice Board                    |
|                                                               |
|                 +-------------------+                         |
|                 |  ██  ██  ███  ██  |                         |
|                 |  ██  ██   █   ██  |                         |
|                 |      ██  ███  ██  |                         |
|                 |  ██  ██   █   ██  |                         |
|                 +-------------------+                         |
|                                                               |
|                  Code: GW-NOTICEBOARD-A                       |
|              [ 📋 Copy Code ]  [ Close ]                      |
+---------------------------------------------------------------+
```

## Risks / Trade-offs

- **[Risk: Camera scan failure in dim hostel environments]** → *Mitigation*: The scannable dialog renders high-contrast black-on-white pixels and prominently displays the plain text code so students can enter it manually in `VerifyQrActivity`.
- **[Risk: Unauthorized student access]** → *Mitigation*: The backend requires admin authentication, and the client UI suppresses all administrative access buttons for student roles.

