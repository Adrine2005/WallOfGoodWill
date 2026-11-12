# Spec Delta: app-shell

## ADDED Requirements

### Requirement: Code Shrinking and ProGuard Preservation
The Android build configuration SHALL specify ProGuard/R8 rules that preserve Data Transfer Objects, serialization annotations, and Retrofit HTTP interfaces during minification.

#### Scenario: Compiling release APK with code shrinking
- **WHEN** the Android release build task runs with `isMinifyEnabled = true`
- **THEN** model classes in `com.goodwill.app.data.remote.dto` retain their fields and `@SerializedName` annotations, preventing JSON deserialization failures

### Requirement: Non-Blocking Main Thread Operations
All disk I/O, image scaling, and network communication in Activities SHALL execute on background worker threads to prevent Application Not Responding (ANR) warnings.

#### Scenario: User selects multiple photos for upload
- **WHEN** a user picks multiple images from the device gallery in `CreateListingActivity`
- **THEN** image stream reading, temp file writes, and network upload dispatch occur off the main UI thread, keeping UI frame rate responsive

