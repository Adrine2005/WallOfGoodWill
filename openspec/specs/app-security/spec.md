# app-security Specification

## Purpose
Enforces component exposure boundaries, Intent payload sanitization, and secure inter-activity communications across the Android application.

## Requirements

### Requirement: Explicit Component Export Controls
All Android application components (Activities, Services, Receivers) declared in `AndroidManifest.xml` SHALL explicitly set `android:exported="false"` unless they are designated entry points intended for external OS launch.

#### Scenario: External app attempts to invoke internal activity
- **WHEN** an external third-party application attempts to directly start an unexported activity (such as `ListingDetailActivity` or `VerifyQrActivity`)
- **THEN** the Android OS denies the invocation with a SecurityException, preventing unauthorized bypass of login or verification

#### Scenario: Launching the application entry point
- **WHEN** the launcher or system starts `SplashActivity`
- **THEN** the activity successfully initializes because it is explicitly exported with the main launcher intent filter

### Requirement: Safe Intent Extras Extraction
Activities that receive data parameters through incoming Intents SHALL validate presence, type, and bounds before consuming extra fields.

#### Scenario: Missing or malformed intent extra
- **WHEN** an activity is launched without expected extras or with null extra payloads
- **THEN** the activity gracefully handles the missing data or finishes cleanly without throwing an unhandled NullPointerException
