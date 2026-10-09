# goodwill-gamification Specification

## Purpose
Tracks student donation milestones, awards points, manages badge tiers, and renders campus donation leaderboards.

## Requirements

### Requirement: Automated Point Award and Tier Calculation
The system SHALL automatically award Goodwill Points to students upon successful donation completion and recalculate their donor tier and earned badges.

#### Scenario: Student earns points upon donation
- **WHEN** a donation request is accepted or item marked donated
- **THEN** the donor receives 50 Goodwill Points and their donation count increments

#### Scenario: Badge tier unlocked
- **WHEN** a student reaches 150 points or 3 donations
- **THEN** the system grants the "Campus Philanthropist" badge

### Requirement: User Karma and Badge Retrieval
The system SHALL allow authenticated students to retrieve their accumulated Goodwill Points, donation statistics, and badge achievements.

#### Scenario: Student views profile points
- **WHEN** an authenticated user requests their points summary
- **THEN** the system returns their total points, current tier name, next tier progress, and list of unlocked badges

### Requirement: Campus Leaderboard Generation
The system SHALL provide a campus leaderboard ranking the top student donors by Goodwill Points.

#### Scenario: Retrieve top donors leaderboard
- **WHEN** an authenticated student requests the leaderboard
- **THEN** the system returns the top campus donors sorted by points descending with their ranks and badge titles
