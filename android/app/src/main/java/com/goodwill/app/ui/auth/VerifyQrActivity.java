package com.goodwill.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.MainActivity;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.databinding.ActivityVerifyQrBinding;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.mlkit.vision.barcode.common.Barcode;

public class VerifyQrActivity extends AppCompatActivity {

    private ActivityVerifyQrBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        displayUserProfile();
        setupObservers();
        setupListeners();
    }

    private void displayUserProfile() {
        UserDto user = authViewModel.getCurrentUser();
        if (user != null) {
            binding.textStudentName.setText(user.getName() != null && !user.getName().isEmpty()
                    ? user.getName() : "Student Account");
            String hostelDetails = String.format("%s • Room %s",
                    user.getHostelBlock() != null ? user.getHostelBlock() : "Campus Hostel",
                    user.getRoomNo() != null ? user.getRoomNo() : "N/A");
            binding.textHostelDetails.setText(hostelDetails);
        }
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnScanQr.setEnabled(!isLoading);
            binding.btnVerifyManual.setEnabled(!isLoading);
            binding.verifyProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.cardError.setVisibility(View.VISIBLE);
                binding.textError.setText(error);
                binding.cardSuccess.setVisibility(View.GONE);
            } else {
                binding.cardError.setVisibility(View.GONE);
            }
        });

        authViewModel.getQrVerifySuccess().observe(this, response -> {
            if (response != null && response.isVerified()) {
                binding.cardError.setVisibility(View.GONE);
                binding.cardSuccess.setVisibility(View.VISIBLE);

                // Show success briefly, then proceed to main app feed
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(VerifyQrActivity.this, MainActivity.class));
                    finish();
                }, 1200);
            }
        });
    }

    private void setupListeners() {
        binding.btnScanQr.setOnClickListener(v -> startGoogleCodeScanner());

        binding.btnVerifyManual.setOnClickListener(v -> {
            String code = binding.etManualCode.getText() != null
                    ? binding.etManualCode.getText().toString().trim()
                    : "";
            if (TextUtils.isEmpty(code)) {
                binding.tilManualCode.setError("Please enter the invite code");
                return;
            }
            binding.tilManualCode.setError(null);
            authViewModel.verifyQr(code);
        });

        binding.btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
            startActivity(new Intent(VerifyQrActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void startGoogleCodeScanner() {
        try {
            GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .enableAutoZoom()
                    .build();

            GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);
            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !rawValue.trim().isEmpty()) {
                            binding.etManualCode.setText(rawValue.trim());
                            authViewModel.verifyQr(rawValue.trim());
                        }
                    })
                    .addOnFailureListener(e -> {
                        // In case of emulator or missing Google Play Services, prompt manual code entry
                        Toast.makeText(this, "Camera scanner unavailable: please enter the code manually", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Camera scanner unavailable: please enter the code manually", Toast.LENGTH_SHORT).show();
        }
    }
}
