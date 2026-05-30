package com.kikepb.auth.presentation.register_success

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.auth.presentation.fake.FakeAuthRepository
import com.kikepb.auth.presentation.util.MainDispatcherRule
import com.kikepb.core.domain.util.DataError.Remote.SERVER_ERROR
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.domain.usecase.ResendEmailVerificationUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterSuccessViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
    }

    private fun createViewModel(email: String? = "user@example.com") = RegisterSuccessViewModel(
        resendEmailVerificationUseCase = ResendEmailVerificationUseCase(authRepository),
        savedStateHandle = SavedStateHandle(mapOf("email" to email))
    )

    @Test
    fun `GIVEN email in SavedStateHandle WHEN state collected THEN registeredEmail is set`() = runTest {
        val viewModel = createViewModel(email = "user@example.com")

        viewModel.state.test {
            assertEquals("user@example.com", awaitItem().registeredEmail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN missing email WHEN ViewModel created THEN throws IllegalStateException`() {
        try {
            createViewModel(email = null)
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertNotNull(e)
        }
    }

    @Test
    fun `GIVEN valid email WHEN resend clicked THEN ResentVerificationEmailSuccess event is emitted`() = runTest {
        authRepository.resendVerificationResult = Success(data = Unit)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(RegisterSuccessAction.OnResendVerificationEmailClick)
            assertEquals(RegisterSuccessEvent.ResentVerificationEmailSuccess, awaitItem())
        }
    }

    @Test
    fun `GIVEN valid email WHEN resend succeeds THEN isResendingVerificationEmail becomes false`() = runTest {
        authRepository.resendVerificationResult = Success(data = Unit)
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onAction(RegisterSuccessAction.OnResendVerificationEmailClick)

            val sending = awaitItem()
            assertFalse(sending.isResendingVerificationEmail)
            assertNull(sending.resendVerificationError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN server error WHEN resend clicked THEN resendVerificationError is set`() = runTest {
        authRepository.resendVerificationResult = Failure(SERVER_ERROR)
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.onAction(RegisterSuccessAction.OnResendVerificationEmailClick)

            val error = awaitItem()

            assertFalse(error.isResendingVerificationEmail)
            assertNotNull(error.resendVerificationError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN resend is in progress WHEN clicked again THEN second request is ignored`() = runTest {
        authRepository.resendVerificationResult = Success(data = Unit)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(RegisterSuccessAction.OnResendVerificationEmailClick)
            viewModel.onAction(RegisterSuccessAction.OnResendVerificationEmailClick)

            assertEquals(RegisterSuccessEvent.ResentVerificationEmailSuccess, awaitItem())
            expectNoEvents()
        }
    }
}
