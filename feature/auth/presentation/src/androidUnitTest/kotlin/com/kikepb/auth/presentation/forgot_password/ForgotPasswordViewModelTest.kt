package com.kikepb.auth.presentation.forgot_password

import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import com.kikepb.auth.presentation.fake.FakeAuthRepository
import com.kikepb.auth.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError.Remote.SERVER_ERROR
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        viewModel = ForgotPasswordViewModel(forgotPasswordUseCase = ForgotPasswordUseCase(authRepository = authRepository))
    }

    @Test
    fun `GIVEN initial state THEN canSubmit is false`() = runTest {
        viewModel.state.test {
            assertFalse(awaitItem().canSubmit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN a valid email WHEN email entered THEN canSubmit becomes true`() = runTest {
        // GIVEN
        viewModel.state.test {
            awaitItem()

            // WHEN
            viewModel.state.value.emailTextFieldState.edit { replace(0, length, "user@example.com") }
            Snapshot.sendApplyNotifications()

            // THEN
            val state = awaitItem()
            assertTrue(state.canSubmit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an invalid email WHEN email entered THEN canSubmit remains false`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.emailTextFieldState.edit { replace(0, length, "not-valid") }
            Snapshot.sendApplyNotifications()

            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canSubmit)
        }
    }

    @Test
    fun `GIVEN a valid email WHEN submit clicked THEN isEmailSentSuccessfully becomes true`() = runTest {
        // GIVEN
        authRepository.forgotPasswordResult = Success(Unit)

        viewModel.state.test {
            awaitItem()

            // WHEN
            viewModel.state.value.emailTextFieldState.edit { replace(0, length, "user@example.com") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canSubmit) {
                state = awaitItem()
            }

            viewModel.onAction(ForgotPasswordAction.OnSubmitClick)

            state = awaitItem()
            state = awaitItem()

            // THEN
            assertTrue(state.isEmailSentSuccessfully)
            assertFalse(state.isLoading)
            assertNull(state.errorText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN server error WHEN submit clicked THEN errorText is set`() = runTest {
        // GIVEN
        authRepository.forgotPasswordResult = Failure(error = SERVER_ERROR)

        viewModel.state.test {
            awaitItem()

            // WHEN
            viewModel.state.value.emailTextFieldState.edit { replace(0, length, "user@example.com") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canSubmit) {
                state = awaitItem()
            }

            viewModel.onAction(ForgotPasswordAction.OnSubmitClick)

            state = awaitItem()
            state = awaitItem()

            // THEN
            assertFalse(state.isLoading)
            assertNotNull(state.errorText)
            assertFalse(state.isEmailSentSuccessfully)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN currently loading WHEN submit clicked again THEN request is not duplicated`() = runTest {
        // GIVEN
        authRepository.forgotPasswordResult = Success(data = Unit)

        viewModel.state.test {
            awaitItem()

            // WHEN
            viewModel.state.value.emailTextFieldState.edit { replace(0, length, "user@example.com") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canSubmit) {
                state = awaitItem()
            }

            viewModel.onAction(ForgotPasswordAction.OnSubmitClick)
            state = awaitItem()

            viewModel.onAction(ForgotPasswordAction.OnSubmitClick)

            // THEN
            state = awaitItem()
            assertTrue(state.isEmailSentSuccessfully)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
