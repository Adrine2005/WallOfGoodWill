package com.goodwill.app.data.remote;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    public interface TokenProvider {
        String getToken();
    }

    private final TokenProvider tokenProvider;

    public AuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (tokenProvider != null) {
            String token = tokenProvider.getToken();
            if (token != null && !token.trim().isEmpty()) {
                builder.header("Authorization", "Bearer " + token.trim());
            }
        }

        return chain.proceed(builder.build());
    }
}
