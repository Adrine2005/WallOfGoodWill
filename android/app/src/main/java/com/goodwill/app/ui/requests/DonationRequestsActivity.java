package com.goodwill.app.ui.requests;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.data.remote.dto.DonationRequestDto;
import com.goodwill.app.databinding.ActivityDonationRequestsBinding;

public class DonationRequestsActivity extends AppCompatActivity implements DonationRequestsAdapter.OnRequestListener {

    private ActivityDonationRequestsBinding binding;
    private RequestsViewModel viewModel;
    private DonationRequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonationRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RequestsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();

        binding.swipeRefreshRequests.setOnRefreshListener(() -> viewModel.loadIncomingRequests());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadIncomingRequests();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new DonationRequestsAdapter(this);
        binding.rvDonationRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDonationRequests.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIncomingRequests().observe(this, requests -> {
            adapter.submitList(requests);
            boolean isEmpty = requests == null || requests.isEmpty();
            binding.layoutEmptyRequests.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvDonationRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.swipeRefreshRequests.setRefreshing(loading);
            if (!loading) {
                binding.progressRequests.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.getActionSuccessMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                viewModel.clearActionSuccess();
            }
        });
    }

    @Override
    public void onAccept(DonationRequestDto request) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Request")
                .setMessage("Are you sure you want to accept " + request.getRequesterName() +
                        "? This will mark your item as DONATED and decline other requests.")
                .setPositiveButton("Accept", (dialog, which) -> {
                    viewModel.acceptRequest(request.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDecline(DonationRequestDto request) {
        new AlertDialog.Builder(this)
                .setTitle("Decline Request")
                .setMessage("Decline donation request from " + request.getRequesterName() + "?")
                .setPositiveButton("Decline", (dialog, which) -> {
                    viewModel.declineRequest(request.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
