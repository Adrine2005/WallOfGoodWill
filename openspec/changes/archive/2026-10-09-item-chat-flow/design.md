# Design

## Context

Wall of Good Will enables college campus students to list items for donation, sale, or request. While students can browse and request items, coordinating exchange logistics (handover time, hostel block, payment verification) previously lacked a private, in-app channel. See `proposal.md` for background and user privacy motivation.

## Goals / Non-Goals

**Goals:**
- Provide secure, authenticated 1-on-1 text messaging tied to specific item listings.
- Guarantee conversation uniqueness: exactly one active conversation thread exists per `(ListingID, BuyerID, SellerID)` tuple.
- Automatically dispatch notifications to message recipients via `notifications.Service`.
- Provide modern Android UI components:
  - "Message Seller / Donor" entry point in `ListingDetailActivity`.
  - `ConversationsActivity` displaying active conversations with preview snippet and last activity timestamp.
  - `ChatActivity` rendering alternating incoming/outgoing message bubbles with timestamping and quick reply input.

**Non-Goals:**
- Media/image attachments or file uploads within messages (text-only for this phase).
- WebSockets or third-party cloud messaging services (Firebase Cloud Messaging, Twilio) that introduce external billing or setup friction.
- Group chat rooms or unassociated user-to-user direct messages outside marketplace listings.

## Decisions

### 1. Listing-Scoped Conversation Data Model
- **Choice**: Conversations are explicitly tied to a `listingId`. The conversation entity holds `ListingID`, `ListingTitle`, `BuyerID`, `BuyerEmail`, `SellerID`, `SellerEmail`, `LastMessage`, and timestamps.
- **Rationale**: Campus exchanges are transaction-specific. Tying conversations to listings provides immediate context on what item is being negotiated or donated, and prevents ambiguity if two students transact across multiple items.
- **Alternatives Considered**: Global user-to-user DM model; discarded because buyers and sellers lose item context and listing status visibility.

### 2. Thread-Safe In-Memory Backend Store
- **Choice**: Implement `chat.Service` with an internal `sync.RWMutex` managing conversation threads and chronological message histories.
- **Rationale**: Matches the zero-cost, dependency-free architecture used across `listings.Service`, `requests.Service`, and `notifications.Service`. Ensures rapid, fully self-contained unit and integration testing.
- **Alternatives Considered**: External SQLite or Redis; rejected to maintain zero-cost local execution without requiring database daemon management.

### 3. Integrated Notification Dispatch
- **Choice**: When `chat.Service.SendMessage` succeeds, the backend creates an in-app notification for the recipient with type `CHAT_MESSAGE`, referencing the listing title and snippet of the message.
- **Rationale**: Reuses the recently delivered notifications center so students receive prompt notification badges without adding extra polling infrastructure.
- **Alternatives Considered**: Isolated chat badges only; rejected because having all campus activity in the notifications center provides a unified experience.

### 4. REST Polling for Client Updates
- **Choice**: Android `ChatViewModel` loads messages on initial view and refreshes after user submission or on pull-to-refresh / view resume.
- **Rationale**: Simple, zero-cost, and robust without long-lived socket management or connection dropouts on mobile data.
- **Alternatives Considered**: WebSocket connection; introduces stateful connection tracking and reconnection complexity on Android without substantial added value for asynchronous campus exchanges.

## Risks / Trade-offs

- **[Risk] High memory footprint with large message histories** → **Mitigation**: Conversations store text payloads, capped in length (e.g., 1000 characters per message), with slice indexing for rapid retrieval.
- **[Risk] Multiple simultaneous requests creating duplicate conversations** → **Mitigation**: Thread lookup and creation are synchronized under write lock in `chat.Service`.
- **[Risk] Ephemeral data reset on backend restart** → **Mitigation**: Follows the existing in-memory paradigm across all services; seed data can be provided for demonstration and manual QA.
