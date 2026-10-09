package com.goodwill.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.databinding.ActivityProfileBinding;
import com.goodwill.app.ui.auth.LoginActivity;
import com.goodwill.app.ui.chat.ConversationsActivity;
import com.goodwill.app.ui.listings.CreateListingActivity;
import com.goodwill.app.ui.listings.ListingDetailActivity;
import com.goodwill.app.ui.requests.DonationRequestsActivity;
import com.goodwill.app.ui.wishlist.WishlistActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private MyListingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadProfile();
        viewModel.loadMyListings();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MyListingsAdapter(new MyListingsAdapter.OnMyListingActionListener() {
            @Override
            public void onListingClick(ListingDto listing) {
                Intent intent = new Intent(ProfileActivity.this, ListingDetailActivity.class);
                intent.putExtra("extra_listing", listing);
                startActivity(intent);
            }

            @Override
            public void onEdit(ListingDto listing) {
                Intent intent = new Intent(ProfileActivity.this, CreateListingActivity.class);
                intent.putExtra(CreateListingActivity.EXTRA_EDIT_LISTING, listing);
                startActivity(intent);
            }

            @Override
            public void onMarkSold(ListingDto listing) {
                new MaterialAlertDialogBuilder(ProfileActivity.this)
                        .setTitle("Mark as Sold")
                        .setMessage(String.format("Mark \"%s\" as sold? It will no longer appear on the campus feed.", listing.getTitle()))
                        .setPositiveButton("Mark Sold", (dialog, which) -> viewModel.markAsSold(listing.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onMarkDonated(ListingDto listing) {
                new MaterialAlertDialogBuilder(ProfileActivity.this)
                        .setTitle("Mark as Donated")
                        .setMessage(String.format("Mark \"%s\" as successfully donated?", listing.getTitle()))
                        .setPositiveButton("Mark Donated", (dialog, which) -> viewModel.markAsDonated(listing.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onDelete(ListingDto listing) {
                new MaterialAlertDialogBuilder(ProfileActivity.this)
                        .setTitle("Delete Listing")
                        .setMessage(String.format("Are you sure you want to permanently delete \"%s\"?", listing.getTitle()))
                        .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteListing(listing.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyListings.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadMyListings());

        binding.btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DonationRequestsActivity.class);
            startActivity(intent);
        });

        binding.btnViewConversations.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ConversationsActivity.class);
            startActivity(intent);
        });

        binding.btnViewWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        binding.btnCreateFirstListing.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CreateListingActivity.class);
            startActivity(intent);
        });

        binding.btnSignOut.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out from Wall of Goodwill?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        viewModel.logout();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                displayProfile(user);
            }
        });

        viewModel.getMyListings().observe(this, list -> {
            adapter.submitList(list);
            int count = (list != null) ? list.size() : 0;
            binding.tvListingsCount.setText(String.format("%d %s", count, count == 1 ? "item" : "items"));

            if (list == null || list.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvMyListings.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvMyListings.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.swipeRefresh.setRefreshing(loading);
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getActionSuccess().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                viewModel.clearMessages();
            }
        });
    }

    private void displayProfile(UserDto user) {
        String name = user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Student";
        binding.tvProfileName.setText(name);

        String initial = name.substring(0, 1).toUpperCase();
        binding.tvAvatarInitial.setText(initial);

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            binding.tvProfileEmail.setText(String.format("✉ %s", user.getEmail()));
            binding.tvProfileEmail.setVisibility(View.VISIBLE);
        } else {
            binding.tvProfileEmail.setVisibility(View.GONE);
        }

        String hostel = user.getHostelBlock() != null ? user.getHostelBlock() : "Campus Resident";
        String room = user.getRoomNo() != null && !user.getRoomNo().isEmpty() ? " • Room " + user.getRoomNo() : "";
        binding.tvProfileHostel.setText(String.format("📍 %s%s", hostel, room));

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            binding.tvProfilePhone.setText(String.format("📞 %s", user.getPhone()));
            binding.tvProfilePhone.setVisibility(View.VISIBLE);
        } else {
            binding.tvProfilePhone.setVisibility(View.GONE);
        }

        if (user.isVerified()) {
            binding.tvVerifiedStatus.setText("✓ CAMPUS VERIFIED");
            binding.tvVerifiedStatus.setVisibility(View.VISIBLE);
        } else {
            binding.tvVerifiedStatus.setText("⚠ PENDING PHYSICAL QR VERIFICATION");
            binding.tvVerifiedStatus.setVisibility(View.VISIBLE);
        }
    }
}
