package com.goodwill.app;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.ui.theme.ThemeManager;

import java.io.InputStream;

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

        // Register custom OkHttpClient with resilient DNS for Glide
        Glide.get(this).getRegistry().replace(
                GlideUrl.class,
                InputStream.class,
                new OkHttpUrlLoader.Factory(ApiClient.getOkHttpClient())
        );
    }
}

