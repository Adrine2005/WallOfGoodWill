package com.goodwill.app.data.remote;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";

    private static ApiService apiService;
    private static String baseUrl = DEFAULT_BASE_URL;

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
