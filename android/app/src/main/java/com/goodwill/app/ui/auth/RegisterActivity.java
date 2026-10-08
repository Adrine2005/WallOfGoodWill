package com.goodwill.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.MainActivity;
import com.goodwill.app.R;
import com.goodwill.app.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupHostelDropdown();
        setupObservers();
        setupListeners();
    }

    private void setupHostelDropdown() {
        String[] blocks = getResources().getStringArray(R.array.hostel_blocks);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                blocks
        );
        binding.actvHostelBlock.setAdapter(adapter);
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnRegister.setEnabled(!isLoading);
            binding.registerProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.cardError.setVisibility(View.VISIBLE);
                binding.textError.setText(error);
            } else {
                binding.cardError.setVisibility(View.GONE);
            }
        });

        authViewModel.getRegisterSuccess().observe(this, response -> {
            if (response != null) {
                if (response.isVerified()) {
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(RegisterActivity.this, VerifyQrActivity.class));
                }
                finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        binding.btnGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilHostelBlock.setError(null);
        binding.tilRoomNo.setError(null);
        binding.cardError.setVisibility(View.GONE);

        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String hostelBlock = binding.actvHostelBlock.getText() != null ? binding.actvHostelBlock.getText().toString().trim() : "";
        String roomNo = binding.etRoomNo.getText() != null ? binding.etRoomNo.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String inviteCode = binding.etInviteCode.getText() != null ? binding.etInviteCode.getText().toString().trim() : "";

        boolean cancel = false;

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Name is required");
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            cancel = true;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            cancel = true;
        }

        if (TextUtils.isEmpty(hostelBlock)) {
            binding.tilHostelBlock.setError("Please select your hostel block");
            cancel = true;
        }

        if (TextUtils.isEmpty(roomNo)) {
            binding.tilRoomNo.setError("Room number is required");
            cancel = true;
        }

        if (!cancel) {
            authViewModel.register(name, email, password, hostelBlock, roomNo, phone, inviteCode);
        }
    }
}
