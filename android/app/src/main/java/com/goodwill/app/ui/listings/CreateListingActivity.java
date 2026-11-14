package com.goodwill.app.ui.listings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.goodwill.app.R;
import com.goodwill.app.data.remote.ApiClient;
import com.goodwill.app.data.remote.dto.CreateListingRequest;
import com.goodwill.app.data.remote.dto.EditListingRequest;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.remote.dto.UploadResponseDto;
import com.goodwill.app.data.repository.RepositoryCallback;
import com.goodwill.app.databinding.ActivityCreateListingBinding;
import com.goodwill.app.databinding.ItemUploadPhotoBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateListingActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_LISTING = "extra_edit_listing";
    public static final int MAX_PHOTOS = 5;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private static class UploadedPhoto {
        String localUriOrUrl;
        String serverUrl;
        boolean isUploading;
        boolean isError;

        UploadedPhoto(String localUriOrUrl, String serverUrl, boolean isUploading) {
            this.localUriOrUrl = localUriOrUrl;
            this.serverUrl = serverUrl;
            this.isUploading = isUploading;
            this.isError = false;
        }
    }

    private ActivityCreateListingBinding binding;
    private ListingsViewModel viewModel;
    private boolean isDonation = true;
    private ListingDto editListingItem = null;
    private final List<UploadedPhoto> photoList = new ArrayList<>();

    private final ActivityResultLauncher<String> pickMultipleImagesLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        if (photoList.size() >= MAX_PHOTOS) {
                            Toast.makeText(this, "Maximum " + MAX_PHOTOS + " photos allowed", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        addAndUploadImageUri(uri);
                    }
                }
            });

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
        renderPhotoThumbnails();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initEditModeIfPresent() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EDIT_LISTING)) {
            try {
                editListingItem = (ListingDto) getIntent().getSerializableExtra(EXTRA_EDIT_LISTING);
            } catch (Exception e) {
                editListingItem = null;
            }
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
                    for (String photoUrl : editListingItem.getPhotos()) {
                        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                            photoList.add(new UploadedPhoto(photoUrl, photoUrl, false));
                        }
                    }
                    renderPhotoThumbnails();
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

        binding.btnAddPhotoCard.setOnClickListener(v -> {
            if (photoList.size() >= MAX_PHOTOS) {
                Toast.makeText(this, "Maximum " + MAX_PHOTOS + " photos allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            pickMultipleImagesLauncher.launch("image/*");
        });

        binding.chipPresetBooks.setOnClickListener(v -> {
            addPresetPhoto("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500");
            binding.actvCategory.setText("Books", false);
        });
        binding.chipPresetKettle.setOnClickListener(v -> {
            addPresetPhoto("https://images.unsplash.com/photo-1594213114663-ddf3f2a08126?w=500");
            binding.actvCategory.setText("Kitchenware", false);
        });
        binding.chipPresetCycle.setOnClickListener(v -> {
            addPresetPhoto("https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=500");
            binding.actvCategory.setText("Cycles", false);
        });
        binding.chipPresetLamp.setOnClickListener(v -> {
            addPresetPhoto("https://images.unsplash.com/photo-1534353436294-0dbd4bdac845?w=500");
            binding.actvCategory.setText("Electronics", false);
        });
        binding.chipPresetDrafter.setOnClickListener(v -> {
            addPresetPhoto("https://images.unsplash.com/photo-1585776245991-cf89dd7fc73a?w=500");
            binding.actvCategory.setText("Stationery", false);
        });
        binding.chipPresetDesk.setOnClickListener(v -> {
            addPresetPhoto("https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=500");
            binding.actvCategory.setText("Furniture", false);
        });
    }

    private void addPresetPhoto(String url) {
        if (photoList.size() >= MAX_PHOTOS) {
            Toast.makeText(this, "Maximum " + MAX_PHOTOS + " photos allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        photoList.add(new UploadedPhoto(url, url, false));
        renderPhotoThumbnails();
    }

    private void addAndUploadImageUri(Uri uri) {
        UploadedPhoto photo = new UploadedPhoto(uri.toString(), null, true);
        photoList.add(photo);
        renderPhotoThumbnails();

        ioExecutor.execute(() -> {
            File tempFile = compressAndCopyUriToTempFile(uri);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    if (tempFile != null) tempFile.delete();
                    return;
                }

                if (tempFile == null) {
                    photo.isUploading = false;
                    photo.isError = true;
                    renderPhotoThumbnails();
                    Toast.makeText(CreateListingActivity.this, "Failed to read selected image", Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.uploadImage(tempFile, new RepositoryCallback<UploadResponseDto>() {
                    @Override
                    public void onSuccess(UploadResponseDto result) {
                        photo.isUploading = false;
                        photo.serverUrl = result.getUrl();
                        renderPhotoThumbnails();
                        tempFile.delete();
                    }

                    @Override
                    public void onError(String message) {
                        photo.isUploading = false;
                        photo.isError = true;
                        renderPhotoThumbnails();
                        tempFile.delete();
                        Toast.makeText(CreateListingActivity.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private File compressAndCopyUriToTempFile(Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, options);
            }

            int origWidth = options.outWidth;
            int origHeight = options.outHeight;
            if (origWidth <= 0 || origHeight <= 0) {
                return copyRawUriToTempFile(uri);
            }

            final int maxDim = 1280;
            int inSampleSize = 1;
            while ((origWidth / inSampleSize) > maxDim * 1.5 || (origHeight / inSampleSize) > maxDim * 1.5) {
                inSampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            Bitmap bitmap;
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                bitmap = BitmapFactory.decodeStream(in, null, options);
            }

            if (bitmap == null) {
                return copyRawUriToTempFile(uri);
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxDim || height > maxDim) {
                float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
                int targetWidth = Math.round(width * ratio);
                int targetHeight = Math.round(height * ratio);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                    bitmap = scaled;
                }
            }

            File tempFile = new File(getCacheDir(), "upload_opt_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");
            try (OutputStream out = new FileOutputStream(tempFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out);
                out.flush();
            } finally {
                bitmap.recycle();
            }

            return tempFile;
        } catch (Exception e) {
            return copyRawUriToTempFile(uri);
        }
    }

    private File copyRawUriToTempFile(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            File tempFile = new File(getCacheDir(), "upload_raw_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");
            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private void renderPhotoThumbnails() {
        binding.tvPhotoCount.setText(photoList.size() + " / " + MAX_PHOTOS);
        binding.btnAddPhotoCard.setVisibility(photoList.size() >= MAX_PHOTOS ? View.GONE : View.VISIBLE);
        binding.llPhotoThumbnails.removeAllViews();

        for (int i = 0; i < photoList.size(); i++) {
            final int index = i;
            UploadedPhoto photo = photoList.get(i);

            ItemUploadPhotoBinding itemBinding = ItemUploadPhotoBinding.inflate(
                    getLayoutInflater(),
                    binding.llPhotoThumbnails,
                    false
            );

            // Cover badge on first image
            itemBinding.tvCoverBadge.setVisibility(index == 0 ? View.VISIBLE : View.GONE);

            // Upload progress overlay
            itemBinding.flUploadingOverlay.setVisibility(photo.isUploading ? View.VISIBLE : View.GONE);

            // Load preview with Glide
            String previewUrl = photo.localUriOrUrl;
            if (photo.serverUrl != null && !photo.serverUrl.isEmpty()) {
                previewUrl = ApiClient.resolveImageUrl(photo.serverUrl);
            }
            Glide.with(this)
                    .load(previewUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_placeholder_general)
                    .into(itemBinding.ivPhotoPreview);

            // Remove button
            itemBinding.btnRemovePhoto.setOnClickListener(v -> {
                photoList.remove(index);
                renderPhotoThumbnails();
            });

            binding.llPhotoThumbnails.addView(itemBinding.getRoot());
        }
    }

    private boolean isAnyPhotoUploading() {
        for (UploadedPhoto p : photoList) {
            if (p.isUploading) {
                return true;
            }
        }
        return false;
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

        if (isAnyPhotoUploading()) {
            Toast.makeText(this, "Please wait for photos to finish uploading", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String category = binding.actvCategory.getText() != null ? binding.actvCategory.getText().toString().trim() : "";
        String condition = binding.actvCondition.getText() != null ? binding.actvCondition.getText().toString().trim() : "";
        String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String manualPhotoUrl = binding.etPhotoUrl.getText() != null ? binding.etPhotoUrl.getText().toString().trim() : "";

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

        List<String> photos = new ArrayList<>();
        for (UploadedPhoto p : photoList) {
            if (p.serverUrl != null && !p.serverUrl.isEmpty()) {
                photos.add(p.serverUrl);
            } else if (p.localUriOrUrl != null && (p.localUriOrUrl.startsWith("http://") || p.localUriOrUrl.startsWith("https://") || p.localUriOrUrl.startsWith("/uploads/"))) {
                photos.add(p.localUriOrUrl);
            }
        }
        if (photos.isEmpty() && !TextUtils.isEmpty(manualPhotoUrl)) {
            photos.add(manualPhotoUrl);
        }

        if (editListingItem != null) {
            EditListingRequest editReq = new EditListingRequest(
                    title,
                    desc,
                    formattedCategory,
                    price,
                    formattedCondition,
                    photos
            );
            viewModel.editListing(editListingItem.getId(), editReq);
            return;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
