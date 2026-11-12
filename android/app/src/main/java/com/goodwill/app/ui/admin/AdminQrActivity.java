package com.goodwill.app.ui.admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.R;
import com.goodwill.app.data.remote.dto.InviteDto;
import com.goodwill.app.databinding.ActivityAdminQrBinding;
import com.goodwill.app.databinding.DialogGenerateQrBinding;
import com.goodwill.app.databinding.DialogViewQrBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminQrActivity extends AppCompatActivity {

    private ActivityAdminQrBinding binding;
    private AdminViewModel viewModel;
    private AdminInvitesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadInvites();
    }

    private void setupToolbar() {
        binding.toolbarAdmin.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshAdmin.setOnRefreshListener(() -> viewModel.loadInvites());
    }

    private void setupRecyclerView() {
        adapter = new AdminInvitesAdapter(invite -> showQrDialog(invite));
        binding.rvAdminInvites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminInvites.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabGenerateQr.setOnClickListener(v -> showGenerateDialog());
    }

    private void setupObservers() {
        viewModel.getInvites().observe(this, list -> {
            binding.swipeRefreshAdmin.setRefreshing(false);
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyAdmin.setVisibility(View.VISIBLE);
                binding.rvAdminInvites.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyAdmin.setVisibility(View.GONE);
                binding.rvAdminInvites.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressAdmin.setVisibility(loading && !binding.swipeRefreshAdmin.isRefreshing() ? View.VISIBLE : View.GONE);
        });

        viewModel.getNewlyGeneratedInvite().observe(this, newInvite -> {
            if (newInvite != null) {
                showQrDialog(newInvite);
                viewModel.clearNewInvite();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                binding.swipeRefreshAdmin.setRefreshing(false);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });

        viewModel.getSuccessMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessages();
            }
        });
    }

    private void showGenerateDialog() {
        DialogGenerateQrBinding dialogBinding = DialogGenerateQrBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancelGenerate.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnConfirmGenerate.setOnClickListener(v -> {
            String label = dialogBinding.etLabel.getText() != null
                    ? dialogBinding.etLabel.getText().toString().trim()
                    : "Notice Board";
            if (label.isEmpty()) {
                label = "Campus Notice Board";
            }

            long durationSeconds = 7 * 24 * 3600; // Default 7 days
            int checkedId = dialogBinding.chipGroupDuration.getCheckedChipId();
            if (checkedId == R.id.chipDuration24h) {
                durationSeconds = 24 * 3600;
            } else if (checkedId == R.id.chipDuration7d) {
                durationSeconds = 7 * 24 * 3600;
            } else if (checkedId == R.id.chipDuration30d) {
                durationSeconds = 30L * 24 * 3600;
            } else if (checkedId == R.id.chipDuration1y) {
                durationSeconds = 365L * 24 * 3600;
            }

            int maxUses = 100;
            try {
                String usesStr = dialogBinding.etMaxUses.getText() != null
                        ? dialogBinding.etMaxUses.getText().toString().trim()
                        : "100";
                maxUses = Integer.parseInt(usesStr);
            } catch (Exception ignored) {}

            dialog.dismiss();
            viewModel.generateInvite(label, durationSeconds, maxUses);
        });

        dialog.show();
    }

    private void showQrDialog(InviteDto invite) {
        DialogViewQrBinding qrBinding = DialogViewQrBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(qrBinding.getRoot())
                .create();

        qrBinding.tvQrDialogLabel.setText(invite.getLabel());
        qrBinding.tvQrCodeValue.setText(invite.getCode());

        Bitmap qrBitmap = QrCodeGenerator.generateQrBitmap(invite.getCode(), 500, 500);
        if (qrBitmap != null) {
            qrBinding.ivQrBitmap.setImageBitmap(qrBitmap);
        }

        qrBinding.btnCopyCodeDialog.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Campus Invite Code", invite.getCode());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied " + invite.getCode() + " to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });

        qrBinding.btnCloseQrDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

