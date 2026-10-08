package com.goodwill.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.MainActivity;
import com.goodwill.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnLogin.setEnabled(!isLoading);
            binding.loginProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.cardError.setVisibility(View.VISIBLE);
                binding.textError.setText(error);
            } else {
                binding.cardError.setVisibility(View.GONE);
            }
        });

        authViewModel.getLoginSuccess().observe(this, user -> {
            if (user != null) {
                if (user.isVerified()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, VerifyQrActivity.class));
                }
                finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.cardError.setVisibility(View.GONE);

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        boolean cancel = false;

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password cannot be empty");
            cancel = true;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email cannot be empty");
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            cancel = true;
        }

        if (!cancel) {
            authViewModel.login(email, password);
        }
    }
}
