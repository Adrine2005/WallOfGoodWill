package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.CreateDonationRequest;
import com.goodwill.app.data.remote.dto.DonationRequestDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public RequestsRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public RequestsRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void createDonationRequest(String listingId, String message, RepositoryCallback<DonationRequestDto> callback) {
        CreateDonationRequest request = new CreateDonationRequest(message);
        apiService.createDonationRequest(listingId, request).enqueue(new Callback<DonationRequestDto>() {
            @Override
            public void onResponse(Call<DonationRequestDto> call, Response<DonationRequestDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<DonationRequestDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getListingRequests(String listingId, RepositoryCallback<List<DonationRequestDto>> callback) {
        apiService.getListingRequests(listingId).enqueue(new Callback<List<DonationRequestDto>>() {
            @Override
            public void onResponse(Call<List<DonationRequestDto>> call, Response<List<DonationRequestDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequestDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getIncomingRequests(RepositoryCallback<List<DonationRequestDto>> callback) {
        apiService.getIncomingRequests().enqueue(new Callback<List<DonationRequestDto>>() {
            @Override
            public void onResponse(Call<List<DonationRequestDto>> call, Response<List<DonationRequestDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequestDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void acceptRequest(String requestId, RepositoryCallback<DonationRequestDto> callback) {
        apiService.acceptRequest(requestId).enqueue(new Callback<DonationRequestDto>() {
            @Override
            public void onResponse(Call<DonationRequestDto> call, Response<DonationRequestDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<DonationRequestDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void declineRequest(String requestId, RepositoryCallback<DonationRequestDto> callback) {
        apiService.declineRequest(requestId).enqueue(new Callback<DonationRequestDto>() {
            @Override
            public void onResponse(Call<DonationRequestDto> call, Response<DonationRequestDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<DonationRequestDto> call, Throwable t) {
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
