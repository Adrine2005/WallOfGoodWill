# Design

## Context

See `proposal.md` for background and user requirements. Wall of Goodwill currently uses `Theme.Material3.DayNight.NoActionBar` as its base parent theme in `res/values/themes.xml`, but only has a single `res/values/colors.xml` file configured with hardcoded light-mode blue colors (`#1E88E5`). There is no `values-night` directory, no Application class initializing night mode preferences, and no user settings UI to adjust appearance.

## Goals / Non-Goals

**Goals:**
- Provide full support for System Default, White (Light Mode), and Black (Dark Mode) themes.
- Implement the "Warm Terracotta & Honey" brand palette across both light and dark modes with WCAG AA compliance.
- Introduce `ThemeManager` to cleanly persist and apply `AppCompatDelegate.NightMode` selections.
- Introduce `GoodWillApp` application class registered in `AndroidManifest.xml` to prevent theme flicker on cold boot.
- Integrate an "Appearance & Theme" selection card in `ProfileFragment` with instant theme application.
- Update UI drawables and cards to ensure consistent rendering across both themes.

**Non-Goals:**
- Custom hex color pickers or user-created custom themes.
- Server-side theme synchronization (preference remains local to device).

## Decisions

### Decision 1: Android Material3 DayNight Framework API
- **Choice**: Utilize standard `AppCompatDelegate.setDefaultNightMode()` with:
  - `AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM` (-1)
  - `AppCompatDelegate.MODE_NIGHT_NO` (1)
  - `AppCompatDelegate.MODE_NIGHT_YES` (2)
- **Rationale**: Cleanest native Android approach; automatically resolves layout resources from `res/values/` and `res/values-night/` without manual view traversal or restarts.
- **Alternatives considered**: Dynamic CSS-style custom runtime attribute inflation — rejected due to excessive complexity and performance overhead.

### Decision 2: ThemeManager & Cold Launch Hook
- **Choice**: Create `ThemeManager.java` managing SharedPreferences key `pref_theme_mode` (`"system"`, `"light"`, `"dark"`). Create `GoodWillApp.java` subclassing `Application` and apply `ThemeManager.applySavedTheme(this)` in `onCreate()`.
- **Rationale**: Guarantees night mode is configured before the initial Activity window inflates, eliminating visual flashes.

### Decision 3: "Warm Terracotta & Honey" Palette Architecture
- **Light Mode (`res/values/colors.xml`)**:
  - `primary`: `#C2410C` (Terracotta)
  - `primary_variant`: `#9A3412` (Deep Rust)
  - `primary_container`: `#FFEDD5` (Warm Peach Cream)
  - `on_primary_container`: `#7C2D12` (Dark Rust)
  - `secondary`: `#059669` (Olive Pine)
  - `secondary_container`: `#D1FAE5` (Pale Mint)
  - `accent_karma`: `#D97706` (Honey Gold)
  - `background`: `#FFFDF9` (Warm Linen Snow)
  - `surface`: `#FFFFFF` (Pure White)
  - `surface_variant`: `#F5F0EB` (Soft Warm Gray)
  - `text_primary`: `#292524` (Dark Walnut)
  - `text_secondary`: `#78716C` (Muted Warm Stone)
  - `outline`: `#E7E5E4` (Warm Border)
- **Dark Mode (`res/values-night/colors.xml`)**:
  - `primary`: `#FB923C` (Warm Tangerine Glow)
  - `primary_variant`: `#EA580C` (Deep Terracotta)
  - `primary_container`: `#7C2D12` (Dark Roast Rust)
  - `on_primary_container`: `#FFEDD5` (Soft Peach)
  - `secondary`: `#34D399` (Vibrant Mint Sage)
  - `secondary_container`: `#064E3B` (Deep Emerald Pine)
  - `accent_karma`: `#FBBF24` (Golden Honey)
  - `background`: `#120E0C` (Dark Roast Black)
  - `surface`: `#1E1714` (Elevated Warm Charcoal)
  - `surface_variant`: `#2C221D` (Distinct Card Accent)
  - `text_primary`: `#F5F5F4` (Warm Ivory White)
  - `text_secondary`: `#A8A29E` (Soft Stone Gray)
  - `outline`: `#44362E` (Muted Warm Border)

### Decision 4: Profile UI Theme Selector
- **Choice**: Add an "Appearance & Theme" section in `ProfileFragment` showing the current theme status and a dialog/selector allowing one-tap switching between "System Default (Auto)", "White (Light Mode)", and "Black (Dark Mode)".

## Risks / Trade-offs

- **[Risk]** Activity re-inflation on runtime theme toggle may reset transient UI state.
  - **Mitigation**: ViewModels retain core activity/fragment data, and `ProfileFragment` re-binds cleanly upon configuration reload.
- **[Risk]** Hardcoded color values in drawables causing unreadable text in dark mode.
  - **Mitigation**: Update all drawables (chips, tags, badges) to reference semantic color tokens (`@color/text_primary`, `@color/surface_variant`, etc.).

