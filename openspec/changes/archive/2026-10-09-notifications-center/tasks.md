# Tasks

## 1. Backend Notifications Service & Requests Triggering

- [x] 1.1 Implement `notifications.Service` in `server/internal/notifications/service.go` with models, storage, and unread counts
- [x] 1.2 Wire notifications dispatching into `server/internal/requests/service.go` on request creation, acceptance, and decline
- [x] 1.3 Implement HTTP handlers in `server/internal/handlers/notifications.go`, mount routes in `server/cmd/api/main.go`, and add test suite in `server/internal/handlers/notifications_test.go`

## 2. Android Data Layer & Repository

- [x] 2.1 Add notification DTOs in `com.goodwill.app.data.remote.dto`, endpoints in `ApiService.java`, and `NotificationsRepository.java`
- [x] 2.2 Implement `NotificationsViewModel.java` providing LiveData for notifications list, unread count, and read actions

## 3. Android UI Screens & Integration

- [x] 3.1 Create `item_notification.xml` layout and `NotificationsAdapter.java`
- [x] 3.2 Create `activity_notifications.xml` layout and `NotificationsActivity.java` with mark-all-read and click routing
- [x] 3.3 Add notification bell action with unread badge counter in `MainActivity.java` and register `NotificationsActivity` in `AndroidManifest.xml`
