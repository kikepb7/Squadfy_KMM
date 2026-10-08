package com.kikepb.chat.presentation.profile

import app.cash.turbine.test
import com.kikepb.chat.domain.usecases.participant.FetchLocalParticipantUseCase
import com.kikepb.chat.domain.usecases.profile.ChangePasswordUseCase
import com.kikepb.chat.domain.usecases.profile.DeleteAccountUseCase
import com.kikepb.chat.domain.usecases.profile.DeleteProfilePictureUseCase
import com.kikepb.chat.domain.usecases.profile.UploadProfilePictureUseCase
import com.kikepb.chat.presentation.fake.FakeAuthRepository
import com.kikepb.chat.presentation.fake.FakeChatParticipantRepository
import com.kikepb.chat.presentation.fake.FakeChatRepository
import com.kikepb.chat.presentation.fake.FakeCrashReportingConsent
import com.kikepb.chat.presentation.fake.FakeFeatureFlags
import com.kikepb.chat.presentation.fake.FakeSessionStorage
import com.kikepb.chat.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import squadfy_app.feature.chat.presentation.generated.resources.Res
import squadfy_app.feature.chat.presentation.generated.resources.error_delete_account_wrong_password

class ProfileAccountSectionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var chatRepository: FakeChatRepository
    private lateinit var featureFlags: FakeFeatureFlags
    private lateinit var viewModel: ProfileViewModel
    private val crashReportingConsent = FakeCrashReportingConsent()

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        sessionStorage = FakeSessionStorage()
        chatRepository = FakeChatRepository()
        featureFlags = FakeFeatureFlags(FeatureFlag.ACCOUNT_DELETION)
        val participantRepository = FakeChatParticipantRepository()
        viewModel = ProfileViewModel(
            changePasswordUseCase = ChangePasswordUseCase(authRepository = authRepository),
            fetchLocalParticipantUseCase = FetchLocalParticipantUseCase(chatParticipantRepository = participantRepository),
            uploadProfilePictureUseCase = UploadProfilePictureUseCase(chatParticipantRepository = participantRepository),
            deleteProfilePictureUseCase = DeleteProfilePictureUseCase(chatParticipantRepository = participantRepository),
            sessionStorage = sessionStorage,
            deleteAccountUseCase = DeleteAccountUseCase(authRepository, sessionStorage, chatRepository),
            featureFlags = featureFlags,
            crashReportingConsent = crashReportingConsent
        )
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
    }

    private fun typePassword(password: String) {
        viewModel.state.value.deleteAccountPasswordState.edit { replace(0, length, password) }
    }

    @Test
    fun `AC-011-07 the delete account option follows the ACCOUNT_DELETION flag`() = runTest {
        collectState()
        assertTrue(viewModel.state.value.isAccountDeletionEnabled)

        featureFlags.set(FeatureFlag.ACCOUNT_DELETION, enabled = false)

        assertFalse(viewModel.state.value.isAccountDeletionEnabled)
    }

    @Test
    fun `AC-011-07 confirming with the password deletes the account, clears the session and emits OnAccountDeleted`() = runTest {
        sessionStorage.set(FakeAuthRepository.defaultAuthInfoModel())
        collectState()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnDeleteAccountClick)
            assertTrue(viewModel.state.value.showDeleteAccountDialog)
            typePassword("Secret123")
            viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)

            assertEquals(ProfileEvent.OnAccountDeleted, awaitItem())
        }
        assertEquals("Secret123", authRepository.lastDeleteAccountPassword)
        assertNull(sessionStorage.savedInfo)
        assertTrue(chatRepository.deleteAllChatsCalled)
        assertFalse(viewModel.state.value.showDeleteAccountDialog)
    }

    @Test
    fun `AC-011-07 a wrong password shows an error and keeps the session`() = runTest {
        sessionStorage.set(FakeAuthRepository.defaultAuthInfoModel())
        authRepository.deleteAccountResult = Result.Failure(DataError.Remote.UNAUTHORIZED)
        collectState()

        viewModel.onAction(ProfileAction.OnDeleteAccountClick)
        typePassword("wrong")
        viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)

        val state = viewModel.state.value
        assertEquals(Res.string.error_delete_account_wrong_password, (state.deleteAccountError as UiText.Resource).id)
        assertTrue(state.showDeleteAccountDialog)
        assertFalse(state.isDeletingAccount)
        assertNotNull(sessionStorage.savedInfo)
        assertFalse(chatRepository.deleteAllChatsCalled)
    }

    @Test
    fun `AC-011-07 nothing is sent without a password or with the flag off`() = runTest {
        collectState()

        viewModel.onAction(ProfileAction.OnDeleteAccountClick)
        viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)
        assertNull(authRepository.lastDeleteAccountPassword)

        featureFlags.set(FeatureFlag.ACCOUNT_DELETION, enabled = false)
        typePassword("Secret123")
        viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)
        assertNull(authRepository.lastDeleteAccountPassword)
    }

    @Test
    fun `AC-011-13 the crash reports switch reflects and changes the stored consent`() = runTest {
        collectState()
        assertTrue(viewModel.state.value.isCrashReportingAvailable)
        assertFalse(viewModel.state.value.crashReportsEnabled)

        viewModel.onAction(ProfileAction.OnCrashReportsChanged(enabled = true))

        assertTrue(crashReportingConsent.enabled.value)
        assertTrue(viewModel.state.value.crashReportsEnabled)
    }
}
