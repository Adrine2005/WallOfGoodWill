package com.goodwill.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.MainActivity;
import com.goodwill.app.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private AuthViewModel authViewModel;
    private boolean sessionChecked = false;
    private boolean minTimeElapsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Ensure splash displays for at least 800ms for branded experience
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            minTimeElapsed = true;
            tryNavigate();
        }, 800);

        authViewModel.getSessionActive().observe(this, active -> {
            if (active != null) {
                sessionChecked = true;
                tryNavigate();
            }
        });

        authViewModel.checkSession();
    }

    private void tryNavigate() {
        if (!minTimeElapsed || !sessionChecked) {
            return;
        }

        Boolean active = authViewModel.getSessionActive().getValue();
        if (Boolean.TRUE.equals(active)) {
            if (authViewModel.isCurrentlyVerified()) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, VerifyQrActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
