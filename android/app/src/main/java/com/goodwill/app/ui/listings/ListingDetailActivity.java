package com.goodwill.app.ui.listings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.databinding.ActivityListingDetailBinding;

public class ListingDetailActivity extends AppCompatActivity {

    private ActivityListingDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        // Price & Badges
        if (item.isDonation()) {
            binding.tvDetailPrice.setText("Free Donation");
            binding.tvDetailPrice.setTextColor(ContextCompat.getColor(this, R.color.success));
            binding.tvDetailBadgeType.setText("DONATE • Free");
            binding.tvDetailBadgeType.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_donate));
            binding.btnContactSeller.setText("Request Item from Donor");
            binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.success));
        } else {
            binding.tvDetailPrice.setText(String.format("₹%.0f", item.getPrice()));
            binding.tvDetailPrice.setTextColor(ContextCompat.getColor(this, R.color.primary));
            binding.tvDetailBadgeType.setText(String.format("₹%.0f", item.getPrice()));
            binding.tvDetailBadgeType.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_sell));
            binding.btnContactSeller.setText(String.format("Contact Seller to Buy (₹%.0f)", item.getPrice()));
            binding.btnContactSeller.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
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

        // Photo loading
        if (item.getPhotos() != null && !item.getPhotos().isEmpty() && item.getPhotos().get(0) != null) {
            Glide.with(this)
                    .load(item.getPhotos().get(0))
                    .centerCrop()
                    .placeholder(R.color.surface_variant)
                    .error(R.color.surface_variant)
                    .into(binding.ivDetailPhoto);
        } else {
            binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        binding.btnContactSeller.setOnClickListener(v -> {
            String action = item.isDonation() ? "Donation request sent!" : "Inquiry sent to seller!";
            Toast.makeText(this, action + " Visit " + item.getHostelBlock() + " Room " + item.getRoomNo(), Toast.LENGTH_LONG).show();
        });
    }
}
