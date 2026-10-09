package com.goodwill.app.ui.listings;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.databinding.ActivityListingDetailBinding;

import com.goodwill.app.data.local.SessionManager;
import com.goodwill.app.ui.chat.ChatActivity;
import com.goodwill.app.ui.requests.RequestItemBottomSheetDialogFragment;

public class ListingDetailActivity extends AppCompatActivity {

    private ActivityListingDetailBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        setupToolbar();

        ListingDto item = (ListingDto) getIntent().getSerializableExtra("extra_listing");
        if (item != null) {
            displayDetails(item);
        } else {
            Toast.makeText(this, "Listing details unavailable", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayDetails(ListingDto item) {
        binding.tvDetailTitle.setText(item.getTitle());
        binding.tvDetailDescription.setText(item.getDescription());

        boolean isOwner = sessionManager.getUser() != null &&
                sessionManager.getUser().getUid().equals(item.getSellerId());
        boolean isClosed = "DONATED".equalsIgnoreCase(item.getStatus()) || "SOLD".equalsIgnoreCase(item.getStatus());

        // Chat Action Button configuration
        if (isOwner || isClosed) {
            binding.btnChatSeller.setVisibility(View.GONE);
        } else {
            binding.btnChatSeller.setVisibility(View.VISIBLE);
            binding.btnChatSeller.setOnClickListener(v -> {
                ChatActivity.startWithListing(this, item.getId(), item.getTitle());
            });
        }

        // Price & Badges
        if (item.isDonation()) {
            binding.tvDetailPrice.setText("Free Donation");
            binding.tvDetailPrice.setTextColor(ContextCompat.getColor(this, R.color.success));
            binding.tvDetailBadgeType.setText(isClosed ? "DONATED" : "DONATE • Free");
            binding.tvDetailBadgeType.setBackground(ContextCompat.getDrawable(this,
                    isClosed ? R.drawable.bg_badge_sold : R.drawable.bg_badge_donate));

            if (isClosed) {
                binding.btnContactSeller.setEnabled(false);
                binding.btnContactSeller.setText("Item Already Donated");
                binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.text_secondary));
            } else if (isOwner) {
                binding.btnContactSeller.setText("You are the Donor (View Requests)");
                binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary));
                binding.btnContactSeller.setOnClickListener(v -> {
                    Toast.makeText(this, "Check your incoming requests from your Profile screen", Toast.LENGTH_SHORT).show();
                });
            } else {
                binding.btnContactSeller.setText("Request Item from Donor");
                binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.success));
                binding.btnContactSeller.setOnClickListener(v -> {
                    RequestItemBottomSheetDialogFragment.newInstance(item.getId(), item.getTitle())
                            .show(getSupportFragmentManager(), RequestItemBottomSheetDialogFragment.TAG);
                });
            }
        } else {
            binding.tvDetailPrice.setText(String.format("₹%.0f", item.getPrice()));
            binding.tvDetailPrice.setTextColor(ContextCompat.getColor(this, R.color.primary));
            binding.tvDetailBadgeType.setText(isClosed ? "SOLD" : String.format("₹%.0f", item.getPrice()));
            binding.tvDetailBadgeType.setBackground(ContextCompat.getDrawable(this,
                    isClosed ? R.drawable.bg_badge_sold : R.drawable.bg_badge_sell));

            if (isClosed) {
                binding.btnContactSeller.setEnabled(false);
                binding.btnContactSeller.setText("Item Sold");
                binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.text_secondary));
            } else if (isOwner) {
                binding.btnContactSeller.setEnabled(false);
                binding.btnContactSeller.setText("Your Listing");
                binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary));
            } else {
                binding.btnContactSeller.setText(String.format("Chat to Buy (₹%.0f)", item.getPrice()));
                binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                binding.btnContactSeller.setOnClickListener(v -> {
                    ChatActivity.startWithListing(this, item.getId(), item.getTitle());
                });
            }
        }

        binding.tvDetailCategory.setText(item.getCategory() != null ? item.getCategory() : "GENERAL");

        if (item.getCondition() != null) {
            binding.tvDetailCondition.setText(item.getCondition().replace("_", " "));
        } else {
            binding.tvDetailCondition.setText("GOOD");
        }

        // Seller Profile Info
        binding.tvDetailSellerName.setText(item.getSellerName() != null && !item.getSellerName().isEmpty()
                ? item.getSellerName() : "Campus Student");

        String hostelLocation = String.format("📍 %s • Room %s",
                item.getHostelBlock() != null ? item.getHostelBlock() : "Campus Hostel",
                item.getRoomNo() != null ? item.getRoomNo() : "N/A");
        binding.tvDetailSellerHostel.setText(hostelLocation);

        // Photo loading with category fallback
        int placeholderRes = CategoryPlaceholderHelper.getPlaceholderForCategory(item.getCategory());
        if (item.getPhotos() != null && !item.getPhotos().isEmpty() && item.getPhotos().get(0) != null) {
            Glide.with(this)
                    .load(item.getPhotos().get(0))
                    .centerCrop()
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .into(binding.ivDetailPhoto);
        } else {
            binding.ivDetailPhoto.setImageResource(placeholderRes);
        }

        // Report Listing Action
        if (!isOwner) {
            binding.btnReportListing.setVisibility(View.VISIBLE);
            binding.btnReportListing.setOnClickListener(v -> {
                ReportBottomSheetDialogFragment dialog = ReportBottomSheetDialogFragment.newInstance(item.getId());
                dialog.show(getSupportFragmentManager(), ReportBottomSheetDialogFragment.TAG);
            });
        } else {
            binding.btnReportListing.setVisibility(View.GONE);
        }
    }
}
