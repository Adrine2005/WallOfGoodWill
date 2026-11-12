package com.goodwill.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.databinding.FragmentProfileBinding;
import com.goodwill.app.ui.admin.AdminQrActivity;
import com.goodwill.app.ui.auth.LoginActivity;
import com.goodwill.app.ui.chat.ConversationsActivity;
import com.goodwill.app.ui.listings.CreateListingActivity;
import com.goodwill.app.ui.listings.ListingDetailActivity;
import com.goodwill.app.ui.points.LeaderboardActivity;
import com.goodwill.app.ui.points.PointsViewModel;
import com.goodwill.app.ui.requests.DonationRequestsActivity;
import com.goodwill.app.ui.theme.ThemeManager;
import com.goodwill.app.ui.wishlist.WishlistActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private PointsViewModel pointsViewModel;
    private MyListingsAdapter adapter;
    private ThemeManager themeManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        pointsViewModel = new ViewModelProvider(requireActivity()).get(PointsViewModel.class);
        themeManager = new ThemeManager(requireContext());

        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadProfile();
        viewModel.loadMyListings();
        pointsViewModel.loadMyPoints();
        updateThemeDisplay();
    }

    private void setupRecyclerView() {
        adapter = new MyListingsAdapter(new MyListingsAdapter.OnMyListingActionListener() {
            @Override
            public void onListingClick(ListingDto listing) {
                Intent intent = new Intent(requireContext(), ListingDetailActivity.class);
                intent.putExtra("extra_listing", listing);
                startActivity(intent);
            }

            @Override
            public void onEdit(ListingDto listing) {
                Intent intent = new Intent(requireContext(), CreateListingActivity.class);
                intent.putExtra(CreateListingActivity.EXTRA_EDIT_LISTING, listing);
                startActivity(intent);
            }

            @Override
            public void onMarkSold(ListingDto listing) {
                if (getContext() == null) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Mark as Sold")
                        .setMessage(String.format("Mark \"%s\" as sold? It will no longer appear on the campus feed.", listing.getTitle()))
                        .setPositiveButton("Mark Sold", (dialog, which) -> viewModel.markAsSold(listing.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onMarkDonated(ListingDto listing) {
                if (getContext() == null) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Mark as Donated")
                        .setMessage(String.format("Mark \"%s\" as successfully donated?", listing.getTitle()))
                        .setPositiveButton("Mark Donated", (dialog, which) -> viewModel.markAsDonated(listing.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onDelete(ListingDto listing) {
                if (getContext() == null) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Listing")
                        .setMessage(String.format("Are you sure you want to permanently delete \"%s\"?", listing.getTitle()))
                        .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteListing(listing.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.rvMyListings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyListings.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadMyListings());

        binding.btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DonationRequestsActivity.class);
            startActivity(intent);
        });

        binding.btnViewConversations.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ConversationsActivity.class);
            startActivity(intent);
        });

        binding.btnViewWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WishlistActivity.class);
            startActivity(intent);
        });

        binding.btnViewLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LeaderboardActivity.class);
            startActivity(intent);
        });

        binding.btnCreateFirstListing.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateListingActivity.class);
            startActivity(intent);
        });

        binding.btnManageAdminQr.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AdminQrActivity.class);
            startActivity(intent);
        });

        View.OnClickListener themeClickListener = v -> showThemeSelectionDialog();
        binding.btnSelectTheme.setOnClickListener(themeClickListener);
        binding.cardThemeSettings.setOnClickListener(themeClickListener);

        binding.btnSignOut.setOnClickListener(v -> {
            if (getContext() == null) return;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out from Wall of Goodwill?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        viewModel.logout();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                displayProfile(user);
            }
        });

        viewModel.getMyListings().observe(getViewLifecycleOwner(), list -> {
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

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.swipeRefresh.setRefreshing(loading);
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty() && getView() != null) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getView() != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                viewModel.clearMessages();
            }
        });

        pointsViewModel.getUserPoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null) {
                binding.tvProfileKarmaScore.setText(points.getPoints() + " pts");
                binding.tvProfileTierName.setText(points.getTierName() != null ? points.getTierName() : "Newcomer Donor");
                int donations = points.getDonationCount();
                binding.tvProfileDonationsCount.setText(donations + (donations == 1 ? " donation completed" : " donations completed"));

                String icon = "🌱";
                if (points.getBadges() != null && !points.getBadges().isEmpty()) {
                    icon = points.getBadges().get(points.getBadges().size() - 1).getIcon();
                }
                binding.tvProfileBadgeIcon.setText(icon);

                binding.progressTierNext.setProgress(points.getProgressPercent());

                if (points.getPoints() >= 250 || points.getDonationCount() >= 5) {
                    binding.tvNextTierHint.setText("Max tier achieved! Outstanding contribution to zero campus waste 🌟");
                } else {
                    int remaining = points.getNextTierPoints() - points.getPoints();
                    if (remaining < 0) remaining = 0;
                    binding.tvNextTierHint.setText(String.format("%d pts to next badge & tier milestone", remaining));
                }
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

        boolean isAdmin = user != null && "admin".equalsIgnoreCase(user.getRole());
        binding.cardAdminConsole.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void updateThemeDisplay() {
        if (binding == null || themeManager == null) return;
        String mode = themeManager.getThemeMode();
        String label;
        if (ThemeManager.MODE_LIGHT.equalsIgnoreCase(mode)) {
            label = "Current: White (Light Mode)";
        } else if (ThemeManager.MODE_DARK.equalsIgnoreCase(mode)) {
            label = "Current: Black (Dark Mode)";
        } else {
            label = "Current: System Default (Auto)";
        }
        binding.tvCurrentThemeLabel.setText(label);
    }

    private void showThemeSelectionDialog() {
        if (getContext() == null || themeManager == null) return;

        String[] options = new String[]{
                "⚙ System Default (Auto)",
                "☀ White (Light Mode)",
                "🌙 Black (Dark Mode)"
        };

        String currentMode = themeManager.getThemeMode();
        int checkedItem = 0;
        if (ThemeManager.MODE_LIGHT.equalsIgnoreCase(currentMode)) {
            checkedItem = 1;
        } else if (ThemeManager.MODE_DARK.equalsIgnoreCase(currentMode)) {
            checkedItem = 2;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Appearance Theme")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String selectedMode;
                    if (which == 1) {
                        selectedMode = ThemeManager.MODE_LIGHT;
                    } else if (which == 2) {
                        selectedMode = ThemeManager.MODE_DARK;
                    } else {
                        selectedMode = ThemeManager.MODE_SYSTEM;
                    }
                    themeManager.setThemeMode(selectedMode);
                    updateThemeDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

