# notifications-center Specification

## Purpose

Enables campus students to receive and manage in-app activity notifications for donation requests and resolution events.

## Requirements

### Requirement: In-App Notification Generation and Dispatch
The backend service SHALL generate in-app notifications for users upon donation request creation and resolution lifecycle events.

#### Scenario: Donor receives notification on new donation request
- **WHEN** an authenticated student submits a donation request for a donor's listing
- **THEN** the system generates an unread notification with type REQUEST_RECEIVED addressed to the donor

#### Scenario: Requester receives notification when request is accepted
- **WHEN** a donor accepts a pending donation request
- **THEN** the system generates an unread notification with type REQUEST_ACCEPTED addressed to the accepted requester

#### Scenario: Requester receives notification when request is declined
- **WHEN** a donation request is declined manually or auto-declined upon competing acceptance
- **THEN** the system generates an unread notification with type REQUEST_DECLINED addressed to the affected requester

### Requirement: Notification Retrieval and Read State Management
The backend service and client application SHALL allow authenticated students to view their notifications, track unread counts, and mark notifications as read.

#### Scenario: Student retrieves notification feed
- **WHEN** an authenticated student requests their notifications
- **THEN** the system returns the user's notifications sorted by most recent first

#### Scenario: Student checks unread notification count
- **WHEN** the client application requests the unread notification count
- **THEN** the system returns the integer count of notifications where read status is false

#### Scenario: Student marks notification as read
- **WHEN** an authenticated student marks an unread notification as read
- **THEN** the system sets the notification read status to true and decrements unread count

#### Scenario: Student marks all notifications as read
- **WHEN** an authenticated student requests to mark all notifications as read
- **THEN** the system sets all of that user's notifications to read status true
