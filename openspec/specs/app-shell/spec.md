# app-shell Specification

## Purpose

Provides foundational Android application runtime container, build configurations, and initial Activity shell for Wall of Goodwill.

## Requirements

### Requirement: Android Application Container
The Android application SHALL initialize on devices running Android 7.0 (API level 24) or higher, targeting Android 14 (API level 34).

#### Scenario: Application launch
- **WHEN** the application is launched on a compatible Android device or emulator
- **THEN** the initial Activity displays successfully without crashing

### Requirement: Version Catalog and Build Setup
The project build system SHALL manage third-party dependencies and plugins through a centralized Gradle Kotlin DSL version catalog.

#### Scenario: Build project from command line or IDE
- **WHEN** the Android project build task is executed
- **THEN** dependencies are resolved from the version catalog and the debug APK builds successfully

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
