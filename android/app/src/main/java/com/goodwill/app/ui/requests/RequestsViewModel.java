package com.goodwill.app.ui.requests;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.DonationRequestDto;
import com.goodwill.app.data.repository.RepositoryCallback;
import com.goodwill.app.data.repository.RequestsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestsViewModel extends AndroidViewModel {

    private final RequestsRepository repository;

    private final MutableLiveData<List<DonationRequestDto>> incomingRequests = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<DonationRequestDto>> listingRequests = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> actionSuccessMessage = new MutableLiveData<>(null);
    private final MutableLiveData<DonationRequestDto> requestSubmitted = new MutableLiveData<>(null);

    public RequestsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new RequestsRepository(application);
    }

    public RequestsViewModel(@NonNull Application application, RequestsRepository repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<List<DonationRequestDto>> getIncomingRequests() {
        return incomingRequests;
    }

    public LiveData<List<DonationRequestDto>> getListingRequests() {
        return listingRequests;
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

    public LiveData<DonationRequestDto> getRequestSubmitted() {
        return requestSubmitted;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void clearActionSuccess() {
        actionSuccessMessage.setValue(null);
    }

    public void clearRequestSubmitted() {
        requestSubmitted.setValue(null);
    }

    public void submitRequest(String listingId, String message) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.createDonationRequest(listingId, message, new RepositoryCallback<DonationRequestDto>() {
            @Override
            public void onSuccess(DonationRequestDto result) {
                isLoading.postValue(false);
                requestSubmitted.postValue(result);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void loadIncomingRequests() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getIncomingRequests(new RepositoryCallback<List<DonationRequestDto>>() {
            @Override
            public void onSuccess(List<DonationRequestDto> result) {
                isLoading.postValue(false);
                incomingRequests.postValue(result != null ? result : Collections.emptyList());
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void loadListingRequests(String listingId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getListingRequests(listingId, new RepositoryCallback<List<DonationRequestDto>>() {
            @Override
            public void onSuccess(List<DonationRequestDto> result) {
                isLoading.postValue(false);
                listingRequests.postValue(result != null ? result : Collections.emptyList());
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void acceptRequest(String requestId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.acceptRequest(requestId, new RepositoryCallback<DonationRequestDto>() {
            @Override
            public void onSuccess(DonationRequestDto result) {
                isLoading.postValue(false);
                actionSuccessMessage.postValue("Request accepted! Donation item marked as donated.");
                // Refresh incoming requests to reflect accepted/auto-declined states
                loadIncomingRequests();
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void declineRequest(String requestId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.declineRequest(requestId, new RepositoryCallback<DonationRequestDto>() {
            @Override
            public void onSuccess(DonationRequestDto result) {
                isLoading.postValue(false);
                actionSuccessMessage.postValue("Request declined.");
                loadIncomingRequests();
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}
