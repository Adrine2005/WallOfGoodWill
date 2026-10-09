package com.goodwill.app.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_LISTING_ID = "extra_listing_id";
    public static final String EXTRA_LISTING_TITLE = "extra_listing_title";
    public static final String EXTRA_PARTY_NAME = "extra_party_name";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;

    private String conversationId;
    private String listingId;
    private String listingTitle;
    private String partyName;

    public static void startWithListing(Context context, String listingId, String listingTitle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_LISTING_ID, listingId);
        intent.putExtra(EXTRA_LISTING_TITLE, listingTitle);
        context.startActivity(intent);
    }

    public static void startWithConversation(Context context, String conversationId, String listingTitle, String partyName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EXTRA_LISTING_TITLE, listingTitle);
        intent.putExtra(EXTRA_PARTY_NAME, partyName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        listingId = getIntent().getStringExtra(EXTRA_LISTING_ID);
        listingTitle = getIntent().getStringExtra(EXTRA_LISTING_TITLE);
        partyName = getIntent().getStringExtra(EXTRA_PARTY_NAME);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();

        if (conversationId != null && !conversationId.isEmpty()) {
            viewModel.loadConversationDetail(conversationId);
            viewModel.loadMessages(conversationId);
        } else if (listingId != null && !listingId.isEmpty()) {
            viewModel.openOrCreateConversation(listingId);
        } else {
            Toast.makeText(this, "Unable to load chat", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        binding.toolbarChat.setNavigationOnClickListener(v -> finish());
        updateToolbarTitles();
    }

    private void updateToolbarTitles() {
        if (partyName != null && !partyName.isEmpty()) {
            binding.toolbarChat.setTitle(partyName);
        } else {
            binding.toolbarChat.setTitle("Chat");
        }

        if (listingTitle != null && !listingTitle.isEmpty()) {
            binding.toolbarChat.setSubtitle("Item: " + listingTitle);
        }
    }

    private void setupRecyclerView() {
        String currentUserId = viewModel.getCurrentUserId();
        adapter = new ChatAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessageInput.getText() != null ? binding.etMessageInput.getText().toString().trim() : "";
            if (text.isEmpty()) {
                return;
            }

            if (conversationId == null || conversationId.isEmpty()) {
                Toast.makeText(this, "Connecting to chat thread...", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.sendMessage(conversationId, text);
            binding.etMessageInput.setText("");
        });
    }

    private void setupObservers() {
        viewModel.getActiveConversation().observe(this, conv -> {
            if (conv != null) {
                conversationId = conv.getId();
                listingTitle = conv.getListingTitle();
                partyName = conv.getOtherPartyName(viewModel.getCurrentUserId());
                updateToolbarTitles();
                viewModel.loadMessages(conversationId);
            }
        });

        viewModel.getMessages().observe(this, list -> {
            if (list == null || list.isEmpty()) {
                binding.layoutEmptyChat.setVisibility(View.VISIBLE);
                binding.rvMessages.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyChat.setVisibility(View.GONE);
                binding.rvMessages.setVisibility(View.VISIBLE);
                adapter.submitList(list, () -> {
                    if (list.size() > 0) {
                        binding.rvMessages.scrollToPosition(list.size() - 1);
                    }
                });
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressChat.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }
}
