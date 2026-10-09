# Design

## Context

Wall of Goodwill prioritizes free item donations across university hostels. To reward and recognize students who regularly donate items instead of throwing them away, a gamification layer tracks donations, awards Goodwill Points, and assigns visual campus badges. See `proposal.md` for user motivation.

## Goals / Non-Goals

**Goals:**
- Provide thread-safe Go `points.Service` tracking user points, donation counts, and badges.
- Automatically award 50 points per completed donation and dynamically compute badge tiers:
  - `Newcomer` (< 50 pts)
  - `Donor Pioneer` (50–149 pts, 1+ donations)
  - `Campus Philanthropist` (150–249 pts, 3+ donations)
  - `Eco Champion` (250+ pts, 5+ donations)
- Provide campus leaderboard sorted by karma score.
- Provide Android UI:
  - Profile card showing points, current badge, and progress bar to next tier.
  - `LeaderboardActivity` displaying top donors.

**Non-Goals:**
- Monetary conversion or cash-out of points (purely reputation and social karma).
- Negative penalties or deduction of points.

## Decisions

### 1. Dynamic Tier and Badge Evaluation
- **Choice**: Compute badge tier on-the-fly based on accumulated points and donation count.
- **Rationale**: Prevents state desynchronization between point balance and badge criteria.
- **Alternatives Considered**: Storing badge unlock events separately in a log table; adds complexity without tangible benefit for 4 discrete tiers.

### 2. Event Hook from Request Acceptance
- **Choice**: Call `pointsService.AwardDonationPoints(donorId)` inside `requests.Service.AcceptRequest` and `listings.Service.UpdateListingStatus(DONATED)`.
- **Rationale**: Ensures donors immediately see their points increment when closing an exchange.
- **Alternatives Considered**: Manual claim button; rejected because students forget to claim.

### 3. In-Memory Aggregation with Thread Safety
- **Choice**: Internal `sync.RWMutex` managing user score structs, pre-seeded with sample contributors for instant demo functionality.
- **Rationale**: Consistent with current zero-cost, zero-dependency Go server design.

## Risks / Trade-offs

- **[Risk] Gaming points through fake listings** → **Mitigation**: Whitelist verification restricts posting to enrolled students; each accepted request requires a distinct requester.
