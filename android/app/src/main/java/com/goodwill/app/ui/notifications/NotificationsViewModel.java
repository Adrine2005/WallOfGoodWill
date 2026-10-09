package com.goodwill.app.ui.notifications;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.NotificationDto;
import com.goodwill.app.data.repository.NotificationsRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {

    private final NotificationsRepository repository;

    private final MutableLiveData<List<NotificationDto>> notifications = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> actionSuccessMessage = new MutableLiveData<>(null);

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NotificationsRepository(application);
    }

    public NotificationsViewModel(@NonNull Application application, NotificationsRepository repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<List<NotificationDto>> getNotifications() {
        return notifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getActionSuccessMessage() {
        return actionSuccessMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void clearActionSuccess() {
        actionSuccessMessage.setValue(null);
    }

    public void loadNotifications() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getNotifications(new RepositoryCallback<List<NotificationDto>>() {
            @Override
            public void onSuccess(List<NotificationDto> result) {
                isLoading.postValue(false);
                notifications.postValue(result != null ? result : Collections.emptyList());

                // Calculate unread count from fetched list
                int unread = 0;
                if (result != null) {
                    for (NotificationDto n : result) {
                        if (!n.isRead()) unread++;
                    }
                }
                unreadCount.postValue(unread);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void loadUnreadCount() {
        repository.getUnreadCount(new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                unreadCount.postValue(count != null ? count : 0);
            }

            @Override
            public void onError(String message) {
                // Fail silently for background badge check
            }
        });
    }

    public void markAsRead(String id) {
        repository.markAsRead(id, new RepositoryCallback<NotificationDto>() {
            @Override
            public void onSuccess(NotificationDto result) {
                List<NotificationDto> current = notifications.getValue();
                if (current != null) {
                    List<NotificationDto> updated = new ArrayList<>(current);
                    for (int i = 0; i < updated.size(); i++) {
                        if (updated.get(i).getId().equals(id)) {
                            NotificationDto copy = updated.get(i);
                            copy.setRead(true);
                            updated.set(i, copy);
                            break;
                        }
                    }
                    notifications.postValue(updated);
                }

                Integer currentCount = unreadCount.getValue();
                if (currentCount != null && currentCount > 0) {
                    unreadCount.postValue(currentCount - 1);
                }
            }

            @Override
            public void onError(String message) {
                // non-blocking
            }
        });
    }

    public void markAllAsRead() {
        isLoading.setValue(true);
        repository.markAllAsRead(new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                isLoading.postValue(false);
                List<NotificationDto> current = notifications.getValue();
                if (current != null) {
                    List<NotificationDto> updated = new ArrayList<>();
                    for (NotificationDto n : current) {
                        n.setRead(true);
                        updated.add(n);
                    }
                    notifications.postValue(updated);
                }
                unreadCount.postValue(0);
                actionSuccessMessage.postValue("All notifications marked as read");
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}
