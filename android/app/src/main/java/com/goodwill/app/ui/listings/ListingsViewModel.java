package com.goodwill.app.ui.listings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.CreateListingRequest;
import com.goodwill.app.data.remote.dto.EditListingRequest;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UploadResponseDto;
import com.goodwill.app.data.repository.ListingsRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ListingsViewModel extends AndroidViewModel {

    private final ListingsRepository repository;

    private final MutableLiveData<List<ListingDto>> listings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<ListingDto> createSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<ListingDto> editSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<ListingDto> selectedListing = new MutableLiveData<>(null);

    private String currentCategory = null;
    private String currentType = null;
    private String currentHostelBlock = null;
    private String currentSearch = null;

    public ListingsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ListingsRepository(application);
    }

    public LiveData<List<ListingDto>> getListings() {
        return listings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<ListingDto> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<ListingDto> getEditSuccess() {
        return editSuccess;
    }

    public LiveData<ListingDto> getSelectedListing() {
        return selectedListing;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void setCategory(String category) {
        this.currentCategory = category;
        loadFeed();
    }

    public void setType(String type) {
        this.currentType = type;
        loadFeed();
    }

    public void setHostelBlock(String hostelBlock) {
        this.currentHostelBlock = hostelBlock;
        loadFeed();
    }

    public void setSearchQuery(String search) {
        this.currentSearch = search;
        loadFeed();
    }

    public void loadFeed() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.getListings(currentCategory, currentType, currentHostelBlock, currentSearch,
                new RepositoryCallback<List<ListingDto>>() {
                    @Override
                    public void onSuccess(List<ListingDto> result) {
                        isLoading.setValue(false);
                        listings.setValue(result);
                    }

                    @Override
                    public void onError(String message) {
                        isLoading.setValue(false);
                        errorMessage.setValue(message);
                    }
                });
    }

    public void createListing(CreateListingRequest request) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.createListing(request, new RepositoryCallback<ListingDto>() {
            @Override
            public void onSuccess(ListingDto result) {
                isLoading.setValue(false);
                createSuccess.setValue(result);
                loadFeed(); // Refresh home feed
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void editListing(String id, EditListingRequest request) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        repository.editListing(id, request, new RepositoryCallback<ListingDto>() {
            @Override
            public void onSuccess(ListingDto result) {
                isLoading.setValue(false);
                editSuccess.setValue(result);
                loadFeed();
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void loadDetail(String id) {
        isLoading.setValue(true);
        repository.getListingDetail(id, new RepositoryCallback<ListingDto>() {
            @Override
            public void onSuccess(ListingDto result) {
                isLoading.setValue(false);
                selectedListing.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void uploadImage(File file, RepositoryCallback<UploadResponseDto> callback) {
        repository.uploadImage(file, callback);
    }
}
