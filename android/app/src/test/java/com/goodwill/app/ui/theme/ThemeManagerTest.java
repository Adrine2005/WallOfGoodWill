package com.goodwill.app.ui.theme;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import org.junit.Before;
import org.junit.Test;

public class ThemeManagerTest {

    private Context mockContext;
    private Context mockAppContext;
    private SharedPreferences mockPrefs;
    private SharedPreferences.Editor mockEditor;

    @Before
    public void setup() {
        mockContext = mock(Context.class);
        mockAppContext = mock(Context.class);
        mockPrefs = mock(SharedPreferences.class);
        mockEditor = mock(SharedPreferences.Editor.class);

        when(mockContext.getApplicationContext()).thenReturn(mockAppContext);
        when(mockAppContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
    }

    @Test
    public void testGetNightModeFor_Light() {
        int nightMode = ThemeManager.getNightModeFor(ThemeManager.MODE_LIGHT);
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, nightMode);
    }

    @Test
    public void testGetNightModeFor_Dark() {
        int nightMode = ThemeManager.getNightModeFor(ThemeManager.MODE_DARK);
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, nightMode);
    }

    @Test
    public void testGetNightModeFor_System() {
        int nightMode = ThemeManager.getNightModeFor(ThemeManager.MODE_SYSTEM);
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, nightMode);
    }

    @Test
    public void testGetNightModeFor_Fallback() {
        int nightModeNull = ThemeManager.getNightModeFor(null);
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, nightModeNull);

        int nightModeUnknown = ThemeManager.getNightModeFor("unknown_mode");
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, nightModeUnknown);
    }

    @Test
    public void testNormalizeMode() {
        assertEquals(ThemeManager.MODE_LIGHT, ThemeManager.normalizeMode("LIGHT"));
        assertEquals(ThemeManager.MODE_DARK, ThemeManager.normalizeMode("dark"));
        assertEquals(ThemeManager.MODE_SYSTEM, ThemeManager.normalizeMode("system"));
        assertEquals(ThemeManager.MODE_SYSTEM, ThemeManager.normalizeMode(null));
        assertEquals(ThemeManager.MODE_SYSTEM, ThemeManager.normalizeMode("random"));
    }

    @Test
    public void testGetThemeMode_ReturnsSavedPreference() {
        when(mockPrefs.getString(eq("pref_theme_mode"), eq(ThemeManager.MODE_SYSTEM)))
                .thenReturn(ThemeManager.MODE_DARK);

        ThemeManager manager = new ThemeManager(mockContext);
        assertEquals(ThemeManager.MODE_DARK, manager.getThemeMode());
    }

    @Test
    public void testSetThemeMode_SavesPreference() {
        ThemeManager manager = new ThemeManager(mockContext);
        manager.setThemeMode(ThemeManager.MODE_LIGHT);

        verify(mockEditor).putString("pref_theme_mode", ThemeManager.MODE_LIGHT);
        verify(mockEditor).apply();
    }
}

