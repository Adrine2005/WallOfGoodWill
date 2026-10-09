package com.goodwill.app.ui.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.dto.ChatConversationDto;
import com.goodwill.app.data.remote.dto.ChatMessageDto;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.repository.ChatRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import java.util.Collections;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final SessionManager sessionManager;

    private final MutableLiveData<List<ChatConversationDto>> conversations = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<ChatConversationDto> activeConversation = new MutableLiveData<>(null);
    private final MutableLiveData<List<ChatMessageDto>> messages = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<ChatMessageDto> messageSent = new MutableLiveData<>(null);

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChatRepository(application);
        this.sessionManager = new SessionManager(application);
    }

    public ChatViewModel(@NonNull Application application, ChatRepository repository, SessionManager sessionManager) {
        super(application);
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    public LiveData<List<ChatConversationDto>> getConversations() {
        return conversations;
    }

    public LiveData<ChatConversationDto> getActiveConversation() {
        return activeConversation;
    }

    public LiveData<List<ChatMessageDto>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<ChatMessageDto> getMessageSent() {
        return messageSent;
    }

    public String getCurrentUserId() {
        UserDto user = sessionManager.getUser();
        return user != null ? user.getUid() : "";
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void loadConversations() {
        isLoading.setValue(true);
        repository.getConversations(new RepositoryCallback<List<ChatConversationDto>>() {
            @Override
            public void onSuccess(List<ChatConversationDto> result) {
                isLoading.setValue(false);
                conversations.setValue(result != null ? result : Collections.emptyList());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void openOrCreateConversation(String listingId) {
        isLoading.setValue(true);
        repository.createOrGetConversation(listingId, new RepositoryCallback<ChatConversationDto>() {
            @Override
            public void onSuccess(ChatConversationDto result) {
                isLoading.setValue(false);
                activeConversation.setValue(result);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void loadConversationDetail(String conversationId) {
        repository.getConversationDetail(conversationId, new RepositoryCallback<ChatConversationDto>() {
            @Override
            public void onSuccess(ChatConversationDto result) {
                activeConversation.setValue(result);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void loadMessages(String conversationId) {
        repository.getMessages(conversationId, new RepositoryCallback<List<ChatMessageDto>>() {
            @Override
            public void onSuccess(List<ChatMessageDto> result) {
                messages.setValue(result != null ? result : Collections.emptyList());
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void sendMessage(String conversationId, String text) {
        if (text == null || text.trim().isEmpty()) {
            errorMessage.setValue("Message cannot be empty");
            return;
        }

        repository.sendMessage(conversationId, text.trim(), new RepositoryCallback<ChatMessageDto>() {
            @Override
            public void onSuccess(ChatMessageDto result) {
                messageSent.setValue(result);
                // Immediately refresh message list
                loadMessages(conversationId);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }
}
