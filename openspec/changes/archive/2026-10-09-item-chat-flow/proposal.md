# Proposal

## Why

Students exchanging marketplace items (whether negotiating prices for sale items or coordinating hostel meetup spots for donations) currently have to share personal phone numbers or leave the application. Adding 1-on-1 in-app messaging scoped to listings protects student privacy, prevents off-platform communication friction, and allows buyers and sellers to agree on safe campus meeting locations.

## What Changes

- Add backend `chat` domain service with thread-safe in-memory store supporting 1-on-1 conversations scoped to listings.
- Add conversation deduplication so a buyer and seller share exactly one conversation per listing.
- Integrate chat messages with `notifications.Service` to automatically generate in-app alerts when a new message is received.
- Add REST endpoints:
  - `POST /v1/chat/conversations`: Initiate or retrieve existing conversation for a listing.
  - `GET /v1/chat/conversations`: List all active conversations for the authenticated user (as buyer or seller).
  - `GET /v1/chat/conversations/{id}/messages`: Retrieve conversation message history.
  - `POST /v1/chat/conversations/{id}/messages`: Send a message in a conversation.
- Add Android client chat interface:
  - Add "Message Seller / Donor" button in `ListingDetailActivity`.
  - Add `ChatActivity` with message bubbles (incoming/outgoing styles), timestamping, and input composer.
  - Add `ConversationsActivity` listing active chat threads with recent message preview and unread status.
  - Add `ChatRepository`, `ChatViewModel`, and DTOs.

## Capabilities

### New Capabilities
- `item-chat`: Enables authenticated students to engage in private, 1-on-1 text conversations scoped to campus marketplace listings to coordinate details and campus meetups.

### Modified Capabilities
None.

## Impact

- `server/internal/chat`: New domain package and service.
- `server/internal/handlers`: HTTP handlers for conversation and message endpoints.
- `server/internal/notifications`: Trigger notification on incoming chat message.
- `android/app/src/main/java/com/goodwill/app/data/`: ApiService endpoints, DTOs, and repository.
- `android/app/src/main/java/com/goodwill/app/ui/chat/`: Chat screens, message adapters, and ViewModel.
- `android/app/src/main/java/com/goodwill/app/ui/listings/ListingDetailActivity.java`: Action button integration.
