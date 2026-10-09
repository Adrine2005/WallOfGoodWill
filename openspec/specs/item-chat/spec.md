# item-chat Specification

## Purpose
Enables authenticated students to engage in private 1-on-1 text conversations scoped to campus marketplace listings to coordinate details and campus meetups.

## Requirements

### Requirement: Listing-Scoped Conversation Initiation and Retrieval
The system SHALL allow authenticated students to initiate or retrieve a 1-on-1 chat conversation tied to a listing, deduplicating conversations so exactly one conversation exists per listing-buyer-seller pair.

#### Scenario: Buyer initiates conversation with listing owner
- **WHEN** an authenticated buyer submits a conversation initiation request for a listing
- **THEN** the system creates and returns a conversation record associating the listing, buyer, and seller

#### Scenario: Repeated conversation initiation returns existing thread
- **WHEN** a buyer initiates a conversation for a listing where an existing thread with the seller already exists
- **THEN** the system returns the existing conversation without creating duplicate threads

#### Scenario: Listing owner cannot start conversation with self
- **WHEN** a user attempts to initiate a conversation on their own listing
- **THEN** the system rejects the request with a client error

### Requirement: User Conversation Feed
The system SHALL allow authenticated students to list all conversations they participate in, ordered by latest activity.

#### Scenario: Participant views conversations list
- **WHEN** an authenticated user queries their conversations
- **THEN** the system returns all conversations where the user is either the buyer or the seller, sorted by most recent message or update timestamp

#### Scenario: Unauthenticated request rejected
- **WHEN** an unauthenticated request attempts to access conversations
- **THEN** the system returns a 401 Unauthorized status

### Requirement: Message Dispatch and Notification Alerting
The system SHALL allow conversation participants to send text messages within an active conversation and generate in-app notifications for the recipient.

#### Scenario: Participant sends text message
- **WHEN** a conversation participant posts a valid non-empty text message
- **THEN** the message is appended to the conversation history with an identifier and timestamp, and an in-app notification is dispatched to the other participant

#### Scenario: Non-participant cannot send message
- **WHEN** a user who is neither buyer nor seller in the conversation attempts to post a message
- **THEN** the system rejects the request with an authorization error

### Requirement: Conversation Message History Retrieval
The system SHALL allow conversation participants to retrieve the chronological message history of a conversation.

#### Scenario: Participant fetches message history
- **WHEN** an authorized participant requests the messages for a conversation
- **THEN** the system returns all messages in chronological order
