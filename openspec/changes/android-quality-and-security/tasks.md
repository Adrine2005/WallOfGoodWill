# Tasks

## 1. Skill Acquisition & Dependency Setup

- [x] 1.1 Ingest official Android skills (`android-intent-security`, `anr-audit-and-fix`, `r8-analyzer`, `testing-setup`) from `android/skills` into `.agents/skills/` and verify all `SKILL.md` files are accessible
- [x] 1.2 Add Mockito (`org.mockito:mockito-core`) and AndroidX Core Testing (`androidx.arch.core:core-testing`) to `android/gradle/libs.versions.toml` and `android/app/build.gradle.kts`, verifying that `./gradlew assembleDebug` resolves dependencies

## 2. Component & Intent Security Hardening

- [x] 2.1 Audit all `<activity>` declarations in `android/app/src/main/AndroidManifest.xml` to enforce that only `SplashActivity` is `android:exported="true"` and all other activities are `android:exported="false"`
- [x] 2.2 Add defensive Intent extra validation and null-safety guards in `ListingDetailActivity.java`, `ChatActivity.java`, and `CreateListingActivity.java`

## 3. Main-Thread ANR Prevention

- [x] 3.1 Refactor image stream reading and temporary file generation in `CreateListingActivity.java` to execute asynchronously on a background `ExecutorService`
- [x] 3.2 Marshal background file processing results safely back to the Android main thread using `runOnUiThread()` before updating thumbnail UI and initiating uploads

## 4. Code Shrinking & ProGuard Rules

- [x] 4.1 Create `android/app/proguard-rules.pro` containing preserve rules for Gson annotations (`@SerializedName`), DTO models (`com.goodwill.app.data.remote.dto.**`), and Retrofit interfaces (`com.goodwill.app.data.remote.ApiService`)
- [x] 4.2 Ensure `android/app/build.gradle.kts` references `proguard-rules.pro` in the `release` build type and verify that `./gradlew checkDebugAarMetadata assembleDebug` succeeds

## 5. Automated Unit Testing & Validation

- [x] 5.1 Implement `ListingsViewModelTest.java` in `android/app/src/test/java/com/goodwill/app/ui/listings/` using Mockito and `InstantTaskExecutorRule` to test feed loading, filtering, and error states
- [x] 5.2 Implement `AuthViewModelTest.java` in `android/app/src/test/java/com/goodwill/app/ui/auth/` to test login validation and session state transitions
- [x] 5.3 Execute `./gradlew testDebugUnitTest` and verify all unit tests pass cleanly

