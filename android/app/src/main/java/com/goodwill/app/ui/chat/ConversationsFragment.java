package com.goodwill.app.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goodwill.app.databinding.FragmentConversationsBinding;

public class ConversationsFragment extends Fragment {

    private FragmentConversationsBinding binding;
    private ChatViewModel viewModel;
    private ConversationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConversationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        setupRefresh();
        setupRecyclerView();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadConversations();
    }

    private void setupRefresh() {
        binding.swipeRefreshConversations.setOnRefreshListener(() -> viewModel.loadConversations());
    }

    private void setupRecyclerView() {
        String currentUserId = viewModel.getCurrentUserId();
        adapter = new ConversationsAdapter(currentUserId, conversation -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getId());
            intent.putExtra(ChatActivity.EXTRA_LISTING_TITLE, conversation.getListingTitle());
            intent.putExtra(ChatActivity.EXTRA_PARTY_NAME, conversation.getOtherPartyName(currentUserId));
            startActivity(intent);
        });

        binding.rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvConversations.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
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

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressConversations.setVisibility(loading && !binding.swipeRefreshConversations.isRefreshing() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.swipeRefreshConversations.setRefreshing(false);
                if (getContext() != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
                viewModel.clearError();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
