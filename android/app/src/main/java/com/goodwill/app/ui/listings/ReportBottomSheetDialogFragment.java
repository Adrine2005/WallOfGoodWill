package com.goodwill.app.ui.listings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.ReportDto;
import com.goodwill.app.data.repository.ModerationRepository;
import com.goodwill.app.data.repository.RepositoryCallback;
import com.goodwill.app.databinding.DialogReportListingBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReportBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ReportBottomSheetDialog";
    private static final String ARG_LISTING_ID = "arg_listing_id";

    private DialogReportListingBinding binding;
    private ModerationRepository repository;
    private String listingId;
    private OnReportSubmittedListener listener;

    public interface OnReportSubmittedListener {
        void onReportSubmitted();
    }

    public static ReportBottomSheetDialogFragment newInstance(String listingId) {
        ReportBottomSheetDialogFragment fragment = new ReportBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LISTING_ID, listingId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnReportSubmittedListener(OnReportSubmittedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listingId = getArguments().getString(ARG_LISTING_ID);
        }
        repository = new ModerationRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogReportListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCancelReport.setOnClickListener(v -> dismiss());

        binding.btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        if (listingId == null || listingId.isEmpty()) {
            dismiss();
            return;
        }

        String reason = "PROHIBITED_ITEM";
        int checkedId = binding.rgReportReasons.getCheckedRadioButtonId();
        if (checkedId == R.id.rbInappropriateContent) {
            reason = "INAPPROPRIATE_CONTENT";
        } else if (checkedId == R.id.rbSpamOrScam) {
            reason = "SPAM_OR_SCAM";
        } else if (checkedId == R.id.rbOther) {
            reason = "OTHER";
        }

        String comment = "";
        if (binding.etReportComment.getText() != null) {
            comment = binding.etReportComment.getText().toString().trim();
        }

        binding.btnSubmitReport.setEnabled(false);
        binding.btnSubmitReport.setText("Submitting...");

        final String finalReason = reason;
        final String finalComment = comment;

        repository.reportListing(listingId, finalReason, finalComment, new RepositoryCallback<ReportDto>() {
            @Override
            public void onSuccess(ReportDto result) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Report submitted. Thank you for keeping our campus safe!", Toast.LENGTH_LONG).show();
                    if (listener != null) {
                        listener.onReportSubmitted();
                    }
                    dismiss();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    binding.btnSubmitReport.setEnabled(true);
                    binding.btnSubmitReport.setText("Submit Report");
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
