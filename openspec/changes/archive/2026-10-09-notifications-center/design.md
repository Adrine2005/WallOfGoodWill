# Design

## Context

Following the implementation of the donation requests flow, students need in-app feedback when their requests are accepted, declined, or when new requests arrive for their listings. Because third-party cloud push messaging (FCM) requires external credentials, this design introduces a zero-cost, persistent in-app notifications system.

## Goals / Non-Goals

**Goals:**
- Provide a `notifications.Service` in Go that manages in-app notifications and read states per user.
- Automatically generate notifications when donation requests are created, accepted, or declined (including competing auto-declines).
- Provide REST endpoints for fetching notifications, unread counts, and updating read statuses.
- Provide an unread badge indicator in `MainActivity`'s toolbar.
- Provide a `NotificationsActivity` on Android allowing students to review notifications and mark them as read.

**Non-Goals:**
- Operating system level push notifications when the app is completely killed.

## Decisions

1. **In-Memory Thread-Safe Notification Registry**:
   The Go service stores notifications in `map[string]*Notification` guarded by a read/write mutex (`sync.RWMutex`). Each notification contains `ID`, `UserID`, `Title`, `Message`, `Type` (`REQUEST_RECEIVED`, `REQUEST_ACCEPTED`, `REQUEST_DECLINED`), `TargetID` (listing/request ID), `IsRead`, and `CreatedAt`.

2. **Decoupled Notification Dispatching in Requests Service**:
   `requests.Service` accepts an optional or required `*notifications.Service` reference and invokes `notifications.CreateNotification(...)` inside `CreateRequest`, `AcceptRequest`, and `DeclineRequest`.

3. **Client-Side Polling and Unread Badge**:
   `MainActivity` checks `GET /v1/notifications/unread-count` on resume and updates the menu badge. Clicking the bell opens `NotificationsActivity`.

4. **Actionable Click Navigation**:
   Clicking a notification item in `NotificationsActivity` marks it as read and routes the user:
   - For `REQUEST_RECEIVED`: routes to `DonationRequestsActivity`.
   - For `REQUEST_ACCEPTED` / `REQUEST_DECLINED`: routes to `ListingDetailActivity` or displays details.

## Risks / Trade-offs

- **[Risk: Notification Explosion on Auto-Decline]** → Mitigation: Each competitor request receives a concise `REQUEST_DECLINED` notification specifying that another student was chosen.
