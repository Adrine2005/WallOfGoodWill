# Tasks

## 1. Backend Chat Domain & Handlers

- [x] 1.1 Implement `server/internal/chat/service.go` with thread-safe models and methods for creating/getting conversations and sending/retrieving messages; verify with Go build.
- [x] 1.2 Implement HTTP handlers in `server/internal/handlers/chat.go` and mount protected routes in `server/cmd/api/main.go`; verify with handler compilation.
- [x] 1.3 Add unit and integration tests in `server/internal/handlers/chat_test.go` covering conversation deduplication, message retrieval, and recipient notifications; verify by running `/usr/local/go/bin/go test -v ./...`.

## 2. Android Chat Data Layer

- [x] 2.1 Add Java DTOs (`ChatConversationDto`, `ChatMessageDto`, `CreateConversationRequest`, `SendMessageRequest`) and update `ApiService` with chat endpoints; verify by inspecting Java models.
- [x] 2.2 Implement `ChatRepository` and `ChatViewModel` for loading conversations, fetching messages, and sending messages; verify classes compile cleanly.

## 3. Android Chat UI & Navigation

- [x] 3.1 Create chat UI layout files (`activity_chat.xml`, `activity_conversations.xml`, `item_chat_message.xml`, `item_conversation.xml`) and adapters (`ChatAdapter`, `ConversationsAdapter`); verify layout resource files are valid XML.
- [x] 3.2 Implement `ChatActivity.java` and `ConversationsActivity.java`, register in `AndroidManifest.xml`, and add "Message Seller / Donor" entry point in `ListingDetailActivity.java`; verify with activity wiring.

## 4. Verification and Specification Sync

- [x] 4.1 Run full backend test suite (`/usr/local/go/bin/go test -v ./...`) and validate change with `openspec validate item-chat-flow --strict`; verify all checks pass.
- [x] 4.2 Sync main specification to `openspec/specs/item-chat/spec.md`, update project plan status, archive change, and push to GitHub; verify clean git status.
