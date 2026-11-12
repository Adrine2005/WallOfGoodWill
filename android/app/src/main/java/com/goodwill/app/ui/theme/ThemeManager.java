package com.goodwill.app.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages application appearance mode (System Default, White / Light Mode, Black / Dark Mode).
 * Persists the selection to SharedPreferences and applies the mode via AppCompatDelegate.
 */
public class ThemeManager {

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private static final String PREF_NAME = "goodwill_theme_prefs";
    private static final String KEY_THEME_MODE = "pref_theme_mode";

    private final SharedPreferences prefs;

    public ThemeManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public void setThemeMode(String mode) {
        String normalizedMode = normalizeMode(mode);
        prefs.edit().putString(KEY_THEME_MODE, normalizedMode).apply();
        applyTheme(normalizedMode);
    }

    public static void applyTheme(String mode) {
        int nightMode = getNightModeFor(mode);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public static int getNightModeFor(String mode) {
        if (MODE_LIGHT.equalsIgnoreCase(mode)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        } else if (MODE_DARK.equalsIgnoreCase(mode)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    public static String normalizeMode(String mode) {
        if (MODE_LIGHT.equalsIgnoreCase(mode)) {
            return MODE_LIGHT;
        } else if (MODE_DARK.equalsIgnoreCase(mode)) {
            return MODE_DARK;
        }
        return MODE_SYSTEM;
    }

    public void applySavedTheme() {
        applyTheme(getThemeMode());
    }

    public static void init(Context context) {
        ThemeManager manager = new ThemeManager(context);
        manager.applySavedTheme();
    }
}

