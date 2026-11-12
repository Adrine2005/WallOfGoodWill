# Proposal: Android Quality, Security & Testing Hardening

## Why

Following the completion of core functional milestones and the photo upload system, the Android application requires production-readiness hardening based on official Android guidance (`android/skills`).
Currently, several Android components lack explicit intent validation, disk/file operations in `CreateListingActivity` execute synchronously on UI dispatch paths which risk Application Not Responding (ANR) events, ProGuard/R8 rules are not configured to protect reflection/Gson models during release minification, and no automated unit testing harness exists for core ViewModels. Implementing these safeguards ensures stability, security compliance, and long-term testability.

## What Changes

- **Android Component & Intent Security**: Audit `AndroidManifest.xml` to ensure all activities have explicit `android:exported` attributes, validate and sanitize incoming extras in `ListingDetailActivity` and `ChatActivity`, and restrict cleartext network traffic boundaries.
- **Main-Thread ANR Prevention**: Offload file creation, bitmap streaming, and media compression in `CreateListingActivity` to background execution threads (`Executors.newSingleThreadExecutor()` / coroutines).
- **R8 / ProGuard Rules**: Create `android/app/proguard-rules.pro` containing preserve rules for Gson DTOs (`com.goodwill.app.data.remote.dto.**`), Retrofit service interfaces, and OkHttp connection classes to guarantee release build shrinking integrity.
- **Automated Testing Setup**: Configure Gradle dependencies for JUnit4, Mockito, and AndroidX Architecture Testing (`InstantTaskExecutorRule`), and write unit tests for `ListingsViewModel` and `AuthViewModel`.
- **Install Official Android Skills**: Install official `android-intent-security`, `anr-audit-and-fix`, `r8-analyzer`, and `testing-setup` skills into `.agents/skills/` for ongoing AI-assisted maintenance.

## Capabilities

### New Capabilities
- `app-security`: Audits and enforces Android component export boundaries, intent extra validation, and secure inter-activity communications.
- `app-testing`: Establishes automated unit testing harnesses, mock providers, and LiveData assertion infrastructure for ViewModels and data layers.

### Modified Capabilities
- `app-shell`: Extends the build configuration to include release ProGuard/R8 preservation rules and non-blocking background thread guarantees for I/O operations.

## Impact

- **Android App**: `AndroidManifest.xml`, `CreateListingActivity.java`, `proguard-rules.pro`, `build.gradle.kts`, `libs.versions.toml`.
- **Agent Skills**: Adds `.agents/skills/android-*` skill modules.
- **Server**: Zero breaking changes to Go API or backend services.

