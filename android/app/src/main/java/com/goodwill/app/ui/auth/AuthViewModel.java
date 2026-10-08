package com.goodwill.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.AuthResponse;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.repository.AuthRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<UserDto> loginSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<AuthResponse> registerSuccess = new MutableLiveData<>(null);
    private final MutableLiveData<AuthResponse> qrVerifySuccess = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> sessionActive = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isVerified = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
        this.isVerified.setValue(authRepository.isVerified());
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<UserDto> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<AuthResponse> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<AuthResponse> getQrVerifySuccess() {
        return qrVerifySuccess;
    }

    public LiveData<Boolean> getSessionActive() {
        return sessionActive;
    }

    public LiveData<Boolean> getIsVerified() {
        return isVerified;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public boolean isCurrentlyVerified() {
        return authRepository.isVerified();
    }

    public boolean isCurrentlyLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public UserDto getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void checkSession() {
        isLoading.setValue(true);
        authRepository.checkSession(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean active) {
                isLoading.setValue(false);
                isVerified.setValue(authRepository.isVerified());
                sessionActive.setValue(active);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                sessionActive.setValue(false);
            }
        });
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.login(email, password, new RepositoryCallback<UserDto>() {
            @Override
            public void onSuccess(UserDto user) {
                isLoading.setValue(false);
                isVerified.setValue(user != null && user.isVerified());
                loginSuccess.setValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void register(String name, String email, String password, String hostelBlock,
                         String roomNo, String phone, String inviteCode) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.register(name, email, password, hostelBlock, roomNo, phone, inviteCode,
                new RepositoryCallback<AuthResponse>() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        isLoading.setValue(false);
                        isVerified.setValue(response != null && response.isVerified());
                        registerSuccess.setValue(response);
                    }

                    @Override
                    public void onError(String message) {
                        isLoading.setValue(false);
                        errorMessage.setValue(message);
                    }
                });
    }

    public void verifyQr(String code) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.verifyQr(code, new RepositoryCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse response) {
                isLoading.setValue(false);
                isVerified.setValue(true);
                qrVerifySuccess.setValue(response);
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
        isVerified.setValue(false);
        sessionActive.setValue(false);
    }
}
