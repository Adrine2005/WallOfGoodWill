package com.goodwill.app.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.repository.AuthRepository;
import com.goodwill.app.data.repository.ListingsRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import java.util.Collections;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final ListingsRepository listingsRepository;

    private final MutableLiveData<UserDto> userProfile = new MutableLiveData<>(null);
    private final MutableLiveData<List<ListingDto>> myListings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> actionSuccess = new MutableLiveData<>(null);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
        this.listingsRepository = new ListingsRepository(application);
    }

    public LiveData<UserDto> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<ListingDto>> getMyListings() {
        return myListings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getActionSuccess() {
        return actionSuccess;
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        actionSuccess.setValue(null);
    }

    public void loadProfile() {
        UserDto currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            userProfile.setValue(currentUser);
        }
    }

    public void loadMyListings() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        listingsRepository.getMyListings(new RepositoryCallback<List<ListingDto>>() {
            @Override
            public void onSuccess(List<ListingDto> result) {
                isLoading.setValue(false);
                myListings.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void markAsSold(String listingId) {
        updateStatus(listingId, "SOLD", "Listing marked as SOLD");
    }

    public void markAsDonated(String listingId) {
        updateStatus(listingId, "DONATED", "Listing marked as DONATED");
    }

    public void markAsActive(String listingId) {
        updateStatus(listingId, "ACTIVE", "Listing marked as ACTIVE");
    }

    private void updateStatus(String listingId, String status, String successMsg) {
        isLoading.setValue(true);
        listingsRepository.updateListingStatus(listingId, status, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoading.setValue(false);
                actionSuccess.setValue(successMsg);
                loadMyListings();
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void deleteListing(String listingId) {
        isLoading.setValue(true);
        listingsRepository.deleteListing(listingId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoading.setValue(false);
                actionSuccess.setValue("Listing deleted successfully");
                loadMyListings();
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void logout() {
        authRepository.logout();
    }
}
