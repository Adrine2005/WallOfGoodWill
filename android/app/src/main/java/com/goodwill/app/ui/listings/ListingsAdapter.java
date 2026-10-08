package com.goodwill.app.ui.listings;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.databinding.ItemListingBinding;

public class ListingsAdapter extends ListAdapter<ListingDto, ListingsAdapter.ViewHolder> {

    public interface OnListingClickListener {
        void onListingClick(ListingDto listing);
    }

    private final OnListingClickListener listener;

    public ListingsAdapter(OnListingClickListener listener) {
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
        ItemListingBinding binding = ItemListingBinding.inflate(
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
        private final ItemListingBinding binding;

        public ViewHolder(@NonNull ItemListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListingDto item, OnListingClickListener listener) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvDescription.setText(item.getDescription());

            if (item.getHostelBlock() != null && !item.getHostelBlock().isEmpty()) {
                binding.tvHostelBlock.setText(String.format("📍 %s", item.getHostelBlock()));
            } else {
                binding.tvHostelBlock.setText("📍 Campus Resident");
            }

            binding.tvCategory.setText(item.getCategory() != null ? item.getCategory() : "GENERAL");

            // Format Condition
            if (item.getCondition() != null) {
                binding.tvCondition.setText(item.getCondition().replace("_", " "));
            } else {
                binding.tvCondition.setText("GOOD");
            }

            // Format Badge (Donate vs Sell)
            if (item.isDonation()) {
                binding.tvBadgeType.setText("DONATE • Free");
                binding.tvBadgeType.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_donate));
            } else {
                binding.tvBadgeType.setText(String.format("₹%.0f", item.getPrice()));
                binding.tvBadgeType.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_sell));
            }

            // Load Image with Glide
            if (item.getPhotos() != null && !item.getPhotos().isEmpty() && item.getPhotos().get(0) != null) {
                Glide.with(itemView.getContext())
                        .load(item.getPhotos().get(0))
                        .centerCrop()
                        .placeholder(R.color.surface_variant)
                        .error(R.color.surface_variant)
                        .into(binding.ivListingImage);
            } else {
                binding.ivListingImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onListingClick(item);
                }
            });
        }
    }
}
