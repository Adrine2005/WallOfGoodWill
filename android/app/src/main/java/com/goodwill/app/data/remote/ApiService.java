package com.goodwill.app.data.remote;

import com.goodwill.app.data.remote.dto.AuthResponse;
import com.goodwill.app.data.remote.dto.RegisterRequest;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.remote.dto.VerifyQrRequest;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("v1/auth/verify-qr")
    Call<AuthResponse> verifyQr(@Body VerifyQrRequest request);

    @GET("v1/auth/me")
    Call<UserDto> getProfile();

    @GET("health")
    Call<Map<String, Object>> getHealth();
}
