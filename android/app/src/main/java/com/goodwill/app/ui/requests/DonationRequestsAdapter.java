package com.goodwill.app.ui.requests;

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
import com.goodwill.app.data.remote.dto.DonationRequestDto;
import com.goodwill.app.databinding.ItemDonationRequestBinding;

import java.util.Objects;

public class DonationRequestsAdapter extends ListAdapter<DonationRequestDto, DonationRequestsAdapter.RequestViewHolder> {

    public interface OnRequestListener {
        void onAccept(DonationRequestDto request);
        void onDecline(DonationRequestDto request);
    }

    private final OnRequestListener listener;

    public DonationRequestsAdapter(OnRequestListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DonationRequestDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DonationRequestDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull DonationRequestDto oldItem, @NonNull DonationRequestDto newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DonationRequestDto oldItem, @NonNull DonationRequestDto newItem) {
                    return Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                            Objects.equals(oldItem.getMessage(), newItem.getMessage()) &&
                            Objects.equals(oldItem.getRequesterName(), newItem.getRequesterName());
                }
            };

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDonationRequestBinding binding = ItemDonationRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        private final ItemDonationRequestBinding binding;

        public RequestViewHolder(@NonNull ItemDonationRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DonationRequestDto item) {
            Context context = itemView.getContext();

            binding.tvRequestListingTitle.setText(item.getListingTitle() != null ? item.getListingTitle() : "Donation Item");
            binding.tvRequesterName.setText(item.getRequesterName() != null ? item.getRequesterName() : "Campus Student");

            String location = String.format("• %s Room %s",
                    item.getRequesterHostel() != null ? item.getRequesterHostel() : "Campus Hostel",
                    item.getRequesterRoom() != null ? item.getRequesterRoom() : "N/A");
            binding.tvRequesterLocation.setText(location);

            binding.tvRequestMessage.setText(item.getMessage() != null ? item.getMessage() : "");

            if (item.isPending()) {
                binding.tvRequestStatusBadge.setText("PENDING");
                binding.tvRequestStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning));
                binding.layoutActions.setVisibility(View.VISIBLE);
                binding.tvResolutionStatus.setVisibility(View.GONE);

                binding.btnAcceptRequest.setOnClickListener(v -> {
                    if (listener != null) listener.onAccept(item);
                });
                binding.btnDeclineRequest.setOnClickListener(v -> {
                    if (listener != null) listener.onDecline(item);
                });
            } else if (item.isAccepted()) {
                binding.tvRequestStatusBadge.setText("ACCEPTED");
                binding.tvRequestStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.success));
                binding.layoutActions.setVisibility(View.GONE);
                binding.tvResolutionStatus.setVisibility(View.VISIBLE);
                binding.tvResolutionStatus.setText("✓ Accepted Recipient • Listing Marked Donated");
                binding.tvResolutionStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
            } else {
                binding.tvRequestStatusBadge.setText("DECLINED");
                binding.tvRequestStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                binding.layoutActions.setVisibility(View.GONE);
                binding.tvResolutionStatus.setVisibility(View.VISIBLE);
                binding.tvResolutionStatus.setText("Declined");
                binding.tvResolutionStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
        }
    }
}
