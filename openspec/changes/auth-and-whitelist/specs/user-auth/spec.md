# Spec Delta

## Purpose

Provides student user authentication screens, hostel profile registration, session management, and API authorization tokens.

## ADDED Requirements

### Requirement: Student Account Registration
The client application SHALL allow users to create an account by providing their name, email address, password, hostel block selection, and room number.

#### Scenario: Valid registration form submission
- **WHEN** user enters valid details, selects a hostel block, and submits the registration form
- **THEN** an account is created and the user is guided to the campus verification screen

#### Scenario: Missing required registration fields
- **WHEN** user submits the registration form with an empty hostel block or missing room number
- **THEN** the submission is blocked and field-specific validation errors are displayed

### Requirement: Student Login and Session Routing
The client application SHALL authenticate existing students using their email and password, redirecting unverified accounts to the QR gateway and verified accounts to the home feed.

#### Scenario: Login with verified account
- **WHEN** a verified student successfully logs in
- **THEN** the client stores the session token and navigates directly to the home feed

#### Scenario: Login with unverified account
- **WHEN** an unverified student successfully logs in
- **THEN** the client navigates to the campus QR verification gateway screen
