package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.AuthResponse;
import com.goodwill.app.data.remote.dto.LoginRequest;
import com.goodwill.app.data.remote.dto.RegisterRequest;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.remote.dto.VerifyQrRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public AuthRepository(Context context) {
        this.sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public AuthRepository(ApiService apiService, SessionManager sessionManager) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public boolean isVerified() {
        return sessionManager.isVerified();
    }

    public UserDto getCurrentUser() {
        return sessionManager.getUser();
    }

    public void register(String name, String email, String password, String hostelBlock,
                         String roomNo, String phone, String inviteCode,
                         RepositoryCallback<AuthResponse> callback) {

        String safeUid = "user-" + Math.abs((email != null ? email : name).hashCode());
        String token = "mock-token-" + safeUid;

        RegisterRequest request = new RegisterRequest(
                safeUid,
                name,
                email,
                password,
                hostelBlock,
                roomNo,
                phone,
                inviteCode
        );

        // Save tentative session token so interceptor sends it
        sessionManager.saveSession(token, null);

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    sessionManager.saveSession(token, authResponse.getUser());
                    callback.onSuccess(authResponse);
                } else {
                    String errorMsg = extractErrorMessage(response);
                    sessionManager.clearSession();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                sessionManager.clearSession();
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void login(String email, String password, RepositoryCallback<UserDto> callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onError("Email is required");
            return;
        }
        if (password == null || password.trim().length() < 6) {
            callback.onError("Password must be at least 6 characters");
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email.trim(), password.trim());
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    String token = authResponse.getToken() != null ? authResponse.getToken() : "mock-token-" + authResponse.getUser().getUid();
                    sessionManager.saveSession(token, authResponse.getUser());
                    callback.onSuccess(authResponse.getUser());
                } else {
                    String errorMsg = extractErrorMessage(response);
                    sessionManager.clearSession();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                sessionManager.clearSession();
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error. Please verify server connection.");
            }
        });
    }

    public void verifyQr(String code, RepositoryCallback<AuthResponse> callback) {
        if (code == null || code.trim().isEmpty()) {
            callback.onError("QR invitation code cannot be empty");
            return;
        }

        apiService.verifyQr(new VerifyQrRequest(code.trim())).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    sessionManager.setVerified(true, code.trim());
                    if (authResponse.getUser() != null) {
                        sessionManager.saveSession(sessionManager.getToken(), authResponse.getUser());
                    }
                    callback.onSuccess(authResponse);
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void checkSession(RepositoryCallback<Boolean> callback) {
        if (!sessionManager.isLoggedIn()) {
            callback.onSuccess(false);
            return;
        }

        // Validate or refresh with backend
        apiService.getProfile().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.saveSession(sessionManager.getToken(), response.body());
                    callback.onSuccess(true);
                } else {
                    // Retain existing local session
                    callback.onSuccess(true);
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                // Offline fallback - trust local session
                callback.onSuccess(true);
            }
        });
    }

    public void logout() {
        sessionManager.clearSession();
    }

    private String extractErrorMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorJson = response.errorBody().string();
                JsonObject obj = gson.fromJson(errorJson, JsonObject.class);
                if (obj != null && obj.has("error")) {
                    return obj.get("error").getAsString();
                }
            } catch (Exception ignored) {
            }
        }
        return "Request failed with HTTP " + response.code();
    }
}
