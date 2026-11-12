package com.goodwill.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.goodwill.app.databinding.ActivityMainBinding;
import com.goodwill.app.ui.chat.ChatViewModel;
import com.goodwill.app.ui.chat.ConversationsFragment;
import com.goodwill.app.ui.listings.CreateListingActivity;
import com.goodwill.app.ui.listings.FeedFragment;
import com.goodwill.app.ui.notifications.NotificationsViewModel;
import com.goodwill.app.ui.profile.ProfileFragment;
import com.goodwill.app.ui.wishlist.WishlistFragment;

public class MainActivity extends AppCompatActivity {

    public static final int TAB_EXPLORE = 0;
    public static final int TAB_WISHLIST = 1;
    public static final int TAB_CHATS = 2;
    public static final int TAB_PROFILE = 3;

    private static final String TAG_FEED = "tag_feed";
    private static final String TAG_WISHLIST = "tag_wishlist";
    private static final String TAG_CHATS = "tag_chats";
    private static final String TAG_PROFILE = "tag_profile";
    private static final String KEY_CURRENT_TAB = "key_current_tab";

    private ActivityMainBinding binding;
    private NotificationsViewModel notifViewModel;
    private ChatViewModel chatViewModel;

    private FeedFragment feedFragment;
    private WishlistFragment wishlistFragment;
    private ConversationsFragment conversationsFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;

    private int currentTabIndex = TAB_EXPLORE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notifViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupFragments(savedInstanceState);
        setupBottomDock();
        setupBackNavigation();
        setupBadgeObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifViewModel.loadUnreadCount();
        chatViewModel.loadConversations();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_TAB, currentTabIndex);
    }

    private void setupFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            feedFragment = new FeedFragment();
            wishlistFragment = new WishlistFragment();
            conversationsFragment = new ConversationsFragment();
            profileFragment = new ProfileFragment();

            fm.beginTransaction()
                    .add(R.id.nav_host_fragment_container, feedFragment, TAG_FEED)
                    .add(R.id.nav_host_fragment_container, wishlistFragment, TAG_WISHLIST)
                    .add(R.id.nav_host_fragment_container, conversationsFragment, TAG_CHATS)
                    .add(R.id.nav_host_fragment_container, profileFragment, TAG_PROFILE)
                    .hide(wishlistFragment)
                    .hide(conversationsFragment)
                    .hide(profileFragment)
                    .show(feedFragment)
                    .commit();

            activeFragment = feedFragment;
            currentTabIndex = TAB_EXPLORE;
        } else {
            feedFragment = (FeedFragment) fm.findFragmentByTag(TAG_FEED);
            wishlistFragment = (WishlistFragment) fm.findFragmentByTag(TAG_WISHLIST);
            conversationsFragment = (ConversationsFragment) fm.findFragmentByTag(TAG_CHATS);
            profileFragment = (ProfileFragment) fm.findFragmentByTag(TAG_PROFILE);

            currentTabIndex = savedInstanceState.getInt(KEY_CURRENT_TAB, TAB_EXPLORE);
            switch (currentTabIndex) {
                case TAB_WISHLIST:
                    activeFragment = wishlistFragment;
                    break;
                case TAB_CHATS:
                    activeFragment = conversationsFragment;
                    break;
                case TAB_PROFILE:
                    activeFragment = profileFragment;
                    break;
                case TAB_EXPLORE:
                default:
                    activeFragment = feedFragment;
                    break;
            }
        }

        updateDockUi(currentTabIndex);
    }

    private void setupBottomDock() {
        binding.floatingDock.tabExplore.setOnClickListener(v -> switchTab(TAB_EXPLORE));
        binding.floatingDock.tabWishlist.setOnClickListener(v -> switchTab(TAB_WISHLIST));
        binding.floatingDock.tabPost.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateListingActivity.class));
        });
        binding.floatingDock.tabChats.setOnClickListener(v -> switchTab(TAB_CHATS));
        binding.floatingDock.tabProfile.setOnClickListener(v -> switchTab(TAB_PROFILE));
    }

    public void switchTab(int tabIndex) {
        if (tabIndex == currentTabIndex && activeFragment != null && activeFragment.isAdded()) {
            return;
        }

        Fragment target;
        switch (tabIndex) {
            case TAB_WISHLIST:
                target = wishlistFragment;
                break;
            case TAB_CHATS:
                target = conversationsFragment;
                break;
            case TAB_PROFILE:
                target = profileFragment;
                break;
            case TAB_EXPLORE:
            default:
                target = feedFragment;
                break;
        }

        if (target != null && target.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
            currentTabIndex = tabIndex;
            updateDockUi(tabIndex);
        }
    }

    private void updateDockUi(int activeTab) {
        int activeColor = ContextCompat.getColor(this, R.color.primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);

        // Explore
        binding.floatingDock.tvTabExplore.setTextColor(activeTab == TAB_EXPLORE ? activeColor : inactiveColor);
        binding.floatingDock.tvTabExplore.setTypeface(null, activeTab == TAB_EXPLORE ? Typeface.BOLD : Typeface.NORMAL);

        // Wishlist
        binding.floatingDock.tvTabWishlist.setTextColor(activeTab == TAB_WISHLIST ? activeColor : inactiveColor);
        binding.floatingDock.tvTabWishlist.setTypeface(null, activeTab == TAB_WISHLIST ? Typeface.BOLD : Typeface.NORMAL);

        // Chats
        binding.floatingDock.tvTabChats.setTextColor(activeTab == TAB_CHATS ? activeColor : inactiveColor);
        binding.floatingDock.tvTabChats.setTypeface(null, activeTab == TAB_CHATS ? Typeface.BOLD : Typeface.NORMAL);

        // Profile
        binding.floatingDock.tvTabProfile.setTextColor(activeTab == TAB_PROFILE ? activeColor : inactiveColor);
        binding.floatingDock.tvTabProfile.setTypeface(null, activeTab == TAB_PROFILE ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTabIndex != TAB_EXPLORE) {
                    switchTab(TAB_EXPLORE);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupBadgeObservers() {
        chatViewModel.getConversations().observe(this, conversations -> {
            if (conversations != null && !conversations.isEmpty()) {
                int count = conversations.size();
                binding.floatingDock.tvChatBadge.setVisibility(View.VISIBLE);
                binding.floatingDock.tvChatBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            } else {
                binding.floatingDock.tvChatBadge.setVisibility(View.GONE);
            }
        });
    }
}
