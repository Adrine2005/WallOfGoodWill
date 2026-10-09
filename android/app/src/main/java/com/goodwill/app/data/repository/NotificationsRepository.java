package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.NotificationDto;
import com.goodwill.app.data.remote.dto.UnreadCountDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public NotificationsRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public NotificationsRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getNotifications(RepositoryCallback<List<NotificationDto>> callback) {
        apiService.getNotifications().enqueue(new Callback<List<NotificationDto>>() {
            @Override
            public void onResponse(Call<List<NotificationDto>> call, Response<List<NotificationDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<NotificationDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getUnreadCount(RepositoryCallback<Integer> callback) {
        apiService.getUnreadCount().enqueue(new Callback<UnreadCountDto>() {
            @Override
            public void onResponse(Call<UnreadCountDto> call, Response<UnreadCountDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getCount());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<UnreadCountDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void markAsRead(String id, RepositoryCallback<NotificationDto> callback) {
        apiService.markNotificationAsRead(id).enqueue(new Callback<NotificationDto>() {
            @Override
            public void onResponse(Call<NotificationDto> call, Response<NotificationDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<NotificationDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void markAllAsRead(RepositoryCallback<Integer> callback) {
        apiService.markAllNotificationsAsRead().enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer count = response.body().get("marked");
                    callback.onSuccess(count != null ? count : 0);
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
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
