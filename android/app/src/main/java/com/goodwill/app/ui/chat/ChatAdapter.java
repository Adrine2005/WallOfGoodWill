package com.goodwill.app.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.goodwill.app.data.remote.dto.ChatMessageDto;
import com.goodwill.app.databinding.ItemChatMessageBinding;

import java.util.Objects;

public class ChatAdapter extends ListAdapter<ChatMessageDto, ChatAdapter.MessageViewHolder> {

    private final String currentUserId;

    public ChatAdapter(String currentUserId) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
    }

    private static final DiffUtil.ItemCallback<ChatMessageDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessageDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessageDto oldItem, @NonNull ChatMessageDto newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessageDto oldItem, @NonNull ChatMessageDto newItem) {
                    return Objects.equals(oldItem.getText(), newItem.getText()) &&
                            Objects.equals(oldItem.getCreatedAt(), newItem.getCreatedAt());
                }
            };

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding binding;

        MessageViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessageDto message) {
            boolean isOutgoing = currentUserId != null && currentUserId.equals(message.getSenderId());

            String formattedTime = "";
            if (message.getCreatedAt() != null && message.getCreatedAt().length() >= 16) {
                try {
                    formattedTime = message.getCreatedAt().substring(11, 16);
                } catch (Exception ignored) {
                }
            }

            if (isOutgoing) {
                binding.layoutOutgoing.setVisibility(View.VISIBLE);
                binding.layoutIncoming.setVisibility(View.GONE);
                binding.tvOutgoingText.setText(message.getText());
                binding.tvOutgoingTime.setText(formattedTime);
            } else {
                binding.layoutOutgoing.setVisibility(View.GONE);
                binding.layoutIncoming.setVisibility(View.VISIBLE);
                binding.tvIncomingText.setText(message.getText());
                binding.tvIncomingSender.setText(message.getSenderName() != null ? message.getSenderName() : "User");
                binding.tvIncomingTime.setText(formattedTime);
            }
        }
    }
}
