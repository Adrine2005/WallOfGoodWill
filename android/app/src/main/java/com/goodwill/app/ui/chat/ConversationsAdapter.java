package com.goodwill.app.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.goodwill.app.data.remote.dto.ChatConversationDto;
import com.goodwill.app.databinding.ItemConversationBinding;

import java.util.Objects;

public class ConversationsAdapter extends ListAdapter<ChatConversationDto, ConversationsAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(ChatConversationDto conversation);
    }

    private final String currentUserId;
    private final OnConversationClickListener listener;

    public ConversationsAdapter(String currentUserId, OnConversationClickListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ChatConversationDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatConversationDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatConversationDto oldItem, @NonNull ChatConversationDto newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatConversationDto oldItem, @NonNull ChatConversationDto newItem) {
                    return Objects.equals(oldItem.getLastMessageText(), newItem.getLastMessageText()) &&
                            Objects.equals(oldItem.getUpdatedAt(), newItem.getUpdatedAt());
                }
            };

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;

        ConversationViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatConversationDto item) {
            String partyName = item.getOtherPartyName(currentUserId);
            binding.tvPartyName.setText(partyName);
            binding.tvListingTitle.setText("Item: " + item.getListingTitle());

            if (item.getLastMessageText() != null && !item.getLastMessageText().isEmpty()) {
                binding.tvLastMessagePreview.setText(item.getLastMessageText());
            } else {
                binding.tvLastMessagePreview.setText("No messages yet. Tap to chat!");
            }

            if (item.getUpdatedAt() != null && item.getUpdatedAt().length() >= 16) {
                try {
                    String time = item.getUpdatedAt().substring(11, 16);
                    binding.tvLastMessageTime.setText(time);
                } catch (Exception e) {
                    binding.tvLastMessageTime.setText("");
                }
            } else {
                binding.tvLastMessageTime.setText("");
            }

            binding.cardConversation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(item);
                }
            });
        }
    }
}
