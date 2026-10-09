package com.goodwill.app.ui.requests;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodwill.app.data.remote.dto.DonationRequestDto;
import com.goodwill.app.data.repository.RepositoryCallback;
import com.goodwill.app.data.repository.RequestsRepository;
import com.goodwill.app.databinding.BottomSheetRequestItemBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RequestItemBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "RequestItemBottomSheet";
    private static final String ARG_LISTING_ID = "arg_listing_id";
    private static final String ARG_LISTING_TITLE = "arg_listing_title";

    private BottomSheetRequestItemBinding binding;
    private RequestsRepository repository;
    private String listingId;
    private String listingTitle;

    public static RequestItemBottomSheetDialogFragment newInstance(String listingId, String listingTitle) {
        RequestItemBottomSheetDialogFragment fragment = new RequestItemBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LISTING_ID, listingId);
        args.putString(ARG_LISTING_TITLE, listingTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listingId = getArguments().getString(ARG_LISTING_ID);
            listingTitle = getArguments().getString(ARG_LISTING_TITLE);
        }
        repository = new RequestsRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetRequestItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!TextUtils.isEmpty(listingTitle)) {
            binding.tvSheetListingTitle.setText(listingTitle);
        }

        binding.btnCancelRequest.setOnClickListener(v -> dismiss());

        binding.btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String message = binding.etRequestMessage.getText() != null
                ? binding.etRequestMessage.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(message)) {
            binding.tilRequestMessage.setError("Please enter a message explaining your need");
            return;
        }

        binding.tilRequestMessage.setError(null);
        setLoading(true);

        repository.createDonationRequest(listingId, message, new RepositoryCallback<DonationRequestDto>() {
            @Override
            public void onSuccess(DonationRequestDto result) {
                if (!isAdded()) return;
                setLoading(false);
                Toast.makeText(requireContext(), "Donation request sent! The donor will review your note.", Toast.LENGTH_LONG).show();
                dismiss();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                setLoading(false);
                binding.tilRequestMessage.setError(error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressRequest.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitRequest.setEnabled(!loading);
        binding.btnCancelRequest.setEnabled(!loading);
        binding.etRequestMessage.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
