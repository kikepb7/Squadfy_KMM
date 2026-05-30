package com.kikepb.auth.presentation.register

import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import com.kikepb.auth.presentation.fake.FakeAuthRepository
import com.kikepb.auth.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError.Remote.CONFLICT
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.usecase.AuthRegisterUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        viewModel = RegisterViewModel(authRegisterUseCase = AuthRegisterUseCase(authRepository))
    }

    @Test
    fun `GIVEN initial state WHEN no input provided THEN canRegister is false`() = runTest {
        viewModel.state.test {
            assertFalse(awaitItem().canRegister)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN all valid inputs WHEN text fields filled THEN canRegister becomes true`() = runTest {
        viewModel.state.test {
            awaitItem()

            fillValidForm()
            Snapshot.sendApplyNotifications()

            val state = awaitItem()
            assertTrue(state.canRegister)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN username too short WHEN text fields filled THEN canRegister remains false`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.usernameTextState.edit { replace(0, length, "ab") }
            viewModel.state.value.emailTextState.edit { replace(0, length, "user@example.com") }
            viewModel.state.value.passwordTextState.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            cancelAndIgnoreRemainingEvents()
            assertFalse(viewModel.state.value.canRegister)
        }
    }

    @Test
    fun `GIVEN valid registration data WHEN register clicked THEN Success event emitted with email`() = runTest {
        authRepository.registerResult = Success(data = Unit)

        viewModel.events.test {
            viewModel.state.test {
                awaitItem()
                fillValidForm()
                Snapshot.sendApplyNotifications()
                awaitItem()

                viewModel.onAction(RegisterAction.OnRegisterClick)
                cancelAndIgnoreRemainingEvents()
            }

            val event = awaitItem()
            assertTrue(event is RegisterEvent.Success)
            assertEquals("user@example.com", (event as RegisterEvent.Success).email)
        }
    }

    @Test
    fun `GIVEN already existing account WHEN register clicked THEN registrationError is set`() = runTest {
        authRepository.registerResult = Failure(error = CONFLICT)

        viewModel.state.test {
            awaitItem()
            fillValidForm()
            Snapshot.sendApplyNotifications()

            var state = awaitItem()
            while (!state.canRegister) {
                state = awaitItem()
            }

            viewModel.onAction(RegisterAction.OnRegisterClick)

            state = awaitItem()
            state = awaitItem()

            assertFalse(state.isRegistering)
            assertNotNull(state.registrationError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN invalid email WHEN register clicked THEN emailError is set`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.usernameTextState.edit { replace(0, length, "validuser") }
            viewModel.state.value.emailTextState.edit { replace(0, length, "not-an-email") }
            viewModel.state.value.passwordTextState.edit { replace(0, length, "Password1") }
            Snapshot.sendApplyNotifications()

            viewModel.onAction(RegisterAction.OnRegisterClick)

            val state = awaitItem()
            assertNotNull(state.emailError)
            assertNull(state.usernameError)
            assertNull(state.passwordError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN invalid password WHEN register clicked THEN passwordError is set`() = runTest {
        viewModel.state.test {
            awaitItem()

            viewModel.state.value.usernameTextState.edit { replace(0, length, "validuser") }
            viewModel.state.value.emailTextState.edit { replace(0, length, "user@example.com") }
            viewModel.state.value.passwordTextState.edit { replace(0, length, "weak") }
            Snapshot.sendApplyNotifications()

            viewModel.onAction(RegisterAction.OnRegisterClick)

            val state = awaitItem()
            assertNotNull(state.passwordError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN password visibility toggled WHEN toggle action THEN isPasswordVisible changes`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(RegisterAction.OnTogglePasswordVisibilityClick)
            assertTrue(awaitItem().isPasswordVisible)
            viewModel.onAction(RegisterAction.OnTogglePasswordVisibilityClick)
            assertFalse(awaitItem().isPasswordVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun fillValidForm() {
        viewModel.state.value.usernameTextState.edit { replace(0, length, "validuser") }
        viewModel.state.value.emailTextState.edit { replace(0, length, "user@example.com") }
        viewModel.state.value.passwordTextState.edit { replace(0, length, "Password1") }
    }
}
