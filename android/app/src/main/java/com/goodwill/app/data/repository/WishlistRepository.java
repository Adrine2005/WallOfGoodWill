package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.CreateWishlistRequest;
import com.goodwill.app.data.remote.dto.WishlistItemDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public WishlistRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public WishlistRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getWishlistItems(String category, String search, RepositoryCallback<List<WishlistItemDto>> callback) {
        apiService.getWishlistItems(category, search).enqueue(new Callback<List<WishlistItemDto>>() {
            @Override
            public void onResponse(Call<List<WishlistItemDto>> call, Response<List<WishlistItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onSuccess(Collections.emptyList());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<WishlistItemDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getMyWishlistItems(RepositoryCallback<List<WishlistItemDto>> callback) {
        apiService.getMyWishlistItems().enqueue(new Callback<List<WishlistItemDto>>() {
            @Override
            public void onResponse(Call<List<WishlistItemDto>> call, Response<List<WishlistItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onSuccess(Collections.emptyList());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<WishlistItemDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void createWishlistItem(CreateWishlistRequest request, RepositoryCallback<WishlistItemDto> callback) {
        apiService.createWishlistItem(request).enqueue(new Callback<WishlistItemDto>() {
            @Override
            public void onResponse(Call<WishlistItemDto> call, Response<WishlistItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<WishlistItemDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void deleteWishlistItem(String id, RepositoryCallback<String> callback) {
        apiService.deleteWishlistItem(id).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Item deleted");
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
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
