package com.goodwill.app.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.databinding.ActivityConversationsBinding;

public class ConversationsActivity extends AppCompatActivity {

    private ActivityConversationsBinding binding;
    private ChatViewModel viewModel;
    private ConversationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadConversations();
    }

    private void setupToolbar() {
        binding.toolbarConversations.setNavigationOnClickListener(v -> finish());
        binding.swipeRefreshConversations.setOnRefreshListener(() -> viewModel.loadConversations());
    }

    private void setupRecyclerView() {
        String currentUserId = viewModel.getCurrentUserId();
        adapter = new ConversationsAdapter(currentUserId, conversation -> {
            Intent intent = new Intent(ConversationsActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getId());
            intent.putExtra(ChatActivity.EXTRA_LISTING_TITLE, conversation.getListingTitle());
            intent.putExtra(ChatActivity.EXTRA_PARTY_NAME, conversation.getOtherPartyName(currentUserId));
            startActivity(intent);
        });

        binding.rvConversations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConversations.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getConversations().observe(this, conversations -> {
            binding.swipeRefreshConversations.setRefreshing(false);
            if (conversations == null || conversations.isEmpty()) {
                binding.layoutEmptyConversations.setVisibility(View.VISIBLE);
                binding.rvConversations.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyConversations.setVisibility(View.GONE);
                binding.rvConversations.setVisibility(View.VISIBLE);
                adapter.submitList(conversations);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressConversations.setVisibility(loading && !binding.swipeRefreshConversations.isRefreshing() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                binding.swipeRefreshConversations.setRefreshing(false);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }
}
