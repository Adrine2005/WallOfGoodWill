package com.goodwill.app.ui.listings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.goodwill.app.data.remote.dto.ListingDto;
import com.goodwill.app.data.repository.ListingsRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

public class ListingsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private Application application;
    private ListingsRepository repository;
    private ListingsViewModel viewModel;

    @Before
    public void setUp() {
        application = mock(Application.class);
        repository = mock(ListingsRepository.class);
        viewModel = new ListingsViewModel(application, repository);
    }

    @Test
    public void testLoadFeed_Success() {
        List<ListingDto> mockList = new ArrayList<>();
        ListingDto item = new ListingDto(
                "id-1", "seller-1", "Seller", "Block A", "101",
                "Calculus Textbook", "Good condition", "BOOKS",
                "SELL", 100.0, "GOOD", null, "ACTIVE", "2026-11-12"
        );
        mockList.add(item);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<List<ListingDto>> callback = invocation.getArgument(4);
                callback.onSuccess(mockList);
                return null;
            }
        }).when(repository).getListings(any(), any(), any(), any(), any());

        viewModel.loadFeed();

        assertFalse(viewModel.getIsLoading().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
        assertNotNull(viewModel.getListings().getValue());
        assertEquals(1, viewModel.getListings().getValue().size());
        assertEquals("Calculus Textbook", viewModel.getListings().getValue().get(0).getTitle());
    }

    @Test
    public void testLoadFeed_Error() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<List<ListingDto>> callback = invocation.getArgument(4);
                callback.onError("Connection refused");
                return null;
            }
        }).when(repository).getListings(any(), any(), any(), any(), any());

        viewModel.loadFeed();

        assertFalse(viewModel.getIsLoading().getValue());
        assertEquals("Connection refused", viewModel.getErrorMessage().getValue());
    }

    @Test
    public void testSetCategory_TriggersFeedLoadWithCategory() {
        viewModel.setCategory("BOOKS");

        verify(repository).getListings(eq("BOOKS"), any(), any(), any(), any());
    }

    @Test
    public void testClearError() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<List<ListingDto>> callback = invocation.getArgument(4);
                callback.onError("Some error");
                return null;
            }
        }).when(repository).getListings(any(), any(), any(), any(), any());

        viewModel.loadFeed();
        assertEquals("Some error", viewModel.getErrorMessage().getValue());

        viewModel.clearError();
        assertNull(viewModel.getErrorMessage().getValue());
    }
}
