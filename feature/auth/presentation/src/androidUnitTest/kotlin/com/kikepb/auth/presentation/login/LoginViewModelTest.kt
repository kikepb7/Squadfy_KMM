package com.kikepb.auth.presentation.login

import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import com.kikepb.auth.presentation.fake.FakeAuthRepository
import com.kikepb.auth.presentation.fake.FakeSessionStorage
import com.kikepb.auth.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError.Remote.FORBIDDEN
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.usecase.LoginUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        sessionStorage = FakeSessionStorage()
        viewModel = LoginViewModel(
            loginUseCase = LoginUseCase(authRepository = authRepository),
            sessionStorage = sessionStorage
        )
    }

    @Test
    fun `GIVEN initial state WHEN no input provided THEN canLogin is false`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.canLogin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN valid email and non-blank password WHEN text fields filled THEN canLogin becomes true`() = runTest {
        // GIVEN
        viewModel.state.test {
            awaitItem()

            val emailField = viewModel.state.value.emailTextFieldState
            val passwordField = viewModel.state.value.passwordTextFieldState

            // WHEN
            emailField.edit { replace(0, length, "user@example.com") }
            passwordField.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            // THEN
            val updatedState = awaitItem()
            assertTrue(updatedState.canLogin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN invalid email WHEN text fields filled THEN canLogin remains false`() = runTest {
        // GIVEN
        viewModel.state.test {
            awaitItem()

            val emailField = viewModel.state.value.emailTextFieldState
            val passwordField = viewModel.state.value.passwordTextFieldState

            // WHEN
            emailField.edit { replace(0, length, "not-an-email") }
            passwordField.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            // THEN
            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canLogin)
        }
    }

    @Test
    fun `GIVEN valid credentials WHEN login clicked THEN Success event is emitted and session is stored`() = runTest {
        // GIVEN
        authRepository.loginResult = Success(data = FakeAuthRepository.defaultAuthInfoModel())

        viewModel.events.test {
            // WHEN
            val emailField = viewModel.state.value.emailTextFieldState
            val passwordField = viewModel.state.value.passwordTextFieldState
            emailField.edit { replace(0, length, "user@example.com") }
            passwordField.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            // THEN
            viewModel.state.test {
                awaitItem()
                viewModel.onAction(LoginAction.OnLoginClick)
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(LoginEvent.Success, awaitItem())
            assertNotNull(sessionStorage.savedInfo)
        }
    }

    @Test
    fun `GIVEN wrong credentials WHEN login clicked THEN error state is set with UNAUTHORIZED message`() = runTest {
        // GIVEN
        authRepository.loginResult = Failure(error = UNAUTHORIZED)

        viewModel.state.test {
            // WHEN
            val emailField = viewModel.state.value.emailTextFieldState
            val passwordField = viewModel.state.value.passwordTextFieldState
            emailField.edit { replace(0, length, "user@example.com") }
            passwordField.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canLogin) {
                state = awaitItem()
            }

            viewModel.onAction(LoginAction.OnLoginClick)

            state = awaitItem()
            state = awaitItem()

            // THEN
            assertFalse(state.isLoggingIn)
            assertNotNull(state.error)
            assertNull(sessionStorage.savedInfo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN unverified email WHEN login clicked THEN error state is set with FORBIDDEN message`() = runTest {
        // GIVEN
        authRepository.loginResult = Failure(error = FORBIDDEN)

        viewModel.state.test {
            // WHEN
            val emailField = viewModel.state.value.emailTextFieldState
            val passwordField = viewModel.state.value.passwordTextFieldState
            emailField.edit { replace(0, length, "unverified@example.com") }
            passwordField.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canLogin) {
                state = awaitItem()
            }

            viewModel.onAction(LoginAction.OnLoginClick)

            state = awaitItem()
            state = awaitItem()

            // THEN
            assertFalse(state.isLoggingIn)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN password is visible WHEN toggle clicked THEN isPasswordVisible becomes false`() = runTest {
        // GIVEN
        viewModel.state.test {
            // WHEN
            awaitItem()
            viewModel.onAction(LoginAction.OnTogglePasswordVisibility)
            val state = awaitItem()

            // THEN
            assertTrue(state.isPasswordVisible)
            viewModel.onAction(LoginAction.OnTogglePasswordVisibility)
            val toggled = awaitItem()
            assertFalse(toggled.isPasswordVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
