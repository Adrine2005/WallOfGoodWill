package com.goodwill.app.ui.listings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.goodwill.app.R;
import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.databinding.FragmentFeedBinding;
import com.goodwill.app.ui.notifications.NotificationsActivity;
import com.goodwill.app.ui.notifications.NotificationsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private ListingsViewModel viewModel;
    private NotificationsViewModel notifViewModel;
    private ListingsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ListingsViewModel.class);
        notifViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        displayUserGreeting();
        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupNotificationsButton();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadFeed();
        notifViewModel.loadUnreadCount();
    }

    private void displayUserGreeting() {
        if (getContext() == null) return;
        SessionManager sessionManager = new SessionManager(requireContext());
        UserDto user = sessionManager.getUser();
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            binding.tvWelcomeStudent.setText(String.format("Hello, %s • %s",
                    user.getName().split(" ")[0],
                    user.getHostelBlock() != null ? user.getHostelBlock() : "Campus"));
        }
    }

    private void setupRecyclerView() {
        adapter = new ListingsAdapter(listing -> {
            Intent intent = new Intent(requireContext(), ListingDetailActivity.class);
            intent.putExtra("extra_listing", listing);
            startActivity(intent);
        });

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvListings.setLayoutManager(layoutManager);
        binding.rvListings.setAdapter(adapter);
    }

    private void setupNotificationsButton() {
        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NotificationsActivity.class);
            startActivity(intent);
        });

        notifViewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                binding.tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            } else {
                binding.tvNotificationBadge.setVisibility(View.GONE);
            }
        });
    }

    private void setupObservers() {
        viewModel.getListings().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            if (list == null || list.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvListings.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvListings.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
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

            new MaterialAlertDialogBuilder(requireContext())
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

