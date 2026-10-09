# Proposal

## Why

After campus students submit donation requests or donors accept/decline them, users lack an in-app activity feed to monitor the status of their requests and listings. An In-App Notifications Center provides students with immediate visibility into key events (request received, request accepted, request declined) and an unread notification badge, closing the loop on campus donation exchanges without requiring paid external cloud push notifications.

## What Changes

- Add backend `notifications` domain service with thread-safe in-memory store tracking user notifications, read states, and unread counts.
- Add backend notification dispatch triggers in `requests.Service` when a donation request is created (notifying the donor) and when a request is accepted or declined (notifying the requester).
- Add HTTP REST endpoints:
  - `GET /v1/notifications`: Retrieve all notifications for the authenticated student.
  - `GET /v1/notifications/unread-count`: Get number of unread notifications.
  - `POST /v1/notifications/{id}/read`: Mark a specific notification as read.
  - `POST /v1/notifications/read-all`: Mark all notifications as read.
- Add Android client notification center:
  - Add notification bell icon with dynamic unread badge in `MainActivity` top toolbar.
  - Add `NotificationsActivity` showing an activity list with timestamps, event icons, and read/unread status.
  - Provide "Mark All as Read" action and click-through navigation to relevant listing/request screens.
  - Add `NotificationsRepository` and `NotificationsViewModel`.

## Capabilities

### New Capabilities
- `notifications-center`: Enables authenticated campus students to receive, view, and manage in-app activity notifications for donation requests, resolutions, and marketplace actions.

### Modified Capabilities
None.

## Impact

- `server/internal/notifications`: New domain package and service.
- `server/internal/requests`: Wire notification service into requests lifecycle (create, accept, decline).
- `server/internal/handlers`: HTTP handlers for notification listing and read status.
- `android/app/src/main/java/com/goodwill/app/data/`: ApiService endpoints, DTOs, and repository methods.
- `android/app/src/main/java/com/goodwill/app/ui/notifications/`: Activity, adapter, ViewModel, and layouts.
- `android/app/src/main/java/com/goodwill/app/MainActivity.java`: Toolbar notification badge and menu action.
