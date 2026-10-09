package com.goodwill.app.ui.wishlist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.WishlistItemDto;
import com.goodwill.app.databinding.ActivityWishlistBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class WishlistActivity extends AppCompatActivity {

    private ActivityWishlistBinding binding;
    private WishlistViewModel viewModel;
    private WishlistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WishlistViewModel.class);

        setupToolbar();
        setupSearchAndFilters();
        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadFeed();
    }

    private void setupToolbar() {
        binding.toolbarWishlist.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshWishlist.setOnRefreshListener(() -> viewModel.loadFeed());
    }

    private void setupSearchAndFilters() {
        binding.etWishlistSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupWishlistCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                viewModel.setCategory(null);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipCatBooks) {
                viewModel.setCategory("BOOKS");
            } else if (checkedId == R.id.chipCatElectronics) {
                viewModel.setCategory("ELECTRONICS");
            } else if (checkedId == R.id.chipCatStationery) {
                viewModel.setCategory("STATIONERY");
            } else if (checkedId == R.id.chipCatKitchen) {
                viewModel.setCategory("KITCHENWARE");
            } else if (checkedId == R.id.chipCatCycles) {
                viewModel.setCategory("CYCLES");
            } else if (checkedId == R.id.chipCatFurniture) {
                viewModel.setCategory("FURNITURE");
            } else if (checkedId == R.id.chipCatOther) {
                viewModel.setCategory("OTHER");
            } else {
                viewModel.setCategory(null);
            }
        });
    }

    private void setupRecyclerView() {
        String currentUserId = viewModel.getCurrentUserId();
        adapter = new WishlistAdapter(currentUserId, new WishlistAdapter.OnWishlistItemClickListener() {
            @Override
            public void onHelpClick(WishlistItemDto item) {
                new MaterialAlertDialogBuilder(WishlistActivity.this)
                        .setTitle("Fulfill Item Request")
                        .setMessage(String.format("You can provide \"%s\" to %s (%s). Would you like to reach out in chat or in person?",
                                item.getTitle(), item.getRequesterName(), item.getRequesterHostel()))
                        .setPositiveButton("Got It", (dialog, which) -> {
                            Toast.makeText(WishlistActivity.this, "Visit " + item.getRequesterHostel() + " Room " + item.getRequesterRoom() + " to coordinate handover!", Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onDeleteClick(WishlistItemDto item) {
                new MaterialAlertDialogBuilder(WishlistActivity.this)
                        .setTitle("Delete Request")
                        .setMessage(String.format("Delete \"%s\"? Has this item been fulfilled?", item.getTitle()))
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.deleteWishlistItem(item.getId());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWishlist.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabPostWishlist.setOnClickListener(v -> {
            CreateWishlistBottomSheetDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), CreateWishlistBottomSheetDialogFragment.TAG);
        });
    }

    private void setupObservers() {
        viewModel.getItems().observe(this, list -> {
            binding.swipeRefreshWishlist.setRefreshing(false);
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyWishlist.setVisibility(View.VISIBLE);
                binding.rvWishlist.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyWishlist.setVisibility(View.GONE);
                binding.rvWishlist.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressWishlist.setVisibility(loading && !binding.swipeRefreshWishlist.isRefreshing() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                binding.swipeRefreshWishlist.setRefreshing(false);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }
}
