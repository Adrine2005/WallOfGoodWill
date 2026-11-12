# Spec Delta

## ADDED Requirements

### Requirement: Dynamic Day/Night Theme and Brand Color Support
The Android client application SHALL support dynamic theme switching between System Default, White (Light Mode), and Black (Dark Mode), applying the Warm Terracotta and Honey brand palette across all activities and screens with persistent storage across launches.

#### Scenario: User selects White (Light) mode
- **WHEN** the user selects the Light Theme option in settings
- **THEN** the application immediately applies the light Warm Terracotta palette using clean light backgrounds and dark text, persisting the selection for future launches

#### Scenario: User selects Black (Dark) mode
- **WHEN** the user selects the Dark Theme option in settings
- **THEN** the application immediately applies the dark Warm Terracotta palette using deep charcoal/black backgrounds, elevated dark card surfaces, and high-contrast light text, persisting the selection for future launches

#### Scenario: User selects System Default theme
- **WHEN** the user selects the System Default option in settings
- **THEN** the application delegates night mode resolution to the Android OS night mode configuration, automatically transitioning between light and dark when the system setting changes

#### Scenario: Cold launch with saved theme preference
- **WHEN** the application process starts up from a cold launch
- **THEN** the application reads the saved theme mode from storage and applies it via AppCompatDelegate before the first activity window renders

