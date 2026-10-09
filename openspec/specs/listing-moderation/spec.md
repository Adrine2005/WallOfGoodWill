# listing-moderation Specification

## Purpose
Enables campus students to report suspicious or prohibited listings, tracks moderation reports, and hides flagged items from public feeds.

## Requirements

### Requirement: Listing Report Submission
The system SHALL allow authenticated students to submit a safety or guideline report against a listing with a structured reason and optional comment.

#### Scenario: Student submits valid report
- **WHEN** an authenticated student submits a report with reason "PROHIBITED_ITEM"
- **THEN** the system logs the report and returns 201 Created with report details

#### Scenario: Submitting report with missing reason rejected
- **WHEN** a student submits a report without specifying a valid reason
- **THEN** the system rejects the request with a validation error

### Requirement: Automated Concealment on Report Threshold
The system SHALL track report count per listing and conceal listings from active campus browse feeds once flagged or reported multiple times.

#### Scenario: Flagged listing hidden from feed
- **WHEN** a listing is reported and marked flagged/hidden
- **THEN** the listing does not appear in public browse and search feed results

### Requirement: Report Audit History Retrieval
The system SHALL allow retrieving report records for a given listing to support community moderation.

#### Scenario: Fetch reports for a listing
- **WHEN** an authenticated user queries reports for a listing
- **THEN** the system returns the list of submitted reports with timestamps and reasons
