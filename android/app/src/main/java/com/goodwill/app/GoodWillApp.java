package com.goodwill.app;

import android.app.Application;

import com.goodwill.app.ui.theme.ThemeManager;

/**
 * Custom Application class for Wall of Goodwill.
 * Initializes persistent configurations, including the user's chosen theme mode.
 */
public class GoodWillApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize and apply saved theme preference before any Activity renders
        ThemeManager.init(this);
    }
}

