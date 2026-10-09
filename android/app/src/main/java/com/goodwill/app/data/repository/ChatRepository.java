package com.goodwill.app.data.repository;

import android.content.Context;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.ApiService;
import com.goodwill.app.data.remote.dto.ChatConversationDto;
import com.goodwill.app.data.remote.dto.ChatMessageDto;
import com.goodwill.app.data.remote.dto.CreateConversationRequest;
import com.goodwill.app.data.remote.dto.SendMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public ChatRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService(sessionManager);
    }

    public ChatRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void createOrGetConversation(String listingId, RepositoryCallback<ChatConversationDto> callback) {
        apiService.createOrGetConversation(new CreateConversationRequest(listingId))
                .enqueue(new Callback<ChatConversationDto>() {
                    @Override
                    public void onResponse(Call<ChatConversationDto> call, Response<ChatConversationDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(extractErrorMessage(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatConversationDto> call, Throwable t) {
                        callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }

    public void getConversations(RepositoryCallback<List<ChatConversationDto>> callback) {
        apiService.getConversations().enqueue(new Callback<List<ChatConversationDto>>() {
            @Override
            public void onResponse(Call<List<ChatConversationDto>> call, Response<List<ChatConversationDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onSuccess(Collections.emptyList());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<ChatConversationDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getConversationDetail(String conversationId, RepositoryCallback<ChatConversationDto> callback) {
        apiService.getConversationDetail(conversationId).enqueue(new Callback<ChatConversationDto>() {
            @Override
            public void onResponse(Call<ChatConversationDto> call, Response<ChatConversationDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ChatConversationDto> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getMessages(String conversationId, RepositoryCallback<List<ChatMessageDto>> callback) {
        apiService.getMessages(conversationId).enqueue(new Callback<List<ChatMessageDto>>() {
            @Override
            public void onResponse(Call<List<ChatMessageDto>> call, Response<List<ChatMessageDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.isSuccessful()) {
                    callback.onSuccess(Collections.emptyList());
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessageDto>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void sendMessage(String conversationId, String text, RepositoryCallback<ChatMessageDto> callback) {
        apiService.sendMessage(conversationId, new SendMessageRequest(text))
                .enqueue(new Callback<ChatMessageDto>() {
                    @Override
                    public void onResponse(Call<ChatMessageDto> call, Response<ChatMessageDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(extractErrorMessage(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMessageDto> call, Throwable t) {
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
