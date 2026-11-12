# Tasks

## 1. Color Palettes & Android Theme Resources

- [x] 1.1 Update `res/values/colors.xml` with the Warm Terracotta and Honey Light Mode palette (Terracotta `#C2410C`, Peach `#FFEDD5`, Olive Pine `#059669`, Honey Gold `#D97706`, Linen Snow `#FFFDF9`, Dark Walnut `#292524`).
- [x] 1.2 Create `res/values-night/colors.xml` with the Warm Terracotta and Honey Dark Mode palette (Tangerine Glow `#FB923C`, Dark Roast Rust `#7C2D12`, Mint Sage `#34D399`, Golden Honey `#FBBF24`, Dark Roast Black `#120E0C`, Warm Charcoal `#1E1714`, Warm Ivory `#F5F5F4`).
- [x] 1.3 Create `res/values-night/themes.xml` configuring `Theme.WallOfGoodWill` for night mode with status bar color and Material 3 DayNight tokens, and audit drawables to ensure semantic color usage.

## 2. Theme Persistence & Application Startup Hook

- [x] 2.1 Implement `ThemeManager.java` managing theme modes (`system`, `light`, `dark`), storing preference in SharedPreferences, and applying `AppCompatDelegate.setDefaultNightMode()`.
- [x] 2.2 Implement unit tests in `ThemeManagerTest.java` verifying theme mode enum mapping, string conversions, and AppCompatDelegate night mode constants.
- [x] 2.3 Create `GoodWillApp.java` extending `Application`, invoke `ThemeManager.init(this)` in `onCreate()`, and register `android:name=".GoodWillApp"` in `AndroidManifest.xml`.

## 3. Profile UI & Theme Adjustment Dialog

- [x] 3.1 Update `fragment_profile.xml` to add the "Appearance & Theme" settings card showing current theme mode and theme change action.
- [x] 3.2 Wire `ProfileFragment.java` to display the active theme name and open a theme selection dialog allowing instant switching between System Default, White Mode, and Black Mode.

## 4. Verification & Validation

- [x] 4.1 Run `./gradlew testDebugUnitTest` and verify all unit tests pass cleanly.
- [x] 4.2 Compile `./gradlew assembleDebug`, install the debug APK onto the emulator, and verify dynamic switching between White and Black themes on device.

