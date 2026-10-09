package com.goodwill.app.ui.points;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.LeaderboardEntryDto;
import com.goodwill.app.databinding.ItemLeaderboardBinding;

import java.util.Objects;

public class LeaderboardAdapter extends ListAdapter<LeaderboardEntryDto, LeaderboardAdapter.LeaderboardViewHolder> {

    public LeaderboardAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<LeaderboardEntryDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<LeaderboardEntryDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull LeaderboardEntryDto oldItem, @NonNull LeaderboardEntryDto newItem) {
                    return Objects.equals(oldItem.getUserId(), newItem.getUserId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull LeaderboardEntryDto oldItem, @NonNull LeaderboardEntryDto newItem) {
                    return oldItem.getRank() == newItem.getRank() &&
                            oldItem.getPoints() == newItem.getPoints() &&
                            oldItem.getDonationCount() == newItem.getDonationCount() &&
                            Objects.equals(oldItem.getUserName(), newItem.getUserName()) &&
                            Objects.equals(oldItem.getTierName(), newItem.getTierName());
                }
            };

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new LeaderboardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaderboardBinding binding;

        LeaderboardViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LeaderboardEntryDto entry) {
            int rank = entry.getRank();
            String rankText = "#" + rank;
            if (rank == 1) {
                rankText = "🥇";
                binding.tvRank.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.warning));
            } else if (rank == 2) {
                rankText = "🥈";
                binding.tvRank.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            } else if (rank == 3) {
                rankText = "🥉";
                binding.tvRank.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.warning));
            } else {
                binding.tvRank.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
            }
            binding.tvRank.setText(rankText);

            binding.tvUserName.setText(entry.getUserName() != null ? entry.getUserName() : "Student");
            binding.tvHostelBlock.setText(entry.getHostelBlock() != null ? "📍 " + entry.getHostelBlock() : "📍 Campus");
            binding.tvTierName.setText(entry.getTierName() != null ? entry.getTierName() : "Member");
            binding.tvBadgeIcon.setText(entry.getBadgeIcon() != null && !entry.getBadgeIcon().isEmpty() ? entry.getBadgeIcon() : "🌱");

            binding.tvPoints.setText(entry.getPoints() + " pts");
            int donations = entry.getDonationCount();
            binding.tvDonations.setText(donations + (donations == 1 ? " donation" : " donations"));
        }
    }
}
