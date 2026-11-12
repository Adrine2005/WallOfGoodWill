# Proposal

## Why

Wall of Goodwill currently relies on a single hardcoded light theme with standard blue colors and lacks dark/night mode support. For college students browsing, posting, or chatting about campus goods late at night in dorm rooms, high-contrast white screens cause significant eye strain and battery drain. Furthermore, the campus peer-to-peer exchange community benefits from a warmer, more approachable brand identity ("Warm Terracotta & Honey") that emphasizes collegiate community goodwill, second-hand reuse, and sustainability over generic corporate tech styling.

Providing both a default theme that respects system settings and direct user controls to toggle between White (Light) and Black (Dark/AMOLED) modes ensures accessibility, aesthetic warmth, and visual comfort across all lighting environments.

## What Changes

- **Theme Mode Selection & Persistence**: Introduce a theme preference mechanism (`ThemeManager`) supporting three user modes:
  - `System Default`: Automatically aligns with device OS night mode setting.
  - `White (Light Mode)`: Crisp warm-white background (`#FFFDF9`), clean white cards (`#FFFFFF`), and dark walnut text (`#292524`).
  - `Black (Dark Mode)`: Deep midnight/dark roast background (`#120E0C` / `#000000`), warm charcoal card surfaces (`#1E1714`), and soft ivory text (`#F5F5F4`).
- **Warm Terracotta & Honey Brand Palette**:
  - Primary: Warm Terracotta (`#C2410C` Light / `#FB923C` Dark)
  - Primary Container: Peach Cream (`#FFEDD5` Light / `#7C2D12` Dark)
  - Secondary / Free Badge: Olive Pine (`#059669` Light / `#34D399` Dark)
  - Karma Accent: Honey Gold (`#D97706` Light / `#FBBF24` Dark)
- **Profile Appearance Control**: Add an "Appearance & Theme" settings section in `ProfileFragment` allowing students to switch between System Default, White Mode, and Black Mode on the fly with immediate visual update.
- **Resource Architecture**: Provide `res/values/colors.xml`, `res/values-night/colors.xml`, and matching theme declarations in `res/values/themes.xml` and `res/values-night/themes.xml`.
- **Application Startup Hook**: Initialize saved theme preference during application startup (`GoodWillApp` / `MainActivity`) using `AppCompatDelegate.setDefaultNightMode()`.

## Capabilities

### Modified Capabilities
- `app-shell`: Add requirement for dynamic Day/Night theme selection, persistence, and the Warm Terracotta & Honey design system.

## Impact

- **Android Resources**: `colors.xml`, `themes.xml`, and new `values-night/colors.xml` and `values-night/themes.xml`.
- **UI Components**: `ProfileFragment`, `fragment_profile.xml` to include theme choice dialog/cards.
- **Local Storage**: `SessionManager` / `ThemeManager` SharedPreferences for persisting user theme preference.
- **Backward Compatibility**: Fully compatible with existing activities and fragment shells inheriting `Theme.Material3.DayNight.NoActionBar`.

