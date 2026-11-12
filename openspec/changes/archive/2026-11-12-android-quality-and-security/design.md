# Design: Android Quality, Security & Testing Hardening

## Context

The Wall of Goodwill Android client currently features 12 completed functional milestones and an OLX-style multi-image upload pipeline. However, as evaluated against official Android engineering standards (`android/skills`):
1. File operations during image picking currently execute synchronously in `copyUriToTempFile()` inside `CreateListingActivity`, which can drop frames or trigger ANRs on lower-end devices when copying multiple large gallery images.
2. Component security requires auditing `AndroidManifest.xml` to prevent intent redirection or unauthorized component launches.
3. R8 code shrinking rules are missing, which would break Gson deserialization in minified release builds.
4. Automated unit tests are missing for MVVM ViewModels (`ListingsViewModel` and `AuthViewModel`).

See `proposal.md` for background motivation.

## Goals / Non-Goals

**Goals:**
- Enforce explicit `android:exported` attributes across all Activity declarations in `AndroidManifest.xml`.
- Guard incoming Intent extra parsing against missing, null, or invalid payloads.
- Move image file stream copying in `CreateListingActivity` to background worker threads using `ExecutorService`.
- Configure `proguard-rules.pro` to keep Retrofit interfaces, DTO fields, and Gson `@SerializedName` annotations intact during release minification.
- Add testing dependencies to `libs.versions.toml` and write unit test suites for `ListingsViewModel` and `AuthViewModel`.
- Install official Google Android skills into `.agents/skills/` for ongoing AI agent guidance.

**Non-Goals:**
- Rewriting existing Views into Jetpack Compose (Compose migration is out of scope).
- Building an entire UI test suite with Roborazzi/screenshot tests (focus is unit testing and ViewModel state correctness).
- Modifying backend Go APIs or database schemas.

## Decisions

### 1. Concurrency Model for File I/O in Activities: ExecutorService vs Coroutines
- **Decision**: Use `Executors.newSingleThreadExecutor()` with UI callbacks via `runOnUiThread(Runnable)`.
- **Rationale**: The project is written in pure Java (not Kotlin). Introducing Kotlin Coroutines would add unnecessary language runtime overhead. Java's `ExecutorService` is lightweight, deterministic, and native.
- **Alternative considered**: `AsyncTask` (deprecated in Android 11) or RxJava (heavyweight external dependency).

### 2. R8 / ProGuard Keep Rules for Data Layer
- **Decision**: Add explicit keep rules for all classes under `com.goodwill.app.data.remote.dto.**` and keep attributes `*Annotation*`, `Signature`, and `InnerClasses`.
- **Rationale**: Gson relies on runtime reflection and `@SerializedName` annotations. Without explicit keep rules, R8 renames or strips model fields, resulting in empty objects upon HTTP 200 responses.

### 3. Unit Testing Architecture
- **Decision**: Use JUnit4 + Mockito + `InstantTaskExecutorRule` from `androidx.arch.core:core-testing`.
- **Rationale**: `InstantTaskExecutorRule` forces Architecture Component LiveData emissions to execute synchronously on the calling thread, avoiding Looper errors in local JVM tests.

### 4. Skill Repository Ingestion
- **Decision**: Package the 4 core skills (`android-intent-security`, `anr-audit-and-fix`, `r8-analyzer`, `testing-setup`) into `.agents/skills/`.
- **Rationale**: Placing `SKILL.md` in `.agents/skills/` conforms to Antigravity's discovery root and ensures every future session has access to these verified rules.

## Risks / Trade-offs

- **[Risk] Background file copying race condition**: User taps Submit while images are still being copied on the background thread.
  - **Mitigation**: Existing `isAnyPhotoUploading()` check in `CreateListingActivity` is augmented to disable submission and display progress overlays until all background worker tasks complete.
- **[Risk] Mocking Context in Unit Tests**: Repositories require Android `Context` for `SessionManager`.
  - **Mitigation**: Use `Mockito.mock(Context.class)` and inject mocked `SessionManager` / `ApiService` instances into ViewModels or repository constructors.

## Migration Plan

1. Update `libs.versions.toml` and `android/app/build.gradle.kts` with testing dependencies.
2. Add `android/app/proguard-rules.pro` and link it in the `release` build type.
3. Update `CreateListingActivity.java` with background executor threading.
4. Audit `AndroidManifest.xml` and validate Intent extras.
5. Add unit tests in `android/app/src/test/java/com/goodwill/app/`.
6. Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` to verify.

