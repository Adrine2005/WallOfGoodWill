package com.goodwill.app.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.data.remote.dto.NotificationDto;
import com.goodwill.app.databinding.ActivityNotificationsBinding;
import com.goodwill.app.ui.requests.DonationRequestsActivity;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.OnNotificationClickListener {

    private ActivityNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();

        binding.swipeRefreshNotifications.setOnRefreshListener(() -> viewModel.loadNotifications());
        binding.btnMarkAllRead.setOnClickListener(v -> viewModel.markAllAsRead());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadNotifications();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getNotifications().observe(this, list -> {
            adapter.submitList(list);
            boolean isEmpty = list == null || list.isEmpty();
            binding.layoutEmptyNotifications.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.btnMarkAllRead.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.swipeRefreshNotifications.setRefreshing(loading);
            if (!loading) {
                binding.progressNotifications.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.getActionSuccessMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearActionSuccess();
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationDto notification) {
        if (!notification.isRead()) {
            viewModel.markAsRead(notification.getId());
        }

        if (notification.isRequestReceived()) {
            Intent intent = new Intent(this, DonationRequestsActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, notification.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
