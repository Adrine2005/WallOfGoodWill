package com.goodwill.app.ui.listings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.CreateListingRequest;
import com.goodwill.app.data.remote.dto.EditListingRequest;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.databinding.ActivityCreateListingBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CreateListingActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_LISTING = "extra_edit_listing";

    private ActivityCreateListingBinding binding;
    private ListingsViewModel viewModel;
    private boolean isDonation = true;
    private ListingDto editListingItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListingsViewModel.class);

        setupToolbar();
        setupDropdowns();
        setupTypeToggle();
        setupListeners();
        setupObservers();
        initEditModeIfPresent();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initEditModeIfPresent() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EDIT_LISTING)) {
            editListingItem = (ListingDto) getIntent().getSerializableExtra(EXTRA_EDIT_LISTING);
            if (editListingItem != null) {
                binding.toolbar.setTitle("Edit Listing");
                binding.btnSubmitPost.setText("Save Changes");

                binding.etTitle.setText(editListingItem.getTitle());
                binding.etDescription.setText(editListingItem.getDescription());
                if (editListingItem.getCategory() != null) {
                    binding.actvCategory.setText(editListingItem.getCategory(), false);
                }
                if (editListingItem.getCondition() != null) {
                    binding.actvCondition.setText(editListingItem.getCondition().replace("_", " "), false);
                }

                if (!editListingItem.isDonation()) {
                    isDonation = false;
                    binding.toggleGroupType.check(R.id.btnTypeSell);
                    binding.tilPrice.setVisibility(View.VISIBLE);
                    binding.etPrice.setText(String.format(Locale.US, "%.0f", editListingItem.getPrice()));
                } else {
                    isDonation = true;
                    binding.toggleGroupType.check(R.id.btnTypeDonate);
                    binding.tilPrice.setVisibility(View.GONE);
                }

                // Prevent type change during edit
                binding.btnTypeDonate.setEnabled(false);
                binding.btnTypeSell.setEnabled(false);

                if (editListingItem.getPhotos() != null && !editListingItem.getPhotos().isEmpty()) {
                    binding.etPhotoUrl.setText(editListingItem.getPhotos().get(0));
                }
            }
        }
    }

    private void setupDropdowns() {
        String[] categories = getResources().getStringArray(R.array.item_categories);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        binding.actvCategory.setAdapter(catAdapter);

        String[] conditions = getResources().getStringArray(R.array.item_conditions);
        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                conditions
        );
        binding.actvCondition.setAdapter(condAdapter);
    }

    private void setupTypeToggle() {
        binding.toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTypeDonate) {
                    isDonation = true;
                    binding.tilPrice.setVisibility(View.GONE);
                } else if (checkedId == R.id.btnTypeSell) {
                    isDonation = false;
                    binding.tilPrice.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupListeners() {
        binding.btnSubmitPost.setOnClickListener(v -> attemptSubmit());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnSubmitPost.setEnabled(!loading);
            binding.progressPost.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvFormError.setText(error);
                binding.tvFormError.setVisibility(View.VISIBLE);
            } else {
                binding.tvFormError.setVisibility(View.GONE);
            }
        });

        viewModel.getCreateSuccess().observe(this, createdItem -> {
            if (createdItem != null) {
                Toast.makeText(this, "Item posted to campus successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getEditSuccess().observe(this, updatedItem -> {
            if (updatedItem != null) {
                Toast.makeText(this, "Listing updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void attemptSubmit() {
        binding.tilTitle.setError(null);
        binding.tilCategory.setError(null);
        binding.tilCondition.setError(null);
        binding.tilPrice.setError(null);
        binding.tilDescription.setError(null);
        binding.tvFormError.setVisibility(View.GONE);

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String category = binding.actvCategory.getText() != null ? binding.actvCategory.getText().toString().trim() : "";
        String condition = binding.actvCondition.getText() != null ? binding.actvCondition.getText().toString().trim() : "";
        String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String photoUrl = binding.etPhotoUrl.getText() != null ? binding.etPhotoUrl.getText().toString().trim() : "";

        boolean cancel = false;

        if (TextUtils.isEmpty(title)) {
            binding.tilTitle.setError("Title is required");
            cancel = true;
        }

        if (TextUtils.isEmpty(category)) {
            binding.tilCategory.setError("Please select a category");
            cancel = true;
        }

        if (TextUtils.isEmpty(condition)) {
            binding.tilCondition.setError("Please select the item condition");
            cancel = true;
        }

        double price = 0;
        if (!isDonation) {
            String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    binding.tilPrice.setError("Selling price must be greater than ₹0");
                    cancel = true;
                }
            } catch (Exception e) {
                binding.tilPrice.setError("Please enter a valid price");
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(desc)) {
            binding.tilDescription.setError("Description is required");
            cancel = true;
        }

        if (cancel) {
            return;
        }

        String formattedCategory = category.toUpperCase().replace(" ", "_");
        String formattedCondition = condition.toUpperCase().replace(" ", "_");

        if (editListingItem != null) {
            EditListingRequest editReq = new EditListingRequest(
                    title,
                    desc,
                    formattedCategory,
                    price,
                    formattedCondition
            );
            viewModel.editListing(editListingItem.getId(), editReq);
            return;
        }

        List<String> photos = new ArrayList<>();
        if (!TextUtils.isEmpty(photoUrl)) {
            photos.add(photoUrl);
        }

        String itemType = isDonation ? "DONATE" : "SELL";

        CreateListingRequest req = new CreateListingRequest(
                title,
                desc,
                formattedCategory,
                itemType,
                price,
                formattedCondition,
                photos
        );

        viewModel.createListing(req);
    }
}
