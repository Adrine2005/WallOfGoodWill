package com.goodwill.app.ui.points;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.databinding.ActivityLeaderboardBinding;

public class LeaderboardActivity extends AppCompatActivity {

    private ActivityLeaderboardBinding binding;
    private PointsViewModel viewModel;
    private LeaderboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PointsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();

        viewModel.loadLeaderboard();
    }

    private void setupToolbar() {
        binding.toolbarLeaderboard.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshLeaderboard.setOnRefreshListener(() -> viewModel.loadLeaderboard());
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLeaderboard.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getLeaderboard().observe(this, entries -> {
            adapter.submitList(entries);
            boolean isEmpty = entries == null || entries.isEmpty();
            binding.llLeaderboardEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvLeaderboard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            if (!isLoading) {
                binding.swipeRefreshLeaderboard.setRefreshing(false);
            }
            binding.progressBarLeaderboard.setVisibility(isLoading && adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
