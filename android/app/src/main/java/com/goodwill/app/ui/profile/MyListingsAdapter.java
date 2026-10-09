package com.goodwill.app.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.databinding.ItemMyListingBinding;
import com.goodwill.app.ui.listings.CategoryPlaceholderHelper;

public class MyListingsAdapter extends ListAdapter<ListingDto, MyListingsAdapter.ViewHolder> {

    public interface OnMyListingActionListener {
        void onListingClick(ListingDto listing);
        void onEdit(ListingDto listing);
        void onMarkSold(ListingDto listing);
        void onMarkDonated(ListingDto listing);
        void onDelete(ListingDto listing);
    }

    private final OnMyListingActionListener listener;

    public MyListingsAdapter(OnMyListingActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ListingDto> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListingDto>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListingDto oldItem, @NonNull ListingDto newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListingDto oldItem, @NonNull ListingDto newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPrice() == newItem.getPrice()
                    && oldItem.getStatus().equals(newItem.getStatus());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingBinding binding = ItemMyListingBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;

        public ViewHolder(@NonNull ItemMyListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListingDto item, OnMyListingActionListener listener) {
            binding.tvTitle.setText(item.getTitle());

            String category = item.getCategory() != null ? item.getCategory() : "GENERAL";
            String condition = item.getCondition() != null ? item.getCondition().replace("_", " ") : "GOOD";
            binding.tvCategoryAndDate.setText(String.format("%s • %s", category, condition));

            // Price / Type badge
            if (item.isDonation()) {
                binding.tvBadgeType.setText("DONATE");
                binding.tvBadgeType.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_donate));
            } else {
                binding.tvBadgeType.setText(String.format("₹%.0f", item.getPrice()));
                binding.tvBadgeType.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_sell));
            }

            // Status badge and action button controls
            String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "ACTIVE";
            binding.tvStatusBadge.setText(status);

            if ("ACTIVE".equalsIgnoreCase(status)) {
                binding.tvStatusBadge.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_donate));
                binding.btnEdit.setVisibility(View.VISIBLE);
                binding.btnMarkSold.setVisibility(item.isDonation() ? View.GONE : View.VISIBLE);
                binding.btnMarkDonated.setVisibility(View.VISIBLE);
            } else {
                binding.tvStatusBadge.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_sold));
                binding.btnEdit.setVisibility(View.GONE);
                binding.btnMarkSold.setVisibility(View.GONE);
                binding.btnMarkDonated.setVisibility(View.GONE);
            }

            // Load photo with category fallback
            int placeholderRes = CategoryPlaceholderHelper.getPlaceholderForCategory(item.getCategory());
            if (item.getPhotos() != null && !item.getPhotos().isEmpty() && item.getPhotos().get(0) != null) {
                Glide.with(itemView.getContext())
                        .load(item.getPhotos().get(0))
                        .centerCrop()
                        .placeholder(placeholderRes)
                        .error(placeholderRes)
                        .into(binding.ivListingImage);
            } else {
                binding.ivListingImage.setImageResource(placeholderRes);
            }

            // Button listeners
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(item);
                }
            });

            binding.btnMarkSold.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkSold(item);
                }
            });

            binding.btnMarkDonated.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkDonated(item);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(item);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListingClick(item);
                }
            });
        }
    }
}
