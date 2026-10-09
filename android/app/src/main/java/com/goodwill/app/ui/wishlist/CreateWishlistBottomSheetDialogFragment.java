package com.goodwill.app.ui.wishlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.databinding.BottomSheetCreateWishlistBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CreateWishlistBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "CreateWishlistBottomSheet";

    private BottomSheetCreateWishlistBinding binding;
    private WishlistViewModel viewModel;

    private static final String[] CATEGORIES = new String[]{
            "BOOKS", "ELECTRONICS", "STATIONERY", "KITCHENWARE", "CYCLES", "FURNITURE", "FASHION", "OTHER"
    };

    public static CreateWishlistBottomSheetDialogFragment newInstance() {
        return new CreateWishlistBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCreateWishlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(WishlistViewModel.class);

        setupCategoryDropdown();
        setupListeners();
        setupObservers();
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                CATEGORIES
        );
        binding.actvWishlistCategory.setAdapter(adapter);
        binding.actvWishlistCategory.setText(CATEGORIES[0], false);
    }

    private void setupListeners() {
        binding.cbDonationPreferred.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.etMaxBudget.setText("0");
                binding.tilMaxBudget.setVisibility(View.GONE);
            } else {
                binding.tilMaxBudget.setVisibility(View.VISIBLE);
            }
        });

        binding.btnCancelWishlist.setOnClickListener(v -> dismiss());

        binding.btnSubmitWishlist.setOnClickListener(v -> {
            String title = binding.etWishlistTitle.getText() != null ? binding.etWishlistTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                binding.tilWishlistTitle.setError("Item title is required");
                return;
            } else {
                binding.tilWishlistTitle.setError(null);
            }

            String category = binding.actvWishlistCategory.getText() != null ? binding.actvWishlistCategory.getText().toString().trim() : "OTHER";
            String description = binding.etWishlistDesc.getText() != null ? binding.etWishlistDesc.getText().toString().trim() : "";
            boolean isDonationPreferred = binding.cbDonationPreferred.isChecked();

            double maxBudget = 0;
            if (!isDonationPreferred && binding.etMaxBudget.getText() != null) {
                try {
                    maxBudget = Double.parseDouble(binding.etMaxBudget.getText().toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }

            binding.progressCreateWishlist.setVisibility(View.VISIBLE);
            binding.btnSubmitWishlist.setEnabled(false);

            viewModel.createWishlistItem(title, description, category, isDonationPreferred, maxBudget);
        });
    }

    private void setupObservers() {
        viewModel.getItemCreated().observe(getViewLifecycleOwner(), created -> {
            if (created != null) {
                binding.progressCreateWishlist.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Wishlist request posted to campus!", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.progressCreateWishlist.setVisibility(View.GONE);
                binding.btnSubmitWishlist.setEnabled(true);
            }
        });
    }
}
