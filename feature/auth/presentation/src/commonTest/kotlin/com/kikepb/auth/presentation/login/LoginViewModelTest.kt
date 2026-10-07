package com.kikepb.auth.presentation.login

import androidx.compose.runtime.snapshots.Snapshot
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.domain.usecase.LoginUseCase
import com.kikepb.domain.usecase.ResendEmailVerificationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuthRepository : AuthRepository {
        var loginResult: Result<AuthInfoModel, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)
        var resendResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
        val resentTo = mutableListOf<String>()

        override suspend fun login(email: String, password: String) = loginResult
        override suspend fun register(username: String, email: String, password: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> {
            resentTo += email
            return resendResult
        }
        override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
    }

    private class FakeSessionStorage : SessionStorage {
        private val info = MutableStateFlow<AuthInfoModel?>(null)
        override fun observeAuthInfo(): Flow<AuthInfoModel?> = info
        override suspend fun set(info: AuthInfoModel?) { this.info.value = info }
    }

    private val repository = FakeAuthRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel() = LoginViewModel(
        loginUseCase = LoginUseCase(repository),
        resendEmailVerificationUseCase = ResendEmailVerificationUseCase(repository),
        sessionStorage = FakeSessionStorage()
    )

    private fun LoginViewModel.fillCredentials() {
        state.value.emailTextFieldState.edit { replace(0, length, "me@example.com") }
        state.value.passwordTextFieldState.edit { replace(0, length, "secret123") }
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun `AC-002-08 login with unverified email offers to resend the verification email`() = runTest(UnconfinedTestDispatcher()) {
        repository.loginResult = Result.Failure(DataError.Remote.FORBIDDEN)
        val viewModel = createViewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.fillCredentials()

        viewModel.onAction(LoginAction.OnLoginClick)

        assertTrue(viewModel.state.value.canResendVerification)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `AC-002-08 resending uses the typed email and confirms it`() = runTest(UnconfinedTestDispatcher()) {
        repository.loginResult = Result.Failure(DataError.Remote.FORBIDDEN)
        val viewModel = createViewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.fillCredentials()
        viewModel.onAction(LoginAction.OnLoginClick)

        viewModel.onAction(LoginAction.OnResendVerificationClick)

        assertEquals(listOf("me@example.com"), repository.resentTo)
        assertFalse(viewModel.state.value.canResendVerification)
        assertNotNull(viewModel.state.value.info)
    }

    @Test
    fun `AC-002-08 wrong credentials do not offer resending`() = runTest(UnconfinedTestDispatcher()) {
        repository.loginResult = Result.Failure(DataError.Remote.UNAUTHORIZED)
        val viewModel = createViewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.fillCredentials()

        viewModel.onAction(LoginAction.OnLoginClick)

        assertFalse(viewModel.state.value.canResendVerification)
        assertNotNull(viewModel.state.value.error)
    }
}
