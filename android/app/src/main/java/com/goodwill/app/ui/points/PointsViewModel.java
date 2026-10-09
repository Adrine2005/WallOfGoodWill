package com.goodwill.app.ui.points;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goodwill.app.data.remote.dto.LeaderboardEntryDto;
import com.goodwill.app.data.remote.dto.UserPointsDto;
import com.goodwill.app.data.repository.PointsRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import java.util.Collections;
import java.util.List;

public class PointsViewModel extends AndroidViewModel {

    private final PointsRepository repository;

    private final MutableLiveData<UserPointsDto> userPoints = new MutableLiveData<>(null);
    private final MutableLiveData<List<LeaderboardEntryDto>> leaderboard = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public PointsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PointsRepository(application);
    }

    public PointsViewModel(@NonNull Application application, PointsRepository repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<UserPointsDto> getUserPoints() {
        return userPoints;
    }

    public LiveData<List<LeaderboardEntryDto>> getLeaderboard() {
        return leaderboard;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadMyPoints() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        repository.getMyPoints(new RepositoryCallback<UserPointsDto>() {
            @Override
            public void onSuccess(UserPointsDto result) {
                isLoading.setValue(false);
                userPoints.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void loadLeaderboard() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        repository.getLeaderboard(new RepositoryCallback<List<LeaderboardEntryDto>>() {
            @Override
            public void onSuccess(List<LeaderboardEntryDto> result) {
                isLoading.setValue(false);
                leaderboard.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
