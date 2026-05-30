package com.kikepb.auth.presentation.reset_password

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.auth.presentation.fake.FakeAuthRepository
import com.kikepb.auth.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError.Remote.CONFLICT
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.usecase.ResetPasswordUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeAuthRepository

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
    }

    private fun createViewModel(token: String? = "valid-token") = ResetPasswordViewModel(
        resetPasswordUseCase = ResetPasswordUseCase(repository),
        savedStateHandler = SavedStateHandle(mapOf("token" to token))
    )

    @Test
    fun `GIVEN missing token WHEN ViewModel created THEN throws IllegalStateException`() {
        try {
            createViewModel(token = null)
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertNotNull(e)
        }
    }

    @Test
    fun `GIVEN initial state THEN canSubmit is false`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertFalse(awaitItem().canSubmit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN a valid password WHEN password entered THEN canSubmit becomes true`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.passwordTextState.edit { replace(0, length, "NewPassword1") }
            Snapshot.sendApplyNotifications()

            val state = awaitItem()
            assertTrue(state.canSubmit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN a weak password WHEN password entered THEN canSubmit remains false`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.passwordTextState.edit { replace(0, length, "weak") }
            Snapshot.sendApplyNotifications()

            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canSubmit)
        }
    }

    @Test
    fun `GIVEN valid token and strong password WHEN submit clicked THEN isResetSuccessful becomes true`() = runTest {
        repository.resetPasswordResult = Success(data = Unit)
        val viewModel = createViewModel(token = "valid-token")

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.passwordTextState.edit { replace(0, length, "NewPassword1") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canSubmit) {
                state = awaitItem()
            }

            viewModel.onAction(ResetPasswordAction.OnSubmitClick)

            state = awaitItem()
            state = awaitItem()

            assertTrue(state.isResetSuccessful)
            assertFalse(state.isLoading)
            assertNull(state.errorText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN expired token WHEN submit clicked THEN errorText is set with token invalid message`() = runTest {
        repository.resetPasswordResult = Failure(error = UNAUTHORIZED)
        val viewModel = createViewModel(token = "expired-token")

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.passwordTextState.edit { replace(0, length, "NewPassword1") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canSubmit) {
                state = awaitItem()
            }

            viewModel.onAction(ResetPasswordAction.OnSubmitClick)

            state = awaitItem()
            state = awaitItem()

            assertFalse(state.isLoading)
            assertNotNull(state.errorText)
            assertFalse(state.isResetSuccessful)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN same password as current WHEN submit clicked THEN errorText is set with same password message`() = runTest {
        repository.resetPasswordResult = Failure(error = CONFLICT)
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.state.value.passwordTextState.edit { replace(0, length, "SamePassword1") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canSubmit) {
                state = awaitItem()
            }

            viewModel.onAction(ResetPasswordAction.OnSubmitClick)

            state = awaitItem()
            state = awaitItem()

            assertFalse(state.isLoading)
            assertNotNull(state.errorText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN password visibility toggle WHEN action fired THEN isPasswordVisible changes`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ResetPasswordAction.OnTogglePasswordVisibilityClick)
            assertTrue(awaitItem().isPasswordVisible)
            viewModel.onAction(ResetPasswordAction.OnTogglePasswordVisibilityClick)
            assertFalse(awaitItem().isPasswordVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
