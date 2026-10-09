package com.goodwill.app.ui.wishlist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.WishlistItemDto;
import com.goodwill.app.databinding.ItemWishlistBinding;

import java.util.Objects;

public class WishlistAdapter extends ListAdapter<WishlistItemDto, WishlistAdapter.WishlistViewHolder> {

    public interface OnWishlistItemClickListener {
        void onHelpClick(WishlistItemDto item);
        void onDeleteClick(WishlistItemDto item);
    }

    private final String currentUserId;
    private final OnWishlistItemClickListener listener;

    public WishlistAdapter(String currentUserId, OnWishlistItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<WishlistItemDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WishlistItemDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull WishlistItemDto oldItem, @NonNull WishlistItemDto newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull WishlistItemDto oldItem, @NonNull WishlistItemDto newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                            Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                            oldItem.isDonationPreferred() == newItem.isDonationPreferred() &&
                            oldItem.getMaxBudget() == newItem.getMaxBudget();
                }
            };

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishlistBinding binding = ItemWishlistBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new WishlistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ItemWishlistBinding binding;

        WishlistViewHolder(ItemWishlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WishlistItemDto item) {
            binding.tvWishlistTitle.setText(item.getTitle());
            binding.tvWishlistCategory.setText(item.getCategory() != null ? item.getCategory() : "GENERAL");

            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                binding.tvWishlistDescription.setText(item.getDescription());
                binding.tvWishlistDescription.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvWishlistDescription.setVisibility(android.view.View.GONE);
            }

            // Preference Badge
            if (item.isDonationPreferred()) {
                binding.tvWishlistBadge.setText("Free Donation Preferred");
                binding.tvWishlistBadge.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_donate));
            } else if (item.getMaxBudget() > 0) {
                binding.tvWishlistBadge.setText(String.format("Budget: Up to ₹%.0f", item.getMaxBudget()));
                binding.tvWishlistBadge.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_sell));
            } else {
                binding.tvWishlistBadge.setText("Open to Buy / Donate");
                binding.tvWishlistBadge.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_donate));
            }

            // Requester details
            binding.tvRequesterName.setText(item.getRequesterName() != null ? item.getRequesterName() : "Student");
            String location = "";
            if (item.getRequesterHostel() != null && !item.getRequesterHostel().isEmpty()) {
                location = "📍 " + item.getRequesterHostel();
                if (item.getRequesterRoom() != null && !item.getRequesterRoom().isEmpty()) {
                    location += " • Room " + item.getRequesterRoom();
                }
            }
            binding.tvRequesterLocation.setText(location.isEmpty() ? "📍 Campus Student" : location);

            // Action button
            boolean isOwn = currentUserId != null && currentUserId.equals(item.getRequesterId());
            if (isOwn) {
                binding.btnWishlistAction.setText("Delete Request");
                binding.btnWishlistAction.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.error));
                binding.btnWishlistAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(item);
                    }
                });
            } else {
                binding.btnWishlistAction.setText("💬 I Have This");
                binding.btnWishlistAction.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                binding.btnWishlistAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onHelpClick(item);
                    }
                });
            }
        }
    }
}
