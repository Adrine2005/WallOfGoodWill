package com.goodwill.app.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.NotificationDto;
import com.goodwill.app.databinding.ItemNotificationBinding;

import java.util.Objects;

public class NotificationsAdapter extends ListAdapter<NotificationDto, NotificationsAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationDto notification);
    }

    private final OnNotificationClickListener listener;

    public NotificationsAdapter(OnNotificationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<NotificationDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NotificationDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationDto oldItem, @NonNull NotificationDto newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationDto oldItem, @NonNull NotificationDto newItem) {
                    return oldItem.isRead() == newItem.isRead() &&
                            Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                            Objects.equals(oldItem.getMessage(), newItem.getMessage());
                }
            };

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final ItemNotificationBinding binding;

        public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NotificationDto item) {
            Context context = itemView.getContext();

            binding.tvNotificationTitle.setText(item.getTitle() != null ? item.getTitle() : "Notification");
            binding.tvNotificationMessage.setText(item.getMessage() != null ? item.getMessage() : "");

            if (item.getCreatedAt() != null && item.getCreatedAt().length() >= 10) {
                binding.tvNotificationTime.setText(item.getCreatedAt().substring(0, 10));
            } else {
                binding.tvNotificationTime.setText("Recent");
            }

            if (item.isRequestAccepted()) {
                binding.tvNotificationIcon.setText("🎉");
            } else if (item.isRequestReceived()) {
                binding.tvNotificationIcon.setText("📥");
            } else if (item.isRequestDeclined()) {
                binding.tvNotificationIcon.setText("ℹ️");
            } else {
                binding.tvNotificationIcon.setText("🔔");
            }

            if (!item.isRead()) {
                binding.viewUnreadDot.setVisibility(View.VISIBLE);
                binding.cardNotification.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_container));
            } else {
                binding.viewUnreadDot.setVisibility(View.GONE);
                binding.cardNotification.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });
        }
    }
}
