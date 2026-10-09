package com.goodwill.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.databinding.ActivityMainBinding;
import com.goodwill.app.ui.listings.CreateListingActivity;
import com.goodwill.app.ui.listings.ListingDetailActivity;
import com.goodwill.app.ui.listings.ListingsAdapter;
import com.goodwill.app.ui.listings.ListingsViewModel;
import com.goodwill.app.ui.notifications.NotificationsActivity;
import com.goodwill.app.ui.notifications.NotificationsViewModel;
import com.goodwill.app.ui.profile.ProfileActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ListingsViewModel viewModel;
    private NotificationsViewModel notifViewModel;
    private ListingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListingsViewModel.class);
        notifViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        displayUserGreeting();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupFab();
        setupProfileButton();
        setupNotificationsButton();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadFeed();
        notifViewModel.loadUnreadCount();
    }

    private void setupNotificationsButton() {
        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        notifViewModel.getUnreadCount().observe(this, count -> {
            if (count != null && count > 0) {
                binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                binding.tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            } else {
                binding.tvNotificationBadge.setVisibility(View.GONE);
            }
        });
    }

    private void displayUserGreeting() {
        SessionManager sessionManager = new SessionManager(this);
        UserDto user = sessionManager.getUser();
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            binding.tvWelcomeStudent.setText(String.format("Hello, %s • %s",
                    user.getName().split(" ")[0],
                    user.getHostelBlock() != null ? user.getHostelBlock() : "Campus"));
        }
    }

    private void setupRecyclerView() {
        adapter = new ListingsAdapter(listing -> {
            Intent intent = new Intent(MainActivity.this, ListingDetailActivity.class);
            intent.putExtra("extra_listing", listing);
            startActivity(intent);
        });

        binding.rvListings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvListings.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getListings().observe(this, list -> {
            adapter.submitList(list);
            if (list == null || list.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvListings.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvListings.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressFeed.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupFilterChips() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                viewModel.setCategory(null);
                viewModel.setType(null);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                viewModel.setCategory(null);
                viewModel.setType(null);
            } else if (checkedId == R.id.chipDonate) {
                viewModel.setCategory(null);
                viewModel.setType("DONATE");
            } else if (checkedId == R.id.chipSell) {
                viewModel.setCategory(null);
                viewModel.setType("SELL");
            } else if (checkedId == R.id.chipBooks) {
                viewModel.setType(null);
                viewModel.setCategory("BOOKS");
            } else if (checkedId == R.id.chipElectronics) {
                viewModel.setType(null);
                viewModel.setCategory("ELECTRONICS");
            } else if (checkedId == R.id.chipCycles) {
                viewModel.setType(null);
                viewModel.setCategory("CYCLES");
            } else if (checkedId == R.id.chipKitchenware) {
                viewModel.setType(null);
                viewModel.setCategory("KITCHENWARE");
            } else if (checkedId == R.id.chipFurniture) {
                viewModel.setType(null);
                viewModel.setCategory("FURNITURE");
            }
        });

        binding.chipHostelFilter.setOnClickListener(v -> {
            String[] hostelOptions = new String[] {
                    "All Hostels",
                    "Hostel Block A (Boys)",
                    "Hostel Block B (Boys)",
                    "Hostel Block C (Girls)",
                    "Hostel Block D (Girls)",
                    "Day Scholar / Off-Campus"
            };

            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Filter by Campus Location")
                    .setItems(hostelOptions, (dialog, which) -> {
                        if (which == 0) {
                            binding.chipHostelFilter.setText("📍 Hostels: All");
                            viewModel.setHostelBlock(null);
                        } else {
                            String choice = hostelOptions[which];
                            binding.chipHostelFilter.setText("📍 " + choice.replace("Hostel ", ""));
                            viewModel.setHostelBlock(choice);
                        }
                    })
                    .show();
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString().trim() : null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String q = binding.etSearch.getText() != null ? binding.etSearch.getText().toString().trim() : "";
                viewModel.setSearchQuery(q);
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        binding.fabPostListing.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateListingActivity.class));
        });
    }

    private void setupProfileButton() {
        binding.btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
    }
}
