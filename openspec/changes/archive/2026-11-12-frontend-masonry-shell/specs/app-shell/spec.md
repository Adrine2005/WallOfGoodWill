# Spec Delta

## ADDED Requirements

### Requirement: Unified Fragment Shell and Bottom Dock Navigation
The Android client application SHALL host primary navigation destinations inside `MainActivity` using an AndroidX Fragment container orchestrated by a floating bottom navigation dock with quick access to Explore, Wishlist, Post Listing, Chats, and Profile.

#### Scenario: Switching navigation tabs in-place
- **WHEN** the user taps the Wishlist, Chats, or Profile tab on the floating bottom dock
- **THEN** `MainActivity` switches to the corresponding fragment within the container without launching a new Activity or losing current session state

#### Scenario: Launching listing creation from elevated dock action
- **WHEN** the user taps the center (+) action button on the floating dock
- **THEN** the application launches `CreateListingActivity` smoothly over the current screen

#### Scenario: Back navigation handling
- **WHEN** the user presses the device back button while viewing a non-Explore tab
- **THEN** the application switches back to the Explore/Feed tab before triggering application exit

