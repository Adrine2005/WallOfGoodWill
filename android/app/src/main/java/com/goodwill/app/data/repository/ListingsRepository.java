package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.CreateListingRequest;
import com.goodwill.app.data.remote.dto.EditListingRequest;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UploadResponseDto;
import com.goodwill.app.data.remote.dto.UpdateStatusRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingsRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public ListingsRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public ListingsRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void getListings(String category, String type, String hostelBlock, String search,
                            RepositoryCallback<List<ListingDto>> callback) {
        apiService.getListings(category, type, hostelBlock, search)
                .enqueue(new Callback<List<ListingDto>>() {
                    @Override
                    public void onResponse(Call<List<ListingDto>> call, Response<List<ListingDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(extractErrorMessage(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ListingDto>> call, Throwable t) {
                        callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }

    public void createListing(CreateListingRequest request, RepositoryCallback<ListingDto> callback) {
        apiService.createListing(request).enqueue(new Callback<ListingDto>() {
            @Override
            public void onResponse(Call<ListingDto> call, Response<ListingDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ListingDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void editListing(String id, EditListingRequest request, RepositoryCallback<ListingDto> callback) {
        apiService.editListing(id, request).enqueue(new Callback<ListingDto>() {
            @Override
            public void onResponse(Call<ListingDto> call, Response<ListingDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ListingDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void uploadImage(File file, RepositoryCallback<UploadResponseDto> callback) {
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);
        apiService.uploadImage(body).enqueue(new Callback<UploadResponseDto>() {
            @Override
            public void onResponse(Call<UploadResponseDto> call, Response<UploadResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<UploadResponseDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Failed to upload image");
            }
        });
    }

    public void getListingDetail(String id, RepositoryCallback<ListingDto> callback) {
        apiService.getListingDetail(id).enqueue(new Callback<ListingDto>() {
            @Override
            public void onResponse(Call<ListingDto> call, Response<ListingDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ListingDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getMyListings(RepositoryCallback<List<ListingDto>> callback) {
        apiService.getMyListings().enqueue(new Callback<List<ListingDto>>() {
            @Override
            public void onResponse(Call<List<ListingDto>> call, Response<List<ListingDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<ListingDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void updateListingStatus(String id, String status, RepositoryCallback<Boolean> callback) {
        apiService.updateListingStatus(id, new UpdateStatusRequest(status)).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(true);
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

    public void deleteListing(String id, RepositoryCallback<Boolean> callback) {
        apiService.deleteListing(id).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(true);
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
