package com.goodwill.app.data.remote;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String EMULATOR_BASE_URL = "http://10.0.2.2:8080/";
    public static final String DEVICE_BASE_URL = "http://127.0.0.1:8080/";
    public static final String DEFAULT_BASE_URL = isEmulator() ? EMULATOR_BASE_URL : DEVICE_BASE_URL;

    private static ApiService apiService;
    private static String baseUrl = DEFAULT_BASE_URL;

    public static boolean isEmulator() {
        String fp = android.os.Build.FINGERPRINT != null ? android.os.Build.FINGERPRINT.toLowerCase() : "";
        String model = android.os.Build.MODEL != null ? android.os.Build.MODEL.toLowerCase() : "";
        String hardware = android.os.Build.HARDWARE != null ? android.os.Build.HARDWARE.toLowerCase() : "";
        String product = android.os.Build.PRODUCT != null ? android.os.Build.PRODUCT.toLowerCase() : "";
        String brand = android.os.Build.BRAND != null ? android.os.Build.BRAND.toLowerCase() : "";
        String device = android.os.Build.DEVICE != null ? android.os.Build.DEVICE.toLowerCase() : "";
        String board = android.os.Build.BOARD != null ? android.os.Build.BOARD.toLowerCase() : "";

        return fp.startsWith("generic")
                || fp.startsWith("unknown")
                || fp.contains("sdk_gphone")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("sdk_gphone")
                || model.contains("android sdk built")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || board.contains("goldfish")
                || product.contains("sdk_gphone")
                || product.contains("emu64")
                || product.contains("google_sdk")
                || (brand.startsWith("generic") && device.startsWith("generic"));
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String resolveImageUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            return photoUrl;
        }
        if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
            return photoUrl;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (!photoUrl.startsWith("/")) {
            photoUrl = "/" + photoUrl;
        }
        return base + photoUrl;
    }

    public static void setBaseUrl(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        baseUrl = url;
        apiService = null; // Recreate on next call
    }

    public static synchronized ApiService getApiService(AuthInterceptor.TokenProvider tokenProvider) {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(logging);

            if (tokenProvider != null) {
                clientBuilder.addInterceptor(new AuthInterceptor(tokenProvider));
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
