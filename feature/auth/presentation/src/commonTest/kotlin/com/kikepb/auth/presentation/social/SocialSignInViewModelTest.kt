package com.kikepb.auth.presentation.social

import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import com.kikepb.auth.presentation.complete_profile.CompleteProfileAction
import com.kikepb.auth.presentation.complete_profile.CompleteProfileEvent
import com.kikepb.auth.presentation.complete_profile.CompleteProfileViewModel
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.SignInMethod
import com.kikepb.core.domain.auth.model.SocialCredential
import com.kikepb.core.domain.auth.model.SocialCredentialResult
import com.kikepb.core.domain.auth.model.SocialSignInResult
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.auth.repository.SocialCredentialProvider
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SocialSignInViewModelTest {

    private val authInfo = AuthInfoModel("", "", UserModel("u1", "a@b.c", "Álvaro", true, null))

    private class FakeProvider(var result: SocialCredentialResult) : SocialCredentialProvider {
        override val availableMethods = listOf(SignInMethod.APPLE, SignInMethod.GOOGLE)
        override suspend fun obtainCredential(method: SignInMethod) = result
        override suspend fun revokeAccess(credential: SocialCredential) = Unit
    }

    private class FakeAuthRepository : AuthRepository {
        var signInResult: Result<SocialSignInResult, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)
        var completeResult: Result<AuthInfoModel, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)
        val completed = mutableListOf<String>()
        var abandoned = false

        override suspend fun signInWithProvider(credential: SocialCredential) = signInResult
        override suspend fun completeProfile(username: String): Result<AuthInfoModel, DataError.Remote> {
            completed += username
            return completeResult
        }
        override suspend fun abandonIncompleteAccount(): EmptyResult<DataError.Remote> {
            abandoned = true
            return Result.Success(Unit)
        }
        override suspend fun login(email: String, password: String): Result<AuthInfoModel, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)
        override suspend fun register(username: String, email: String, password: String): Result<Boolean, DataError.Remote> = Result.Success(false)
        override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
        override suspend fun deleteAccount(password: String): EmptyResult<DataError.Remote> = Result.Success(Unit)
    }

    private class FakeSessionStorage : SessionStorage {
        val info = MutableStateFlow<AuthInfoModel?>(null)
        override fun observeAuthInfo(): Flow<AuthInfoModel?> = info
        override suspend fun set(info: AuthInfoModel?) { this.info.value = info }
    }

    private val repository = FakeAuthRepository()
    private val session = FakeSessionStorage()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `AC-019-23 a known account signs in and stores its session`() = runTest(UnconfinedTestDispatcher()) {
        repository.signInResult = Result.Success(SocialSignInResult.SignedIn(authInfo))
        val viewModel = SocialSignInViewModel(FakeProvider(SocialCredentialResult.Success(SocialCredential.Google("token"))), repository, session)
        assertEquals(listOf(SignInMethod.APPLE, SignInMethod.GOOGLE), viewModel.state.value.methods)
        viewModel.events.test {
            viewModel.onAction(SocialSignInAction.OnMethodClick(SignInMethod.GOOGLE))
            assertEquals(SocialSignInEvent.SignedIn, awaitItem())
        }
        assertEquals(authInfo, session.info.value)
        assertNull(viewModel.state.value.inProgress)
    }

    @Test
    fun `AC-019-23 the first sign-in asks for the username with a suggestion`() = runTest(UnconfinedTestDispatcher()) {
        repository.signInResult = Result.Success(SocialSignInResult.NeedsProfile("Álvaro Gil"))
        val viewModel = SocialSignInViewModel(FakeProvider(SocialCredentialResult.Success(SocialCredential.Apple("t", "n"))), repository, session)
        viewModel.events.test {
            viewModel.onAction(SocialSignInAction.OnMethodClick(SignInMethod.APPLE))
            assertEquals(SocialSignInEvent.NeedsProfile("Álvaro Gil"), awaitItem())
        }
        assertNull(session.info.value)
    }

    @Test
    fun `AC-019-23 closing the sheet is not an error, a failure is`() = runTest(UnconfinedTestDispatcher()) {
        val provider = FakeProvider(SocialCredentialResult.Cancelled)
        val viewModel = SocialSignInViewModel(provider, repository, session)
        viewModel.onAction(SocialSignInAction.OnMethodClick(SignInMethod.GOOGLE))
        assertNull(viewModel.state.value.error)
        provider.result = SocialCredentialResult.Failed
        viewModel.onAction(SocialSignInAction.OnMethodClick(SignInMethod.GOOGLE))
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `AC-019-23 the username step validates 3-20 characters and reports a taken name`() = runTest(UnconfinedTestDispatcher()) {
        repository.completeResult = Result.Failure(DataError.Remote.CONFLICT)
        val viewModel = CompleteProfileViewModel("Al", repository, session)
        Snapshot.sendApplyNotifications()
        assertFalse(viewModel.state.value.canContinue)
        viewModel.state.value.usernameTextState.edit { replace(0, length, "Álvaro") }
        Snapshot.sendApplyNotifications()
        assertTrue(viewModel.state.value.canContinue)
        viewModel.onAction(CompleteProfileAction.OnContinueClick)
        assertEquals(listOf("Álvaro"), repository.completed)
        assertNotNull(viewModel.state.value.error)

        repository.completeResult = Result.Success(authInfo)
        viewModel.events.test {
            viewModel.onAction(CompleteProfileAction.OnContinueClick)
            assertEquals(CompleteProfileEvent.Done, awaitItem())
        }
        assertEquals(authInfo, session.info.value)
    }

    @Test
    fun `AC-019-23 leaving the username step deletes the half-created account`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = CompleteProfileViewModel("Álvaro", repository, session)
        viewModel.events.test {
            viewModel.onAction(CompleteProfileAction.OnCancelClick)
            assertEquals(CompleteProfileEvent.Left, awaitItem())
        }
        assertTrue(repository.abandoned)
    }
}
