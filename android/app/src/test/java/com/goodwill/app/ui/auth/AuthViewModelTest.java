package com.goodwill.app.ui.auth;

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

import com.goodwill.app.data.remote.dto.AuthResponse;
import com.goodwill.app.data.remote.dto.UserDto;
import com.goodwill.app.data.repository.AuthRepository;
import com.goodwill.app.data.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AuthViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private Application application;
    private AuthRepository authRepository;
    private AuthViewModel viewModel;

    @Before
    public void setUp() {
        application = mock(Application.class);
        authRepository = mock(AuthRepository.class);
        viewModel = new AuthViewModel(application, authRepository);
    }

    @Test
    public void testLogin_Success() {
        UserDto mockUser = new UserDto(
                "student-123", "Student User", "student@campus.edu",
                "Hostel Block A", "101", "9876543210", "student", true, "GW-NOTICEBOARD-A"
        );

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<UserDto> callback = invocation.getArgument(2);
                callback.onSuccess(mockUser);
                return null;
            }
        }).when(authRepository).login(eq("student@campus.edu"), eq("student123"), any());

        viewModel.login("student@campus.edu", "student123");

        assertFalse(viewModel.getIsLoading().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
        assertNotNull(viewModel.getLoginSuccess().getValue());
        assertEquals("student-123", viewModel.getLoginSuccess().getValue().getUid());
        assertEquals("student@campus.edu", viewModel.getLoginSuccess().getValue().getEmail());
    }

    @Test
    public void testLogin_Error() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<UserDto> callback = invocation.getArgument(2);
                callback.onError("invalid credentials");
                return null;
            }
        }).when(authRepository).login(any(), any(), any());

        viewModel.login("wrong@campus.edu", "wrongpass");

        assertFalse(viewModel.getIsLoading().getValue());
        assertEquals("invalid credentials", viewModel.getErrorMessage().getValue());
        assertNull(viewModel.getLoginSuccess().getValue());
    }

    @Test
    public void testVerifyQr_Success() {
        AuthResponse mockResp = new AuthResponse(
                "mock-token-verified", true, "student", "Verified successfully", null
        );

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                RepositoryCallback<AuthResponse> callback = invocation.getArgument(1);
                callback.onSuccess(mockResp);
                return null;
            }
        }).when(authRepository).verifyQr(eq("GW-NOTICEBOARD-A"), any());

        viewModel.verifyQr("GW-NOTICEBOARD-A");

        assertFalse(viewModel.getIsLoading().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
        assertNotNull(viewModel.getQrVerifySuccess().getValue());
        assertEquals("mock-token-verified", viewModel.getQrVerifySuccess().getValue().getToken());
    }

    @Test
    public void testLogout() {
        viewModel.logout();
        verify(authRepository).logout();
        assertEquals(Boolean.FALSE, viewModel.getSessionActive().getValue());
    }
}
