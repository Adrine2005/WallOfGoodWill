package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.ReportDto;
import com.goodwill.app.data.remote.dto.ReportListingRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModerationRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public ModerationRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public ModerationRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void reportListing(String listingId, String reason, String comment, RepositoryCallback<ReportDto> callback) {
        ReportListingRequest request = new ReportListingRequest(reason, comment);
        apiService.reportListing(listingId, request).enqueue(new Callback<ReportDto>() {
            @Override
            public void onResponse(Call<ReportDto> call, Response<ReportDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ReportDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getListingReports(String listingId, RepositoryCallback<List<ReportDto>> callback) {
        apiService.getListingReports(listingId).enqueue(new Callback<List<ReportDto>>() {
            @Override
            public void onResponse(Call<List<ReportDto>> call, Response<List<ReportDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onSuccess(Collections.emptyList());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<ReportDto>> call, Throwable t) {
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
