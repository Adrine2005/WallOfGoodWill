package com.goodwill.app.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.goodwill.app.data.remote.dto.GenerateQrRequest;
import com.goodwill.app.data.remote.dto.InviteDto;
import com.goodwill.app.data.repository.AdminRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

public class AdminViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private Application application;
    private AdminRepository repository;
    private AdminViewModel viewModel;

    @Before
    public void setUp() {
        application = mock(Application.class);
        repository = mock(AdminRepository.class);
        viewModel = new AdminViewModel(application, repository);
    }

    @Test
    public void testLoadInvites_Success() {
        List<InviteDto> mockList = new ArrayList<>();
        mockList.add(new InviteDto("GW-NOTICEBOARD-A", "Hostel Block A Main Notice Board", "2026-11-12", "2027-11-12", 1000, 14, true));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<List<InviteDto>> callback = invocation.getArgument(0);
                callback.onSuccess(mockList);
                return null;
            }
        }).when(repository).listInvites(any());

        viewModel.loadInvites();

        assertNotNull(viewModel.getInvites().getValue());
        assertEquals(1, viewModel.getInvites().getValue().size());
        assertEquals("GW-NOTICEBOARD-A", viewModel.getInvites().getValue().get(0).getCode());
        assertFalse(viewModel.getIsLoading().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
    }

    @Test
    public void testLoadInvites_Error() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<List<InviteDto>> callback = invocation.getArgument(0);
                callback.onError("Server offline");
                return null;
            }
        }).when(repository).listInvites(any());

        viewModel.loadInvites();

        assertEquals("Server offline", viewModel.getErrorMessage().getValue());
        assertFalse(viewModel.getIsLoading().getValue());
    }

    @Test
    public void testGenerateInvite_Success() {
        InviteDto newInvite = new InviteDto("GW-NEWCODE-1", "Library Poster", "2026-11-12", "2026-11-13", 50, 0, true);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<List<InviteDto>> callback = invocation.getArgument(0);
                callback.onSuccess(new ArrayList<>());
                return null;
            }
        }).when(repository).listInvites(any());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<InviteDto> callback = invocation.getArgument(1);
                callback.onSuccess(newInvite);
                return null;
            }
        }).when(repository).generateInvite(any(GenerateQrRequest.class), any());

        viewModel.generateInvite("Library Poster", 86400, 50);

        assertNotNull(viewModel.getNewlyGeneratedInvite().getValue());
        assertEquals("GW-NEWCODE-1", viewModel.getNewlyGeneratedInvite().getValue().getCode());
        assertEquals("Library Poster", viewModel.getNewlyGeneratedInvite().getValue().getLabel());
        assertFalse(viewModel.getIsLoading().getValue());
    }

    @Test
    public void testGenerateInvite_Error() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<InviteDto> callback = invocation.getArgument(1);
                callback.onError("Permission denied");
                return null;
            }
        }).when(repository).generateInvite(any(GenerateQrRequest.class), any());

        viewModel.generateInvite("Library Poster", 86400, 50);

        assertEquals("Permission denied", viewModel.getErrorMessage().getValue());
        assertNull(viewModel.getNewlyGeneratedInvite().getValue());
        assertFalse(viewModel.getIsLoading().getValue());
    }
}
