# Spec Delta: app-testing

## Purpose

Provides automated unit testing harnesses, mock providers, and LiveData assertion infrastructure for ViewModels and repository data flows.

## ADDED Requirements

### Requirement: ViewModel Unit Test Execution
The Android build system SHALL support automated unit test execution for application ViewModels using JUnit4 and Mockito without requiring a physical device or emulator.

#### Scenario: Running ViewModel unit tests
- **WHEN** the `./gradlew testDebugUnitTest` command is executed
- **THEN** unit tests instantiate ViewModels with mocked dependencies and verify that LiveData state emissions match expected business logic

### Requirement: LiveData Synchronization Test Harness
The unit test environment SHALL enforce synchronous execution of Android Architecture Components background tasks through `InstantTaskExecutorRule`.

#### Scenario: Observing LiveData in unit tests
- **WHEN** a test asserts the value of a MutableLiveData or LiveData emitted by a ViewModel
- **THEN** value changes are observed synchronously on the test thread without race conditions or Looper initialization failures

