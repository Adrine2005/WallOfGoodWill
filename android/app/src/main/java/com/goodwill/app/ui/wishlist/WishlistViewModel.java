package com.goodwill.app.ui.wishlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.dto.CreateWishlistRequest;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.remote.dto.WishlistItemDto;
import com.goodwill.app.data.repository.RepositoryCallback;
import com.goodwill.app.data.repository.WishlistRepository;

import java.util.Collections;
import java.util.List;

public class WishlistViewModel extends AndroidViewModel {

    private final WishlistRepository repository;
    private final SessionManager sessionManager;

    private final MutableLiveData<List<WishlistItemDto>> items = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<WishlistItemDto>> myItems = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<WishlistItemDto> itemCreated = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> itemDeleted = new MutableLiveData<>(false);

    private String selectedCategory = null;
    private String searchQuery = null;

    public WishlistViewModel(@NonNull Application application) {
        super(application);
        this.repository = new WishlistRepository(application);
        this.sessionManager = new SessionManager(application);
    }

    public WishlistViewModel(@NonNull Application application, WishlistRepository repository, SessionManager sessionManager) {
        super(application);
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    public LiveData<List<WishlistItemDto>> getItems() {
        return items;
    }

    public LiveData<List<WishlistItemDto>> getMyItems() {
        return myItems;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<WishlistItemDto> getItemCreated() {
        return itemCreated;
    }

    public LiveData<Boolean> getItemDeleted() {
        return itemDeleted;
    }

    public String getCurrentUserId() {
        UserDto user = sessionManager.getUser();
        return user != null ? user.getUid() : "";
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void setCategory(String category) {
        this.selectedCategory = (category != null && !category.equalsIgnoreCase("ALL")) ? category : null;
        loadFeed();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        loadFeed();
    }

    public void loadFeed() {
        isLoading.setValue(true);
        repository.getWishlistItems(selectedCategory, searchQuery, new RepositoryCallback<List<WishlistItemDto>>() {
            @Override
            public void onSuccess(List<WishlistItemDto> result) {
                isLoading.setValue(false);
                items.setValue(result != null ? result : Collections.emptyList());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void loadMyItems() {
        isLoading.setValue(true);
        repository.getMyWishlistItems(new RepositoryCallback<List<WishlistItemDto>>() {
            @Override
            public void onSuccess(List<WishlistItemDto> result) {
                isLoading.setValue(false);
                myItems.setValue(result != null ? result : Collections.emptyList());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void createWishlistItem(String title, String description, String category, boolean isDonationPreferred, double maxBudget) {
        if (title == null || title.trim().isEmpty()) {
            errorMessage.setValue("Title is required");
            return;
        }

        isLoading.setValue(true);
        CreateWishlistRequest request = new CreateWishlistRequest(
                title.trim(),
                description != null ? description.trim() : "",
                category != null ? category : "OTHER",
                isDonationPreferred,
                maxBudget
        );

        repository.createWishlistItem(request, new RepositoryCallback<WishlistItemDto>() {
            @Override
            public void onSuccess(WishlistItemDto result) {
                isLoading.setValue(false);
                itemCreated.setValue(result);
                loadFeed();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void deleteWishlistItem(String id) {
        isLoading.setValue(true);
        repository.deleteWishlistItem(id, new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                isLoading.setValue(false);
                itemDeleted.setValue(true);
                loadFeed();
                loadMyItems();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}
