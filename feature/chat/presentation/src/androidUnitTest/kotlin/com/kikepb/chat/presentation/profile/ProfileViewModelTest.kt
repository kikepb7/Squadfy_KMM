package com.kikepb.chat.presentation.profile

import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import com.kikepb.chat.domain.usecases.participant.FetchLocalParticipantUseCase
import com.kikepb.chat.domain.usecases.profile.ChangePasswordUseCase
import com.kikepb.chat.domain.usecases.profile.DeleteAccountUseCase
import com.kikepb.chat.domain.usecases.profile.DeleteProfilePictureUseCase
import com.kikepb.chat.domain.usecases.profile.UploadProfilePictureUseCase
import com.kikepb.chat.presentation.fake.FakeAuthRepository
import com.kikepb.chat.presentation.fake.FakeChatParticipantRepository
import com.kikepb.chat.presentation.fake.FakeChatRepository
import com.kikepb.chat.presentation.fake.FakeFeatureFlags
import com.kikepb.chat.presentation.fake.FakeSessionStorage
import com.kikepb.chat.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var participantRepository: FakeChatParticipantRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        sessionStorage = FakeSessionStorage()
        participantRepository = FakeChatParticipantRepository()
        viewModel = createViewModel()
    }

    private fun createViewModel() = ProfileViewModel(
        changePasswordUseCase = ChangePasswordUseCase(authRepository = authRepository),
        fetchLocalParticipantUseCase = FetchLocalParticipantUseCase(chatParticipantRepository = participantRepository),
        uploadProfilePictureUseCase = UploadProfilePictureUseCase(chatParticipantRepository = participantRepository),
        deleteProfilePictureUseCase = DeleteProfilePictureUseCase(chatParticipantRepository = participantRepository),
        sessionStorage = sessionStorage,
        deleteAccountUseCase = DeleteAccountUseCase(authRepository, sessionStorage, FakeChatRepository()),
        featureFlags = FakeFeatureFlags()
    )

    @Test
    fun `GIVEN initial state WHEN no passwords entered THEN canChangePassword is false`() = runTest {
        viewModel.state.test {
            assertFalse(awaitItem().canChangePassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN valid current password and valid new password WHEN text fields filled THEN canChangePassword becomes true`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.currentPasswordTextState.edit { replace(0, length, "CurrentPass") }
            viewModel.state.value.newPasswordTextState.edit { replace(0, length, "NewPass123") }
            Snapshot.sendApplyNotifications()

            val state = awaitItem()
            assertTrue(state.canChangePassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN current password empty WHEN text fields changed THEN canChangePassword remains false`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.currentPasswordTextState.edit { replace(0, length, "") }
            viewModel.state.value.newPasswordTextState.edit { replace(0, length, "NewPass123") }
            Snapshot.sendApplyNotifications()

            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canChangePassword)
        }
    }

    @Test
    fun `GIVEN weak new password WHEN text fields changed THEN canChangePassword remains false`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.currentPasswordTextState.edit { replace(0, length, "CurrentPass" ) }
            viewModel.state.value.newPasswordTextState.edit { replace(0, length, "weak") }
            Snapshot.sendApplyNotifications()

            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canChangePassword)
        }
    }

    @Test
    fun `GIVEN valid passwords WHEN changePassword succeeds THEN isPasswordChangeSuccessful becomes true`() = runTest {
        authRepository.changePasswordResult = Result.Success(Unit)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.currentPasswordTextState.edit { replace(0, length, "CurrentPass") }
            viewModel.state.value.newPasswordTextState.edit { replace(0, length, "NewPass123") }
            Snapshot.sendApplyNotifications()
            awaitItem()

            viewModel.onAction(ProfileAction.OnChangePasswordClick)

            val loadingState = awaitItem()
            assertTrue(loadingState.isChangingPassword)

            val resultState = awaitItem()
            assertFalse(resultState.isChangingPassword)
            assertTrue(resultState.isPasswordChangeSuccessful)
            assertNull(resultState.newPasswordError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN wrong current password WHEN changePassword fails with UNAUTHORIZED THEN newPasswordError is set`() = runTest {
        authRepository.changePasswordResult = Result.Failure(error = DataError.Remote.UNAUTHORIZED)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.currentPasswordTextState.edit { replace(0, length, "WrongPass") }
            viewModel.state.value.newPasswordTextState.edit { replace(0, length, "NewPass123") }
            Snapshot.sendApplyNotifications()
            awaitItem()

            viewModel.onAction(ProfileAction.OnChangePasswordClick)

            val loadingState = awaitItem()
            assertTrue(loadingState.isChangingPassword)

            val errorState = awaitItem()
            assertFalse(errorState.isChangingPassword)
            assertNotNull(errorState.newPasswordError)
            assertFalse(errorState.isPasswordChangeSuccessful)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN same current and new password WHEN changePassword fails with CONFLICT THEN newPasswordError is set`() = runTest {
        authRepository.changePasswordResult = Result.Failure(error = DataError.Remote.CONFLICT)

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.currentPasswordTextState.edit { replace(0, length, "SamePass1") }
            viewModel.state.value.newPasswordTextState.edit { replace(0, length, "SamePass1") }
            Snapshot.sendApplyNotifications()
            awaitItem()

            viewModel.onAction(ProfileAction.OnChangePasswordClick)
            awaitItem() // isChangingPassword = true

            val errorState = awaitItem()
            assertNotNull(errorState.newPasswordError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN current password hidden WHEN OnToggleCurrentPasswordVisibility THEN isCurrentPasswordVisible toggles`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnToggleCurrentPasswordVisibility)
            assertTrue(awaitItem().isCurrentPasswordVisible)
            viewModel.onAction(ProfileAction.OnToggleCurrentPasswordVisibility)
            assertFalse(awaitItem().isCurrentPasswordVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN new password hidden WHEN OnToggleNewPasswordVisibility THEN isNewPasswordVisible toggles`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnToggleNewPasswordVisibility)
            assertTrue(awaitItem().isNewPasswordVisible)
            viewModel.onAction(ProfileAction.OnToggleNewPasswordVisibility)
            assertFalse(awaitItem().isNewPasswordVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN OnDeletePictureClick WHEN action dispatched THEN showDeleteConfirmationDialog becomes true`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnDeletePictureClick)
            assertTrue(awaitItem().showDeleteConfirmationDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN OnDismissDeleteConfirmationDialogClick THEN dialog is dismissed`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnDeletePictureClick)
            awaitItem()
            viewModel.onAction(ProfileAction.OnDismissDeleteConfirmationDialogClick)
            assertFalse(awaitItem().showDeleteConfirmationDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN picture exists WHEN OnConfirmDeleteClick succeeds THEN isDeletingImage becomes false`() = runTest {
        participantRepository.deleteProfilePictureResult = Result.Success(Unit)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnConfirmDeleteClick)

            val deletingState = awaitItem()
            assertTrue(deletingState.isDeletingImage)

            val doneState = awaitItem()
            assertFalse(doneState.isDeletingImage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN delete fails WHEN OnConfirmDeleteClick THEN imageError is set`() = runTest {
        participantRepository.deleteProfilePictureResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnConfirmDeleteClick)

            awaitItem() // isDeletingImage = true

            val errorState = awaitItem()
            assertFalse(errorState.isDeletingImage)
            assertNotNull(errorState.imageError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN valid image bytes WHEN OnPictureSelected succeeds THEN isUploadingImage becomes false`() = runTest {
        participantRepository.uploadProfilePictureResult = Result.Success(Unit)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnPictureSelected(bytes = byteArrayOf(1, 2, 3), mimeType = "image/jpeg"))

            val uploadingState = awaitItem()
            assertTrue(uploadingState.isUploadingImage)

            val doneState = awaitItem()
            assertFalse(doneState.isUploadingImage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN null mimeType WHEN OnPictureSelected THEN imageError is set without uploading`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ProfileAction.OnPictureSelected(bytes = byteArrayOf(1, 2, 3), mimeType = null))

            val errorState = awaitItem()
            assertFalse(errorState.isUploadingImage)
            assertNotNull(errorState.imageError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
