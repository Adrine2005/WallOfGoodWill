package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.LeaderboardEntryDto;
import com.goodwill.app.data.remote.dto.UserPointsDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointsRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public PointsRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public PointsRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getMyPoints(RepositoryCallback<UserPointsDto> callback) {
        apiService.getMyPoints().enqueue(new Callback<UserPointsDto>() {
            @Override
            public void onResponse(Call<UserPointsDto> call, Response<UserPointsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<UserPointsDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getLeaderboard(RepositoryCallback<List<LeaderboardEntryDto>> callback) {
        apiService.getLeaderboard().enqueue(new Callback<List<LeaderboardEntryDto>>() {
            @Override
            public void onResponse(Call<List<LeaderboardEntryDto>> call, Response<List<LeaderboardEntryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onSuccess(Collections.emptyList());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<LeaderboardEntryDto>> call, Throwable t) {
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
