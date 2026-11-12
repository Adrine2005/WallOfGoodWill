package com.goodwill.app.ui.admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.InviteDto;
import com.goodwill.app.databinding.ItemAdminInviteBinding;

public class AdminInvitesAdapter extends ListAdapter<InviteDto, AdminInvitesAdapter.ViewHolder> {

    public interface OnInviteActionListener {
        void onShowQr(InviteDto invite);
    }

    private final OnInviteActionListener listener;

    public AdminInvitesAdapter(OnInviteActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<InviteDto> DIFF_CALLBACK = new DiffUtil.ItemCallback<InviteDto>() {
        @Override
        public boolean areItemsTheSame(@NonNull InviteDto oldItem, @NonNull InviteDto newItem) {
            return oldItem.getCode().equals(newItem.getCode());
        }

        @Override
        public boolean areContentsTheSame(@NonNull InviteDto oldItem, @NonNull InviteDto newItem) {
            return oldItem.getCode().equals(newItem.getCode())
                    && oldItem.getUsedCount() == newItem.getUsedCount()
                    && oldItem.isActive() == newItem.isActive()
                    && oldItem.getLabel().equals(newItem.getLabel());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminInviteBinding binding = ItemAdminInviteBinding.inflate(
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
        private final ItemAdminInviteBinding binding;

        public ViewHolder(@NonNull ItemAdminInviteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(InviteDto invite, OnInviteActionListener listener) {
            binding.tvInviteLabel.setText(invite.getLabel());
            binding.tvInviteCode.setText(invite.getCode());

            String maxStr = invite.getMaxUses() > 0 ? String.valueOf(invite.getMaxUses()) : "unlimited";
            binding.tvUsesCount.setText(String.format("👥 %d / %s uses", invite.getUsedCount(), maxStr));

            if (invite.getExpiresAt() != null && invite.getExpiresAt().length() >= 10) {
                binding.tvExpiresAt.setText("⏳ Expires: " + invite.getExpiresAt().substring(0, 10));
            } else {
                binding.tvExpiresAt.setText("⏳ Active");
            }

            if (invite.isActive()) {
                binding.tvInviteStatus.setText("ACTIVE");
                binding.tvInviteStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_donate));
            } else {
                binding.tvInviteStatus.setText("EXPIRED");
                binding.tvInviteStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_badge_sell));
            }

            binding.btnCopyCode.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Campus Invite Code", invite.getCode());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(itemView.getContext(), "Copied " + invite.getCode() + " to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });

            binding.btnShowQr.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShowQr(invite);
                }
            });
        }
    }
}
