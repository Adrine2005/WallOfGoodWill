package com.goodwill.app.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.GenerateQrRequest;
import com.goodwill.app.data.remote.dto.InviteDto;
import com.goodwill.app.data.repository.AdminRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import java.util.Collections;
import java.util.List;

public class AdminViewModel extends AndroidViewModel {

    private final AdminRepository repository;

    private final MutableLiveData<List<InviteDto>> invites = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<InviteDto> newlyGeneratedInvite = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>(null);

    public AdminViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AdminRepository(application);
    }

    public AdminViewModel(@NonNull Application application, AdminRepository repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<List<InviteDto>> getInvites() {
        return invites;
    }

    public LiveData<InviteDto> getNewlyGeneratedInvite() {
        return newlyGeneratedInvite;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void loadInvites() {
        isLoading.setValue(true);
        repository.listInvites(new RepositoryCallback<List<InviteDto>>() {
            @Override
            public void onSuccess(List<InviteDto> data) {
                isLoading.setValue(false);
                invites.setValue(data != null ? data : Collections.emptyList());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error != null ? error : "Failed to load campus invites");
            }
        });
    }

    public void generateInvite(String label, long durationSeconds, int maxUses) {
        isLoading.setValue(true);
        GenerateQrRequest request = new GenerateQrRequest(label, durationSeconds, maxUses);
        repository.generateInvite(request, new RepositoryCallback<InviteDto>() {
            @Override
            public void onSuccess(InviteDto data) {
                isLoading.setValue(false);
                newlyGeneratedInvite.setValue(data);
                successMessage.setValue("Generated QR Code: " + data.getCode());
                loadInvites(); // Refresh list to include new invite
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error != null ? error : "Failed to generate invite code");
            }
        });
    }

    public void clearNewInvite() {
        newlyGeneratedInvite.setValue(null);
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }
}
